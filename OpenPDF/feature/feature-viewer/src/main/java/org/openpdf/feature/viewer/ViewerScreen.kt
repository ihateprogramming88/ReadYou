package org.openpdf.feature.viewer

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.openpdf.core.model.document.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    documentUri: Uri?,
    onNavigateBack: () -> Unit,
    viewModel: ViewerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(documentUri) {
        documentUri?.let { uri ->
            viewModel.onIntent(ViewerIntent.OpenDocument(uri.toString()))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                viewModel.onIntent(ViewerIntent.ToggleToolbar)
            },
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            state.error != null -> {
                Text(
                    text = state.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                )
            }
            else -> {
                // PDF page content area
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Page ${state.currentPage + 1} / ${state.totalPages}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        // Top app bar (auto-hide)
        AnimatedVisibility(
            visible = state.isToolbarVisible,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = state.documentInfo?.title
                            ?: state.documentInfo?.uri?.substringAfterLast('/')
                            ?: "OpenPDF",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onIntent(ViewerIntent.ToggleNightMode) }) {
                        Icon(
                            imageVector = if (state.viewMode == ViewMode.NIGHT) {
                                Icons.Filled.LightMode
                            } else {
                                Icons.Filled.DarkMode
                            },
                            contentDescription = "Toggle night mode",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ),
            )
        }

        // Bottom page slider (auto-hide)
        AnimatedVisibility(
            visible = state.isToolbarVisible && state.totalPages > 1,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Page ${state.currentPage + 1} / ${state.totalPages}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                if (state.totalPages > 1) {
                    Slider(
                        value = state.currentPage.toFloat(),
                        onValueChange = { page ->
                            viewModel.onIntent(ViewerIntent.GoToPage(page.toInt()))
                        },
                        valueRange = 0f..(state.totalPages - 1).toFloat(),
                        steps = (state.totalPages - 2).coerceAtLeast(0),
                    )
                }
            }
        }
    }
}
