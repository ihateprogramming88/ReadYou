package org.openpdf.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_documents")
data class RecentDocumentEntity(
    @PrimaryKey
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
    val addedAt: Long,
)
