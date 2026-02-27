package org.openpdf.core.model.recent

data class RecentDocument(
    val uri: String,
    val displayName: String,
    val title: String?,
    val author: String?,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val lastOpenedAt: Long,
    val lastPageIndex: Int,
    val lastZoom: Float,
    val thumbnailPath: String?,
    val isPasswordProtected: Boolean,
)
