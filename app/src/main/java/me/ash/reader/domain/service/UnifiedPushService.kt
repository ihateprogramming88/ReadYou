package me.ash.reader.domain.service

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.domain.model.push.PushNotification
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.PushNotificationDao
import org.unifiedpush.android.connector.UnifiedPush
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnifiedPushService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountDao: AccountDao,
    private val feedDao: FeedDao,
    private val pushNotificationDao: PushNotificationDao,
) {

    suspend fun registerForPush(accountId: Int) {
        val distributor = UnifiedPush.getDistributor(context)
        Log.i("UnifiedPush", "Registering for push notifications. Distributor: ${distributor ?: "NONE INSTALLED"}")

        if (distributor.isNullOrEmpty()) {
            Log.e("UnifiedPush", "No UnifiedPush distributor found! Please install ntfy or another distributor.")
            return
        }

        val feeds = feedDao.queryAll(accountId)
        Log.i("UnifiedPush", "Registering ${feeds.size} feeds for account $accountId")

        feeds.forEach { feed ->
            val instance = "readyou_${accountId}_${feed.id}"
            UnifiedPush.registerApp(context, instance)
            Log.i("UnifiedPush", "Registered instance: $instance")
        }
    }

    suspend fun unregisterFromPush(accountId: Int) {
        val pushNotifications = pushNotificationDao.queryByAccountId(accountId)
        pushNotifications.forEach { push ->
            val instance = "readyou_${accountId}_${push.feedId}"
            UnifiedPush.unregisterApp(context, instance)
        }
        pushNotificationDao.deleteByAccountId(accountId)
    }

    suspend fun onNewEndpoint(endpoint: String, instance: String) {
        val parts = instance.split("_")
        if (parts.size != 3 || parts[0] != "readyou") return

        val accountId = parts[1].toIntOrNull() ?: return
        val feedId = parts[2]

        val pushNotification = PushNotification(
            id = UUID.randomUUID().toString(),
            accountId = accountId,
            feedId = feedId,
            endpoint = endpoint,
            registeredAt = Date(),
            lastPushAt = null
        )
        pushNotificationDao.insert(pushNotification)
        Log.i("UnifiedPush", "Registered endpoint for feed: $feedId")
    }

    suspend fun onUnregistered(instance: String) {
        val parts = instance.split("_")
        if (parts.size != 3 || parts[0] != "readyou") return

        val feedId = parts[2]
        val existing = pushNotificationDao.queryByFeedId(feedId)
        if (existing != null) {
            pushNotificationDao.delete(existing)
        }
    }

    suspend fun updateLastPushTime(instance: String) {
        val parts = instance.split("_")
        if (parts.size != 3 || parts[0] != "readyou") return

        val feedId = parts[2]
        val push = pushNotificationDao.queryByFeedId(feedId)
        if (push != null) {
            push.lastPushAt = Date()
            pushNotificationDao.update(push)
        }
    }

    fun getDistributorName(): String {
        val distributor = UnifiedPush.getDistributor(context)
        return when {
            distributor.isNullOrEmpty() -> "Not installed"
            distributor.contains("ntfy") -> "ntfy"
            distributor.contains("nextpush") -> "NextPush"
            distributor.contains("fcm") -> "FCM"
            else -> distributor.substringAfterLast(".")
        }
    }
}
