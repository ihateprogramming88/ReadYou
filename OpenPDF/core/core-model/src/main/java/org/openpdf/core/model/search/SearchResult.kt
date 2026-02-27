package org.openpdf.core.model.search

data class SearchResult(
    val pageIndex: Int,
    val quads: List<FloatArray>,
    val snippetText: String,
)
