package com.evest.antivirus.scan

import android.content.Context
import com.evest.antivirus.data.AppDatabase
import com.evest.antivirus.data.ProtectionEvent
import com.evest.antivirus.data.ThreatEntity
import com.evest.antivirus.threatdb.ThreatDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScanEngine(private val context: Context) {

    suspend fun runScan(type: ScanType): ScanReport = withContext(Dispatchers.IO) {
        val startedAt = System.currentTimeMillis()
        val threatDb = ThreatDatabase.load(context)
        val appScanner = AppScanner(context, threatDb)
        val fileScanner = FileScanner(context, threatDb)

        val isDeep = type == ScanType.DEEP
        val appThreats = appScanner.scan(deep = isDeep)
        val fileThreats = if (type != ScanType.QUICK) fileScanner.scanDownloads() else emptyList()

        val allThreats = appThreats + fileThreats
        val itemsScanned = appScanner.installedAppCount() +
            if (type != ScanType.QUICK) fileScanner.scannableFileCount() else 0

        val finishedAt = System.currentTimeMillis()
        val report = ScanReport(type, startedAt, finishedAt, itemsScanned, allThreats)

        persist(report)
        report
    }

    private suspend fun persist(report: ScanReport) {
        val db = AppDatabase.get(context)
        val now = report.finishedAt

        if (report.threats.isEmpty()) {
            db.protectionEventDao().insert(
                ProtectionEvent(
                    timestamp = now,
                    eventType = "SCAN_COMPLETED",
                    title = "Проверка завершена",
                    details = "Проверено объектов: ${report.itemsScanned}. Угроз не найдено.",
                    status = "CLEANED"
                )
            )
            return
        }

        for (threat in report.threats) {
            db.threatDao().insert(
                ThreatEntity(
                    detectedAt = now,
                    signatureId = threat.signatureId,
                    family = threat.family,
                    severity = threat.severity.name,
                    targetLabel = threat.targetLabel,
                    targetPackage = threat.targetPackage,
                    targetPath = threat.targetPath,
                    source = threat.source,
                    description = threat.description,
                    recommendation = threat.recommendation.name,
                    status = "NEW"
                )
            )
        }

        db.protectionEventDao().insert(
            ProtectionEvent(
                timestamp = now,
                eventType = "THREAT_FOUND",
                title = "Обнаружены угрозы",
                details = "Найдено угроз: ${report.threatCount} из ${report.itemsScanned} проверенных объектов.",
                status = "ACTION_REQUIRED"
            )
        )
    }
}
