# ntfy & UnifiedPush Implementation Plan for ReadYou

## Executive Summary

This document outlines the comprehensive implementation plan for adding ntfy and UnifiedPush support to ReadYou. This will reduce battery drain by replacing aggressive polling (every 30 minutes) with event-driven push notifications.

### Expected Impact
- **Battery Savings**: 70-90% reduction in sync-related battery usage
- **Sync Efficiency**: Reduce from ~240,000 DB operations/day to ~500-1,000
- **User Experience**: Real-time updates instead of 30-minute delays

---

## Architecture Overview

### Current Architecture Issues
1. **Polling-based sync** every 30 minutes (default)
2. **5,000+ sequential database queries** per sync cycle
3. **No selective sync** - all feeds synced every cycle
4. **Network radio constantly active**

### Proposed Architecture
1. **UnifiedPush integration** for event-driven updates
2. **Selective sync** triggered by push notifications
3. **Fallback polling** for feeds without push support
4. **Per-feed push endpoint storage**

---

## Implementation Plan

### Phase 1: Dependencies & Database Schema

#### 1.1 Add UnifiedPush Dependency

**File**: `gradle/libs.versions.toml`
```toml
[versions]
unifiedpush = "2.5.0"

[libraries]
unifiedpush-connector = { group = "com.github.UnifiedPush", name = "android-connector", version.ref = "unifiedpush" }
```

**File**: `app/build.gradle.kts`
```kotlin
dependencies {
    implementation(libs.unifiedpush.connector)
}
```

#### 1.2 Create PushNotification Entity

**File**: `app/src/main/java/me/ash/reader/domain/model/push/PushNotification.kt`
```kotlin
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
    var lastPushAt: Date?
)
```

#### 1.3 Create PushNotificationDao

**File**: `app/src/main/java/me/ash/reader/domain/repository/PushNotificationDao.kt`
```kotlin
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
```

#### 1.4 Database Migration

**File**: `app/src/main/java/me/ash/reader/infrastructure/db/AndroidDatabase.kt`

Add migration from version 6 to 7:
```kotlin
@Database(
    entities = [Account::class, Feed::class, Article::class, Group::class, PushNotification::class],
    version = 7
)
```

**File**: `app/src/main/java/me/ash/reader/infrastructure/db/AndroidDatabase.kt` (migration)
```kotlin
object MIGRATION_6_7 : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS push_notification (
                id TEXT PRIMARY KEY NOT NULL,
                accountId INTEGER NOT NULL,
                feedId TEXT,
                endpoint TEXT NOT NULL,
                registeredAt INTEGER NOT NULL,
                lastPushAt INTEGER
            )
            """.trimIndent()
        )
        database.execSQL(
            "CREATE INDEX index_push_notification_accountId ON push_notification(accountId)"
        )
        database.execSQL(
            "CREATE INDEX index_push_notification_feedId ON push_notification(feedId)"
        )
        database.execSQL(
            """
            ALTER TABLE account ADD COLUMN unifiedPushEnabled INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
        database.execSQL(
            """
            ALTER TABLE account ADD COLUMN ntfyServerUrl TEXT DEFAULT 'https://ntfy.sh'
            """.trimIndent()
        )
    }
}
```

### Phase 2: Preferences & Settings

#### 2.1 Create UnifiedPushEnabledPreference

**File**: `app/src/main/java/me/ash/reader/infrastructure/preference/UnifiedPushEnabledPreference.kt`
```kotlin
package me.ash.reader.infrastructure.preference

import android.content.Context
import me.ash.reader.R
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

sealed class UnifiedPushEnabledPreference(val value: Boolean) {

    object On : UnifiedPushEnabledPreference(true)
    object Off : UnifiedPushEnabledPreference(false)

    fun put(accountId: Int, viewModel: AccountViewModel) {
        viewModel.update(accountId) { unifiedPushEnabled = this@UnifiedPushEnabledPreference }
    }

    fun toDesc(context: Context): String =
        when (this) {
            On -> context.getString(R.string.on)
            Off -> context.getString(R.string.off)
        }

    companion object {
        val default = Off
        val values = listOf(On, Off)
    }
}

operator fun UnifiedPushEnabledPreference.not(): UnifiedPushEnabledPreference =
    when (value) {
        true -> UnifiedPushEnabledPreference.Off
        false -> UnifiedPushEnabledPreference.On
    }
```

#### 2.2 Create Type Converters

