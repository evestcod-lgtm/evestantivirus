package com.evest.antivirus.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evest.antivirus.data.SettingsRepository
import com.evest.antivirus.scan.ScanEngine
import com.evest.antivirus.scan.ScanReport
import com.evest.antivirus.scan.ScanType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanUiState(
    val running: Boolean = false,
    val scanType: ScanType? = null,
    val progressLabel: String = "",
    val report: ScanReport? = null
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val scanEngine = ScanEngine(application)
    private val settings = SettingsRepository(application)

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    fun startScan(type: ScanType) {
        if (_state.value.running) return
        viewModelScope.launch {
            _state.value = ScanUiState(running = true, scanType = type, progressLabel = labelFor(type))
            val report = scanEngine.runScan(type)
            settings.setLastScanTimestamp(report.finishedAt)
            _state.value = _state.value.copy(running = false, report = report)
        }
    }

    fun reset() {
        _state.value = ScanUiState()
    }

    private fun labelFor(type: ScanType) = when (type) {
        ScanType.QUICK -> "Быстрая проверка ключевых компонентов..."
        ScanType.DEEP -> "Глубокая проверка приложений и файлов..."
        ScanType.MANUAL -> "Проверка устройства..."
    }
}
