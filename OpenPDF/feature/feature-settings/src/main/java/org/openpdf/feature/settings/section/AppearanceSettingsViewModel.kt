package org.openpdf.feature.settings.section

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openpdf.core.datastore.PdfPreferencesDataStore
import org.openpdf.core.model.settings.ThemeMode
import javax.inject.Inject

data class AppearanceUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    private val preferencesDataStore: PdfPreferencesDataStore,
) : ViewModel() {

    private val mUiState = MutableStateFlow(AppearanceUiState())
    val uiState: StateFlow<AppearanceUiState> = mUiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesDataStore.viewerSettings.collect { settings ->
                mUiState.update { it.copy(themeMode = settings.themeMode) }
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesDataStore.updateThemeMode(mode)
        }
    }
}
