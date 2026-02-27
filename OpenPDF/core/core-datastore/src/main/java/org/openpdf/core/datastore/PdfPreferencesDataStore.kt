package org.openpdf.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openpdf.core.model.document.PageLayout
import org.openpdf.core.model.document.ViewMode
import org.openpdf.core.model.settings.ThemeMode
import org.openpdf.core.model.settings.ViewerSettings
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "openpdf_prefs")

@Singleton
class PdfPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val DEFAULT_PAGE_LAYOUT = intPreferencesKey("default_page_layout")
        val DEFAULT_VIEW_MODE = intPreferencesKey("default_view_mode")
        val DEFAULT_ZOOM = floatPreferencesKey("default_zoom")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val REMEMBER_LAST_PAGE = booleanPreferencesKey("remember_last_page")
    }

    val viewerSettings: Flow<ViewerSettings> = context.dataStore.data.map { prefs ->
        ViewerSettings(
            themeMode = ThemeMode.entries.getOrElse(
                prefs[Keys.THEME_MODE] ?: 0,
            ) { ThemeMode.SYSTEM },
            defaultPageLayout = PageLayout.entries.getOrElse(
                prefs[Keys.DEFAULT_PAGE_LAYOUT] ?: 1,
            ) { PageLayout.CONTINUOUS },
            defaultViewMode = ViewMode.entries.getOrElse(
                prefs[Keys.DEFAULT_VIEW_MODE] ?: 0,
            ) { ViewMode.NORMAL },
            defaultZoom = prefs[Keys.DEFAULT_ZOOM] ?: 1f,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: false,
            rememberLastPage = prefs[Keys.REMEMBER_LAST_PAGE] ?: true,
        )
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.ordinal }
    }

    suspend fun updateDefaultPageLayout(layout: PageLayout) {
        context.dataStore.edit { it[Keys.DEFAULT_PAGE_LAYOUT] = layout.ordinal }
    }

    suspend fun updateDefaultViewMode(mode: ViewMode) {
        context.dataStore.edit { it[Keys.DEFAULT_VIEW_MODE] = mode.ordinal }
    }

    suspend fun updateKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun updateRememberLastPage(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMEMBER_LAST_PAGE] = enabled }
    }
}
