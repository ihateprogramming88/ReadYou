package org.openpdf.core.model.settings

import org.openpdf.core.model.document.PageLayout
import org.openpdf.core.model.document.ViewMode

data class ViewerSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultPageLayout: PageLayout = PageLayout.CONTINUOUS,
    val defaultViewMode: ViewMode = ViewMode.NORMAL,
    val defaultZoom: Float = 1f,
    val keepScreenOn: Boolean = false,
    val rememberLastPage: Boolean = true,
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED,
}
