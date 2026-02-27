package org.openpdf.feature.filemanager

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openpdf.core.database.dao.RecentDocumentDao
import org.openpdf.core.database.entity.RecentDocumentEntity
import org.openpdf.core.model.recent.RecentDocument
import javax.inject.Inject

data class RecentFilesUiState(
    val recentDocuments: List<RecentDocument> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class RecentFilesViewModel @Inject constructor(
    private val recentDocumentDao: RecentDocumentDao,
) : ViewModel() {

    private val mUiState = MutableStateFlow(RecentFilesUiState())
    val uiState: StateFlow<RecentFilesUiState> = mUiState.asStateFlow()

    init {
        loadRecentDocuments()
    }

    private fun loadRecentDocuments() {
        viewModelScope.launch {
            recentDocumentDao.getRecentDocuments().collect { entities ->
                mUiState.update {
                    it.copy(
                        recentDocuments = entities.map { entity -> entity.toDomain() },
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun removeRecent(uri: String) {
        viewModelScope.launch {
            recentDocumentDao.delete(uri)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            recentDocumentDao.deleteAll()
        }
    }

    private fun RecentDocumentEntity.toDomain(): RecentDocument = RecentDocument(
        uri = uri,
        displayName = displayName,
        title = title,
        author = author,
        pageCount = pageCount,
        fileSizeBytes = fileSizeBytes,
        lastOpenedAt = lastOpenedAt,
        lastPageIndex = lastPageIndex,
        lastZoom = lastZoom,
        thumbnailPath = thumbnailPath,
        isPasswordProtected = isPasswordProtected,
    )
}
