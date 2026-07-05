package com.evest.antivirus.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

class EvestViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(application) as T
            modelClass.isAssignableFrom(ScanViewModel::class.java) -> ScanViewModel(application) as T
            modelClass.isAssignableFrom(ThreatLogViewModel::class.java) -> ThreatLogViewModel(application) as T
            modelClass.isAssignableFrom(ProtectionLogViewModel::class.java) -> ProtectionLogViewModel(application) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(application) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