**File**: `app/src/main/java/me/ash/reader/domain/model/account/UnifiedPushEnabledConverters.kt`
```kotlin
package me.ash.reader.domain.model.account

import androidx.room.TypeConverter
import me.ash.reader.infrastructure.preference.UnifiedPushEnabledPreference

class UnifiedPushEnabledConverters {

    @TypeConverter
    fun toUnifiedPushEnabled(value: Boolean): UnifiedPushEnabledPreference {
        return when (value) {
            true -> UnifiedPushEnabledPreference.On
            false -> UnifiedPushEnabledPreference.Off
        }
    }

    @TypeConverter
    fun fromUnifiedPushEnabled(preference: UnifiedPushEnabledPreference): Boolean {
        return preference.value
    }
}
```

#### 2.3 Update Account Model

**File**: `app/src/main/java/me/ash/reader/domain/model/account/Account.kt`
```kotlin
@Entity(tableName = "account")
data class Account(
    // ... existing fields ...
    @ColumnInfo(defaultValue = "0")
    var unifiedPushEnabled: UnifiedPushEnabledPreference = UnifiedPushEnabledPreference.default,
    @ColumnInfo(defaultValue = "https://ntfy.sh")
    var ntfyServerUrl: String? = "https://ntfy.sh",
)
```

### Phase 3: UnifiedPush Integration

#### 3.1 Create UnifiedPushReceiver

**File**: `app/src/main/java/me/ash/reader/infrastructure/push/UnifiedPushReceiver.kt`
```kotlin
package me.ash.reader.infrastructure.push

import android.content.Context
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import me.ash.reader.domain.repository.PushNotificationDao
import me.ash.reader.domain.service.RssService
import org.unifiedpush.android.connector.MessagingReceiver
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UnifiedPushReceiver : MessagingReceiver() {

    @Inject
    lateinit var pushNotificationDao: PushNotificationDao

    @Inject
    lateinit var unifiedPushService: UnifiedPushService

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onMessage(context: Context, message: ByteArray, instance: String) {
        Log.i("UnifiedPush", "Received message: ${String(message)}")
        scope.launch {
            val pushNotification = pushNotificationDao.queryByEndpoint(instance)
            if (pushNotification != null) {
                RssService.get().syncFeedSelective(pushNotification.feedId)
            }
        }
    }

    override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
        Log.i("UnifiedPush", "New endpoint: $endpoint")
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
```

#### 3.2 Create UnifiedPushService

**File**: `app/src/main/java/me/ash/reader/domain/service/UnifiedPushService.kt`
```kotlin
package me.ash.reader.domain.service

import android.content.Context
import android.util.Log
import me.ash.reader.domain.model.push.PushNotification
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.PushNotificationDao
import me.ash.reader.ui.ext.currentAccountId
import org.unifiedpush.android.connector.UnifiedPush
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnifiedPushService @Inject constructor(
    private val context: Context,
    private val accountDao: AccountDao,
    private val feedDao: FeedDao,
    private val pushNotificationDao: PushNotificationDao,
) {

    suspend fun registerForPush(accountId: Int) {
        val account = accountDao.queryById(accountId) ?: return
        if (!account.unifiedPushEnabled.value) return

        val feeds = feedDao.queryAll(accountId)
        feeds.forEach { feed ->
            val instance = "readyou_${accountId}_${feed.id}"
            UnifiedPush.registerApp(context, instance)
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
        val existing = pushNotificationDao.queryByEndpoint(instance)
        if (existing != null) {
            pushNotificationDao.delete(existing)
        }
    }
}
```

### Phase 4: Selective Sync Implementation

#### 4.1 Add Selective Sync to AbstractRssRepository

**File**: `app/src/main/java/me/ash/reader/domain/service/AbstractRssRepository.kt`

Add new method:
```kotlin
open suspend fun syncFeedSelective(feedId: String?): ListenableWorker.Result {
    if (feedId == null) return ListenableWorker.Result.failure()

    return supervisorScope {
        val preDate = Date()
        val feed = feedDao.queryById(feedId) ?: return@supervisorScope ListenableWorker.Result.failure()

        val feedWithArticle = syncFeed(feed, preDate)
        val newArticles = articleDao.insertListIfNotExist(feedWithArticle.articles)

        if (feedWithArticle.feed.isNotification) {
            notificationHelper.notify(feedWithArticle.copy(articles = newArticles))
        }

        ListenableWorker.Result.success()
    }
}
```

#### 4.2 Add Selective Sync to RssService

**File**: `app/src/main/java/me/ash/reader/domain/service/RssService.kt`

Add method:
```kotlin
abstract suspend fun syncFeedSelective(feedId: String?): ListenableWorker.Result
```

Implement in LocalRssService, FeverRssService, GoogleReaderRssService

### Phase 5: UI Implementation

#### 5.1 Add UnifiedPush Settings to AccountDetailsPage

