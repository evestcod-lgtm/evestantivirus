package com.evest.antivirus

import android.app.Application
import com.evest.antivirus.data.SettingsRepository
import com.evest.antivirus.notifications.NotificationHelper
import com.evest.antivirus.notifications.ScanWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EvestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)

        val settings = SettingsRepository(this)
        CoroutineScope(Dispatchers.IO).launch {
            if (settings.autoScanEnabled.first()) {
                val interval = settings.autoScanIntervalMinutes.first()
                ScanWorker.schedule(this@EvestApp, interval)
            }
        }
    }
}
