package com.evest.antivirus.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protection_events")
data class ProtectionEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val eventType: String,       // SCAN_COMPLETED, THREAT_FOUND, THREAT_REMOVED, THREAT_QUARANTINED, THREAT_IGNORED, ACTION_REQUIRED
    val title: String,
    val details: String,
    val status: String           // CLEANED, REMOVED, QUARANTINED, IGNORED, ACTION_REQUIRED
)

@Entity(tableName = "threats")
data class ThreatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val detectedAt: Long,
    val signatureId: String,
    val family: String,
    val severity: String,           // LOW, MEDIUM, HIGH, CRITICAL
    val targetLabel: String,        // имя приложения или файла
    val targetPackage: String?,     // package name, если это приложение
    val targetPath: String?,        // путь к файлу, если это файл
    val source: String,             // APP_SCAN, FILE_SCAN, PERMISSION_HEURISTIC
    val description: String,
    val recommendation: String,     // UNINSTALL, QUARANTINE, REVIEW, IGNORE
    val status: String              // NEW, QUARANTINED, REMOVED, IGNORED
)
