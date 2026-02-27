package org.openpdf.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["documentUri", "pageIndex"], unique = true)],
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentUri: String,
    val pageIndex: Int,
    val title: String,
    val createdAt: Long,
    val sortOrder: Int = 0,
)
