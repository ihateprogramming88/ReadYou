package org.openpdf.feature.settings.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppearanceSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                Text(
                    text = "Theme",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            val themeOptions = listOf(
                "System default" to org.openpdf.core.model.settings.ThemeMode.SYSTEM,
                "Light" to org.openpdf.core.model.settings.ThemeMode.LIGHT,
                "Dark" to org.openpdf.core.model.settings.ThemeMode.DARK,
                "AMOLED black" to org.openpdf.core.model.settings.ThemeMode.AMOLED,
            )

            themeOptions.forEach { (label, mode) ->
                item {
                    ListItem(
                        headlineContent = { Text(label) },
                        trailingContent = {
                            RadioButton(
                                selected = state.themeMode == mode,
                                onClick = { viewModel.updateThemeMode(mode) },
                            )
                        },
                        modifier = Modifier.clickable { viewModel.updateThemeMode(mode) },
                    )
                }
            }
        }
    }
}

private val Int.dp get() = androidx.compose.ui.unit.dp.times(this.toFloat())
