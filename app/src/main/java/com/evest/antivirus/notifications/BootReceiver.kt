package com.evest.antivirus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.evest.antivirus.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val settings = SettingsRepository(context.applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            val enabled = settings.autoScanEnabled.first()
            if (enabled) {
                val interval = settings.autoScanIntervalMinutes.first()
                ScanWorker.schedule(context.applicationContext, interval)
            }
        }
    }
}
