package org.openpdf.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.openpdf.core.database.entity.RecentDocumentEntity

@Dao
interface RecentDocumentDao {

    @Query("SELECT * FROM recent_documents ORDER BY lastOpenedAt DESC")
    fun getRecentDocuments(): Flow<List<RecentDocumentEntity>>

    @Query("SELECT * FROM recent_documents ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun getRecentDocuments(limit: Int): Flow<List<RecentDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(document: RecentDocumentEntity)

    @Query("DELETE FROM recent_documents WHERE uri = :uri")
    suspend fun delete(uri: String)

    @Query("DELETE FROM recent_documents")
    suspend fun deleteAll()

    @Query(
        "UPDATE recent_documents SET lastPageIndex = :page, lastZoom = :zoom, " +
            "lastOpenedAt = :timestamp WHERE uri = :uri"
    )
    suspend fun updateReadingProgress(uri: String, page: Int, zoom: Float, timestamp: Long)
}
