package com.evest.antivirus.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.evest.antivirus.data.SettingsRepository
import com.evest.antivirus.scan.ScanEngine
import com.evest.antivirus.scan.ScanType
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ScanWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = SettingsRepository(applicationContext)
        if (!settings.autoScanEnabled.first()) {
            return Result.success() // автоскан отключён пользователем — честно ничего не делаем
        }

        NotificationHelper.ensureChannels(applicationContext)
        val report = ScanEngine(applicationContext).runScan(ScanType.QUICK)
        settings.setLastScanTimestamp(report.finishedAt)

        if (report.threatCount == 0) {
            NotificationHelper.showScanCleanResult(applicationContext, report.itemsScanned)
        } else {
            NotificationHelper.showThreatsFound(applicationContext, report.threatCount)
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "evest_auto_scan"

        fun schedule(context: Context, intervalMinutes: Int) {
            val safeInterval = if (intervalMinutes < 15) 15 else intervalMinutes
            val request = PeriodicWorkRequestBuilder<ScanWorker>(
                safeInterval.toLong(), TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
