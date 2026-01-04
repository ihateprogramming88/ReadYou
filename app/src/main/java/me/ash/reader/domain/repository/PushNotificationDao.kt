package me.ash.reader.domain.repository

import androidx.room.*
import me.ash.reader.domain.model.push.PushNotification

@Dao
interface PushNotificationDao {

    @Query("SELECT * FROM push_notification WHERE accountId = :accountId")
    suspend fun queryByAccountId(accountId: Int): List<PushNotification>

    @Query("SELECT * FROM push_notification WHERE feedId = :feedId")
    suspend fun queryByFeedId(feedId: String): PushNotification?

    @Query("SELECT * FROM push_notification WHERE endpoint = :endpoint")
    suspend fun queryByEndpoint(endpoint: String): PushNotification?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg pushNotification: PushNotification)

    @Update
    suspend fun update(vararg pushNotification: PushNotification)

    @Delete
    suspend fun delete(vararg pushNotification: PushNotification)

    @Query("DELETE FROM push_notification WHERE accountId = :accountId")
    suspend fun deleteByAccountId(accountId: Int)

    @Query("DELETE FROM push_notification WHERE feedId = :feedId")
    suspend fun deleteByFeedId(feedId: String)
}
