package me.ash.reader.domain.model.push

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "push_notification")
data class PushNotification(
    @PrimaryKey
    var id: String,
    @ColumnInfo(index = true)
    var accountId: Int,
    @ColumnInfo(index = true)
    var feedId: String?,
    @ColumnInfo
    var endpoint: String,
    @ColumnInfo
    var registeredAt: Date,
    @ColumnInfo
    var lastPushAt: Date? = null,
)
