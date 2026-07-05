package com.evest.antivirus.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evest.antivirus.data.AppDatabase
import com.evest.antivirus.data.ProtectionEvent
import com.evest.antivirus.data.ThreatEntity
import com.evest.antivirus.notifications.NotificationHelper
import com.evest.antivirus.quarantine.QuarantineManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThreatLogViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.get(application)
    private val quarantineManager = QuarantineManager(application)

    val threats: StateFlow<List<ThreatEntity>> = db.threatDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pendingUninstallIntent = MutableStateFlow<Intent?>(null)
    val pendingUninstallIntent: StateFlow<Intent?> = _pendingUninstallIntent

    fun quarantine(threat: ThreatEntity) {
        viewModelScope.launch {
            if (threat.targetPath != null) {
                quarantineManager.quarantineFile(threat)
                logResolved(threat, "QUARANTINED")
            }
        }
    }

    fun requestUninstall(threat: ThreatEntity) {
        val pkg = threat.targetPackage ?: return
        _pendingUninstallIntent.value = quarantineManager.buildUninstallIntent(pkg)
    }

    fun clearPendingIntent() {
        _pendingUninstallIntent.value = null
    }

    fun ignore(threat: ThreatEntity) {
        viewModelScope.launch {
            quarantineManager.markStatus(threat, "IGNORED")
            logResolved(threat, "IGNORED")
        }
    }

    fun confirmRemovedByUser(threat: ThreatEntity) {
        viewModelScope.launch {
            quarantineManager.markStatus(threat, "REMOVED")
            logResolved(threat, "REMOVED")
            NotificationHelper.showCleanupDone(getApplication(), 1)
        }
    }

    private suspend fun logResolved(threat: ThreatEntity, status: String) {
        db.protectionEventDao().insert(
            ProtectionEvent(
                timestamp = System.currentTimeMillis(),
                eventType = "THREAT_${status}",
                title = threat.targetLabel,
                details = "Угроза '${threat.family}' обработана: $status",
                status = status
            )
        )
    }
}

class ProtectionLogViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.get(application)

    val events: StateFlow<List<ProtectionEvent>> = db.protectionEventDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
