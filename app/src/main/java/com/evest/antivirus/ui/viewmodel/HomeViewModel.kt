package com.evest.antivirus.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evest.antivirus.data.AppDatabase
import com.evest.antivirus.data.SettingsRepository
import com.evest.antivirus.scan.ScanEngine
import com.evest.antivirus.scan.ScanReport
import com.evest.antivirus.scan.ScanType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class ProtectionStatus { SAFE, WARNING, DANGER, SCANNING, UNKNOWN }

data class HomeUiState(
    val status: ProtectionStatus = ProtectionStatus.UNKNOWN,
    val activeThreatCount: Int = 0,
    val lastScanTimestamp: Long = 0L,
    val isScanning: Boolean = false,
    val lastReport: ScanReport? = null,
    val mascotMessage: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = SettingsRepository(application)
    private val db = AppDatabase.get(application)
    private val scanEngine = ScanEngine(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                db.threatDao().observeActive(),
                settings.lastScanTimestamp
            ) { threats, lastScan ->
                val status = when {
                    _uiState.value.isScanning -> ProtectionStatus.SCANNING
                    threats.any { it.severity == "CRITICAL" || it.severity == "HIGH" } -> ProtectionStatus.DANGER
                    threats.isNotEmpty() -> ProtectionStatus.WARNING
                    lastScan == 0L -> ProtectionStatus.UNKNOWN
                    else -> ProtectionStatus.SAFE
                }
                _uiState.value.copy(
                    status = status,
                    activeThreatCount = threats.size,
                    lastScanTimestamp = lastScan
                )
            }.collect { newState -> _uiState.value = newState }
        }
    }

    fun runManualScan(type: ScanType = ScanType.MANUAL) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, status = ProtectionStatus.SCANNING)
            val report = scanEngine.runScan(type)
            settings.setLastScanTimestamp(report.finishedAt)
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                lastReport = report,
                mascotMessage = if (report.threatCount == 0)
                    "Я твой защитник Евестевень. Всё чисто, можешь быть спокоен."
                else
                    "Я твой защитник Евестевень. Нашёл ${report.threatCount} угроз, посмотри журнал."
            )
        }
    }

    fun dismissMascot() {
        _uiState.value = _uiState.value.copy(mascotMessage = null)
    }
}
