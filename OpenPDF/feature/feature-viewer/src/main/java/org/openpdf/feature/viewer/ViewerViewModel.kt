package org.openpdf.feature.viewer

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openpdf.core.common.di.IODispatcher
import org.openpdf.core.model.document.ViewMode
import org.openpdf.mupdf.DocumentSource
import org.openpdf.mupdf.MuPdfDocument
import org.openpdf.mupdf.MuPdfEngine
import org.openpdf.mupdf.PasswordRequiredException
import org.openpdf.mupdf.cache.PageBitmapCache
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val mupdfEngine: MuPdfEngine,
    private val pageBitmapCache: PageBitmapCache,
    private val savedStateHandle: SavedStateHandle,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val mUiState = MutableStateFlow(ViewerUiState())
    val uiState: StateFlow<ViewerUiState> = mUiState.asStateFlow()

    private val mSideEffect = Channel<ViewerSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<ViewerSideEffect> = mSideEffect.receiveAsFlow()

    private var mDocument: MuPdfDocument? = null

    fun onIntent(intent: ViewerIntent) {
        when (intent) {
            is ViewerIntent.OpenDocument -> openDocument(intent.uriString, intent.password)
            is ViewerIntent.GoToPage -> goToPage(intent.page)
            is ViewerIntent.ChangeZoom -> changeZoom(intent.zoom)
            is ViewerIntent.ChangeViewMode -> changeViewMode(intent.mode)
            is ViewerIntent.ChangePageLayout -> changePageLayout(intent.layout)
            is ViewerIntent.ToggleToolbar -> toggleToolbar()
            is ViewerIntent.ToggleNightMode -> toggleNightMode()
        }
    }

    private fun openDocument(uriString: String, password: String?) {
        viewModelScope.launch(ioDispatcher) {
            mUiState.update { it.copy(isLoading = true, error = null) }
            try {
                val uri = Uri.parse(uriString)
                val doc = mupdfEngine.openDocument(DocumentSource.FromUri(uri), password)
                mDocument = doc
                mUiState.update {
                    it.copy(
                        documentInfo = doc.info,
                        totalPages = doc.pageCount,
                        currentPage = 0,
                        isLoading = false,
                    )
                }
            } catch (e: PasswordRequiredException) {
                mUiState.update { it.copy(isLoading = false) }
                mSideEffect.send(ViewerSideEffect.PasswordRequired(uriString))
            } catch (e: Exception) {
                mUiState.update { it.copy(isLoading = false, error = e.message) }
                mSideEffect.send(ViewerSideEffect.ShowError(e.message ?: "Failed to open document"))
            }
        }
    }

    private fun goToPage(page: Int) {
        val state = mUiState.value
        if (page in 0 until state.totalPages) {
            mUiState.update { it.copy(currentPage = page) }
        }
    }

    private fun changeZoom(zoom: Float) {
        val clampedZoom = zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
        mUiState.update { it.copy(zoom = clampedZoom) }
    }

    private fun changeViewMode(mode: ViewMode) {
        mUiState.update { it.copy(viewMode = mode) }
    }

    private fun changePageLayout(layout: org.openpdf.core.model.document.PageLayout) {
        mUiState.update { it.copy(pageLayout = layout) }
    }

    private fun toggleToolbar() {
        mUiState.update { it.copy(isToolbarVisible = !it.isToolbarVisible) }
    }

    private fun toggleNightMode() {
        val current = mUiState.value.viewMode
        val newMode = if (current == ViewMode.NIGHT) ViewMode.NORMAL else ViewMode.NIGHT
        mUiState.update { it.copy(viewMode = newMode) }
    }

    override fun onCleared() {
        mDocument?.close()
        pageBitmapCache.clear()
        super.onCleared()
    }

    companion object {
        private const val MIN_ZOOM = 0.2f
        private const val MAX_ZOOM = 10f
    }
}
