package me.ash.reader.domain.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.infrastructure.preference.SyncIntervalPreference
import me.ash.reader.infrastructure.preference.SyncOnlyOnWiFiPreference
import me.ash.reader.infrastructure.preference.SyncOnlyWhenChargingPreference
import me.ash.reader.ui.ext.currentAccountId
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountDao: AccountDao,
    private val rssService: RssService,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result =
        withContext(Dispatchers.Default) {
            Log.i("RLog", "doWork: ")
            val account = accountDao.queryById(applicationContext.currentAccountId)
            if (account?.unifiedPushEnabled?.value == true) {
                Log.i("RLog", "UnifiedPush enabled, skipping periodic sync")
                return@withContext Result.success()
            }
            rssService.get().sync(this@SyncWorker).also {
                rssService.get().clearKeepArchivedArticles()
            }
        }

    companion object {

        private const val IS_SYNCING = "isSyncing"
        private const val WORK_NAME_PERIODIC = "ReadYou"
        private const val WORK_NAME_ONETIME = "SYNC_ONETIME"
        const val WORK_TAG = "SYNC_TAG"

        fun cancelOneTimeWork(workManager: WorkManager) {
            workManager.cancelUniqueWork(WORK_NAME_ONETIME)
        }

        fun cancelPeriodicWork(workManager: WorkManager) {
            workManager.cancelUniqueWork(WORK_NAME_PERIODIC)
        }

        fun enqueueOneTimeWork(
            workManager: WorkManager,
        ) {
            workManager.enqueueUniqueWork(
                WORK_NAME_ONETIME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<SyncWorker>().addTag(WORK_TAG).build()
            )
        }

        fun enqueuePeriodicWork(
            workManager: WorkManager,
            syncInterval: SyncIntervalPreference,
            syncOnlyWhenCharging: SyncOnlyWhenChargingPreference,
            syncOnlyOnWiFi: SyncOnlyOnWiFiPreference,
        ) {
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<SyncWorker>(syncInterval.value, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresCharging(syncOnlyWhenCharging.value)
                            .setRequiredNetworkType(if (syncOnlyOnWiFi.value) NetworkType.UNMETERED else NetworkType.CONNECTED)
                            .build()
                    )
                    .addTag(WORK_TAG)
                    .setInitialDelay(syncInterval.value, TimeUnit.MINUTES)
                    .build()
            )
        }

        fun setIsSyncing(boolean: Boolean) = workDataOf(IS_SYNCING to boolean)
        fun Data.getIsSyncing(): Boolean = getBoolean(IS_SYNCING, false)
    }
}
