package org.openpdf.feature.annotations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed interface AnnotationToolState {
    data object Idle : AnnotationToolState
    data class HighlightMode(val color: Int = 0xFFFFFF00.toInt()) : AnnotationToolState
    data class InkMode(val color: Int = 0xFF000000.toInt(), val width: Float = 2f) : AnnotationToolState
    data object TextMode : AnnotationToolState
    data object FreeTextMode : AnnotationToolState
}

@Composable
fun AnnotationToolbar(
    currentTool: AnnotationToolState,
    onToolSelected: (AnnotationToolState) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(onClick = { onToolSelected(AnnotationToolState.HighlightMode()) }) {
                Icon(Icons.Filled.BorderColor, contentDescription = "Highlight")
            }
            IconButton(onClick = { onToolSelected(AnnotationToolState.InkMode()) }) {
                Icon(Icons.Filled.Draw, contentDescription = "Ink")
            }
            IconButton(onClick = { onToolSelected(AnnotationToolState.TextMode) }) {
                Icon(Icons.Filled.NoteAlt, contentDescription = "Note")
            }
            IconButton(onClick = { onToolSelected(AnnotationToolState.FreeTextMode) }) {
                Icon(Icons.Filled.FormatColorText, contentDescription = "Text")
            }
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(Icons.Filled.Undo, contentDescription = "Undo")
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(Icons.Filled.Redo, contentDescription = "Redo")
            }
        }
    }
}
