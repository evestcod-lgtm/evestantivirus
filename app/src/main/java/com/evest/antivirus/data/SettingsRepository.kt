package com.evest.antivirus.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "evest_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val AUTO_SCAN_ENABLED = booleanPreferencesKey("auto_scan_enabled")
        val AUTO_SCAN_INTERVAL_MINUTES = intPreferencesKey("auto_scan_interval_minutes")
        val FIRST_LAUNCH_DONE = booleanPreferencesKey("first_launch_done")
        val LAST_SCAN_TIMESTAMP = longPreferencesKey("last_scan_timestamp")
        val VT_API_KEY = stringPreferencesKey("vt_api_key")
        val UPDATE_DB_URL = stringPreferencesKey("update_db_url")
    }

    companion object {
        const val DEFAULT_INTERVAL_MINUTES = 10
        const val MIN_INTERVAL_MINUTES = 15 // ограничение WorkManager (минимум для периодической работы)
    }

    val autoScanEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.AUTO_SCAN_ENABLED] ?: true }

    val autoScanIntervalMinutes: Flow<Int> =
        context.dataStore.data.map { it[Keys.AUTO_SCAN_INTERVAL_MINUTES] ?: DEFAULT_INTERVAL_MINUTES }

    val firstLaunchDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.FIRST_LAUNCH_DONE] ?: false }

    val lastScanTimestamp: Flow<Long> =
        context.dataStore.data.map { it[Keys.LAST_SCAN_TIMESTAMP] ?: 0L }

    val virusTotalApiKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.VT_API_KEY] }

    val updateDbUrl: Flow<String?> =
        context.dataStore.data.map { it[Keys.UPDATE_DB_URL] }

    suspend fun setAutoScanEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SCAN_ENABLED] = enabled }
    }

    suspend fun setAutoScanIntervalMinutes(minutes: Int) {
        // WorkManager не поддерживает периодичность реже, чем раз в 15 минут.
        // Если пользователь просит меньший интервал, честно приводим к минимуму,
        // а не притворяемся, что система может чаще.
        val safe = if (minutes < MIN_INTERVAL_MINUTES) MIN_INTERVAL_MINUTES else minutes
        context.dataStore.edit { it[Keys.AUTO_SCAN_INTERVAL_MINUTES] = safe }
    }

    suspend fun setFirstLaunchDone(done: Boolean) {
        context.dataStore.edit { it[Keys.FIRST_LAUNCH_DONE] = done }
    }

    suspend fun setLastScanTimestamp(ts: Long) {
        context.dataStore.edit { it[Keys.LAST_SCAN_TIMESTAMP] = ts }
    }

    suspend fun setVirusTotalApiKey(key: String) {
        context.dataStore.edit { it[Keys.VT_API_KEY] = key }
    }

    suspend fun setUpdateDbUrl(url: String) {
        context.dataStore.edit { it[Keys.UPDATE_DB_URL] = url }
    }
}
