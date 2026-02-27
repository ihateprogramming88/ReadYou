package org.openpdf.feature.viewer

import org.openpdf.core.model.document.PageLayout
import org.openpdf.core.model.document.PdfDocumentInfo
import org.openpdf.core.model.document.ViewMode

data class ViewerUiState(
    val documentInfo: PdfDocumentInfo? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val zoom: Float = 1f,
    val viewMode: ViewMode = ViewMode.NORMAL,
    val pageLayout: PageLayout = PageLayout.CONTINUOUS,
    val isToolbarVisible: Boolean = true,
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
)

sealed interface ViewerIntent {
    data class OpenDocument(val uriString: String, val password: String? = null) : ViewerIntent
    data class GoToPage(val page: Int) : ViewerIntent
    data class ChangeZoom(val zoom: Float) : ViewerIntent
    data class ChangeViewMode(val mode: ViewMode) : ViewerIntent
    data class ChangePageLayout(val layout: PageLayout) : ViewerIntent
    data object ToggleToolbar : ViewerIntent
    data object ToggleNightMode : ViewerIntent
}

sealed interface ViewerSideEffect {
    data class ShowError(val message: String) : ViewerSideEffect
    data class PasswordRequired(val uriString: String) : ViewerSideEffect
    data object DocumentSaved : ViewerSideEffect
}
