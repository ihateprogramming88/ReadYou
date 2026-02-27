package org.openpdf.core.model.bookmark

data class Bookmark(
    val id: Long = 0,
    val documentUri: String,
    val pageIndex: Int,
    val title: String,
    val createdAt: Long,
)