**File**: `app/src/main/java/me/ash/reader/ui/page/settings/accounts/AccountDetailsPage.kt`

Add after "Synchronous" section:
```kotlin
item {
    Subtitle(
        modifier = Modifier.padding(horizontal = 24.dp),
        text = stringResource(R.string.unified_push),
    )
    SettingItem(
        title = stringResource(R.string.enable_unified_push),
        desc = stringResource(R.string.enable_unified_push_desc),
        onClick = {
            selectedAccount?.id?.let {
                (!selectedAccount.unifiedPushEnabled).put(it, viewModel)
                if (selectedAccount.unifiedPushEnabled.value) {
                    viewModel.registerUnifiedPush(it)
                } else {
                    viewModel.unregisterUnifiedPush(it)
                }
            }
        },
    ) {
        RYSwitch(activated = selectedAccount?.unifiedPushEnabled?.value == true) {
            selectedAccount?.id?.let {
                (!selectedAccount.unifiedPushEnabled).put(it, viewModel)
            }
        }
    }
    if (selectedAccount?.unifiedPushEnabled?.value == true) {
        Tips(text = stringResource(R.string.unified_push_tips))
    }
    Spacer(modifier = Modifier.height(24.dp))
}
```

#### 5.2 Add String Resources

**File**: `app/src/main/res/values/strings.xml`
```xml
<string name="unified_push">UnifiedPush</string>
<string name="enable_unified_push">Enable UnifiedPush</string>
<string name="enable_unified_push_desc">Reduce battery usage with push notifications</string>
<string name="unified_push_tips">Requires ntfy app or compatible UnifiedPush distributor</string>
```

### Phase 6: Integration

#### 6.1 Update AndroidManifest

**File**: `app/src/main/AndroidManifest.xml`

Add receiver:
```xml
<receiver
    android:name=".infrastructure.push.UnifiedPushReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="org.unifiedpush.android.connector.MESSAGE" />
        <action android:name="org.unifiedpush.android.connector.NEW_ENDPOINT" />
        <action android:name="org.unifiedpush.android.connector.UNREGISTERED" />
        <action android:name="org.unifiedpush.android.connector.REGISTRATION_FAILED" />
    </intent-filter>
</receiver>
```

#### 6.2 Update DatabaseModule

**File**: `app/src/main/java/me/ash/reader/infrastructure/di/DatabaseModule.kt`

Add:
```kotlin
@Provides
@Singleton
fun providePushNotificationDao(androidDatabase: AndroidDatabase): PushNotificationDao =
    androidDatabase.pushNotificationDao()
```

#### 6.3 Update SyncWorker

**File**: `app/src/main/java/me/ash/reader/domain/service/SyncWorker.kt`

Modify to check if push is enabled before syncing:
```kotlin
override suspend fun doWork(): Result {
    val account = accountDao.queryById(context.currentAccountId)
    if (account?.unifiedPushEnabled?.value == true) {
        return Result.success()
    }
    return RssService.get().sync(this)
}
```

---

## Testing Plan

### Unit Tests
1. Test PushNotificationDao CRUD operations
2. Test UnifiedPushService registration/unregistration
3. Test selective sync logic

### Integration Tests
1. Test push message reception
2. Test selective feed sync triggered by push
3. Test fallback to polling when push disabled
4. Test migration from version 6 to 7

### Manual Testing
1. Install ntfy app from F-Droid
2. Enable UnifiedPush in ReadYou settings
3. Subscribe to a feed
4. Trigger push notification from ntfy
5. Verify only affected feed syncs
6. Verify battery usage improvement

---

## Rollout Strategy

### Phase 1 (Week 1)
- Database schema changes
- Preferences implementation
- Basic UnifiedPush integration

### Phase 2 (Week 2)
- Selective sync implementation
- UI integration
- Testing

### Phase 3 (Week 3)
- Documentation
- Beta release to F-Droid
- User feedback collection

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| UnifiedPush not installed | High | Graceful fallback to polling |
| Push notification delays | Medium | Keep polling as backup |
| Battery drain from push | Low | UnifiedPush is battery-efficient |
| Migration failures | High | Extensive testing, rollback plan |

---

## Success Metrics

- Battery usage reduced by 70%+ for users with push enabled
- Sync operations reduced from 240k/day to <1k/day
- No increase in user-reported sync issues
- 30%+ adoption rate within 3 months

---

## References

- [UnifiedPush Documentation](https://unifiedpush.org/developers/android/)
- [ntfy Documentation](https://docs.ntfy.sh/)
- [Android WorkManager Best Practices](https://developer.android.com/topic/libraries/architecture/workmanager/advanced)
