package org.openpdf.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import org.openpdf.core.database.dao.BookmarkDao
import org.openpdf.core.database.dao.RecentDocumentDao
import org.openpdf.core.database.entity.BookmarkEntity
import org.openpdf.core.database.entity.RecentDocumentEntity

@Database(
    entities = [
        RecentDocumentEntity::class,
        BookmarkEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PdfDatabase : RoomDatabase() {
    abstract fun recentDocumentDao(): RecentDocumentDao
    abstract fun bookmarkDao(): BookmarkDao
}
