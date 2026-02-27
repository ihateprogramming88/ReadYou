package org.openpdf.feature.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
) {
    // Placeholder: Full-text search with result highlighting and navigation.
    // Uses MuPDF's StructuredText.search() for per-page text search in Phase 3.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Search screen")
    }
}
