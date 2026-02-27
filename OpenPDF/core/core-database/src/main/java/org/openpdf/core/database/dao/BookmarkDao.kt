package org.openpdf.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.openpdf.core.database.entity.BookmarkEntity

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks WHERE documentUri = :uri ORDER BY sortOrder ASC, pageIndex ASC")
    fun getBookmarksForDocument(uri: String): Flow<List<BookmarkEntity>>

    @Insert
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun delete(bookmark: BookmarkEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE documentUri = :uri AND pageIndex = :page)")
    suspend fun isPageBookmarked(uri: String, page: Int): Boolean
}
