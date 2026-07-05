package com.evest.antivirus.scan

import com.evest.antivirus.threatdb.Recommendation
import com.evest.antivirus.threatdb.Severity

enum class ScanType { QUICK, DEEP, MANUAL }

data class DetectedThreat(
    val signatureId: String,
    val family: String,
    val severity: Severity,
    val targetLabel: String,
    val targetPackage: String?,
    val targetPath: String?,
    val source: String,
    val description: String,
    val recommendation: Recommendation
)

data class ScanReport(
    val scanType: ScanType,
    val startedAt: Long,
    val finishedAt: Long,
    val itemsScanned: Int,
    val threats: List<DetectedThreat>
) {
    val threatCount get() = threats.size
    val durationMs get() = finishedAt - startedAt
}
