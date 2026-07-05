package com.evest.antivirus.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evest.antivirus.data.SettingsRepository
import com.evest.antivirus.notifications.ScanWorker
import com.evest.antivirus.threatdb.ThreatDbUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val autoScanEnabled: Boolean = true,
    val intervalMinutes: Int = SettingsRepository.DEFAULT_INTERVAL_MINUTES,
    val updateDbUrl: String = "",
    val dbUpdateMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = SettingsRepository(application)
    private val updater = ThreatDbUpdater(application)

    val uiState: StateFlow<SettingsUiState> = combine(
        settings.autoScanEnabled,
        settings.autoScanIntervalMinutes,
        settings.updateDbUrl
    ) { enabled, interval, url ->
        SettingsUiState(enabled, interval, url ?: "")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setAutoScanEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setAutoScanEnabled(enabled)
            if (enabled) {
                val interval = settings.autoScanIntervalMinutes.first()
                ScanWorker.schedule(getApplication(), interval)
            } else {
                ScanWorker.cancel(getApplication())
            }
        }
    }

    fun setIntervalMinutes(minutes: Int) {
        viewModelScope.launch {
            settings.setAutoScanIntervalMinutes(minutes)
            if (settings.autoScanEnabled.first()) {
                ScanWorker.schedule(getApplication(), minutes)
            }
        }
    }

    fun updateThreatDatabase(url: String) {
        viewModelScope.launch {
            settings.setUpdateDbUrl(url)
            val result = withContext(Dispatchers.IO) { updater.updateFromUrl(url) }
            val message = when (result) {
                is ThreatDbUpdater.UpdateResult.Success ->
                    "База обновлена. Версия ${result.newVersion}, сигнатур: ${result.signatureCount}"
                is ThreatDbUpdater.UpdateResult.Failure ->
                    "Не удалось обновить базу: ${result.reason}"
            }
            _messageChannel.value = message
        }
    }

    private val _messageChannel = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _messageChannel

    fun clearMessage() { _messageChannel.value = null }
}
