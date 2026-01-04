package me.ash.reader.infrastructure.push

import android.content.Context
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ash.reader.domain.repository.PushNotificationDao
import me.ash.reader.domain.service.RssService
import me.ash.reader.domain.service.UnifiedPushService
import org.unifiedpush.android.connector.MessagingReceiver
import javax.inject.Inject

@AndroidEntryPoint
class UnifiedPushReceiver : MessagingReceiver() {

    @Inject
    lateinit var pushNotificationDao: PushNotificationDao

    @Inject
    lateinit var unifiedPushService: UnifiedPushService

    @Inject
    lateinit var rssService: RssService

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onMessage(context: Context, message: ByteArray, instance: String) {
        Log.i("UnifiedPush", "Received message: ${String(message)}")
        scope.launch {
            val parts = instance.split("_")
            if (parts.size == 3 && parts[0] == "readyou") {
                val feedId = parts[2]
                unifiedPushService.updateLastPushTime(instance)
                rssService.get().syncFeedSelective(feedId)
            }
        }
    }

    override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
        Log.i("UnifiedPush", "New endpoint: $endpoint for instance: $instance")
        scope.launch {
            unifiedPushService.onNewEndpoint(endpoint, instance)
        }
    }

    override fun onRegistrationFailed(context: Context, instance: String) {
        Log.e("UnifiedPush", "Registration failed for: $instance")
    }

    override fun onUnregistered(context: Context, instance: String) {
        Log.i("UnifiedPush", "Unregistered: $instance")
        scope.launch {
            unifiedPushService.onUnregistered(instance)
        }
    }
}
