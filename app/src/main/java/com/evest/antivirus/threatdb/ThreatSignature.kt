package com.evest.antivirus.threatdb

enum class SignatureType { PACKAGE_NAME, SHA256, PERMISSION_COMBO, INSTALL_SOURCE }

enum class Severity(val weight: Int) {
    LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);

    companion object {
        fun from(text: String): Severity = when (text.lowercase()) {
            "critical" -> CRITICAL
            "high" -> HIGH
            "medium" -> MEDIUM
            else -> LOW
        }
    }
}

enum class Recommendation { UNINSTALL, QUARANTINE, REVIEW, IGNORE }

data class ThreatSignature(
    val id: String,
    val type: SignatureType,
    val matchSingle: String? = null,
    val matchList: List<String> = emptyList(),
    val severity: Severity,
    val family: String,
    val description: String,
    val recommendation: Recommendation
)
