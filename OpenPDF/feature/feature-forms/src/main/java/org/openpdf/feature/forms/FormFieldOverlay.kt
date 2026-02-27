package org.openpdf.feature.forms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FormFieldOverlay(
    modifier: Modifier = Modifier,
) {
    // Placeholder: Form field overlay renders interactive widgets on top of PDF pages.
    // Widgets include text fields, checkboxes, radio buttons, combo boxes, and list boxes.
    // Populated via MuPDF's PDFWidget API in Phase 3.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Form field overlay")
    }
}
