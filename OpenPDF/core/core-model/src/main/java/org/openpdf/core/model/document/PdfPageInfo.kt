package org.openpdf.core.model.document

data class PdfPageInfo(
    val pageIndex: Int,
    val widthPt: Float,
    val heightPt: Float,
    val rotation: Int,
    val label: String?,
)
