package com.evest.antivirus.threatdb

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File

/**
 * Локальная база сигнатур угроз.
 *
 * Источник данных, по приоритету:
 * 1. Обновлённый файл в filesDir/threat_db/signatures.json (если пользователь/updater его положил)
 * 2. Встроенный ассет assets/threat_db/signatures.json
 *
 * Формат — простой JSON, расширяемый без изменения кода (см. ThreatDbUpdater).
 */
class ThreatDatabase private constructor(
    val signatures: List<ThreatSignature>,
    val version: Int,
    val updatedAt: String
) {
    val packageSignatures: Map<String, ThreatSignature> =
        signatures.filter { it.type == SignatureType.PACKAGE_NAME }
            .associateBy { it.matchSingle!! }

    val hashSignatures: Map<String, ThreatSignature> =
        signatures.filter { it.type == SignatureType.SHA256 }
            .associateBy { it.matchSingle!!.lowercase() }

    val permissionComboSignatures: List<ThreatSignature> =
        signatures.filter { it.type == SignatureType.PERMISSION_COMBO }

    val installSourceSignatures: List<ThreatSignature> =
        signatures.filter { it.type == SignatureType.INSTALL_SOURCE }

    companion object {
        private const val TAG = "ThreatDatabase"
        private const val UPDATED_DB_DIR = "threat_db"
        private const val UPDATED_DB_FILE = "signatures.json"

        fun load(context: Context): ThreatDatabase {
            val updatedFile = File(File(context.filesDir, UPDATED_DB_DIR), UPDATED_DB_FILE)
            val jsonText = if (updatedFile.exists()) {
                Log.i(TAG, "Загружаю обновлённую базу угроз из ${updatedFile.path}")
                updatedFile.readText()
            } else {
                context.assets.open("threat_db/signatures.json").bufferedReader().use { it.readText() }
            }
            return parse(jsonText)
        }

        fun parse(jsonText: String): ThreatDatabase {
            val root = JSONObject(jsonText)
            val version = root.optInt("db_version", 1)
            val updated = root.optString("updated", "unknown")
            val arr = root.getJSONArray("signatures")
            val list = mutableListOf<ThreatSignature>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeStr = obj.getString("type")
                val type = when (typeStr) {
                    "package_name" -> SignatureType.PACKAGE_NAME
                    "sha256" -> SignatureType.SHA256
                    "permission_combo" -> SignatureType.PERMISSION_COMBO
                    "install_source" -> SignatureType.INSTALL_SOURCE
                    else -> continue
                }
                val recommendation = when (obj.optString("recommendation", "review")) {
                    "uninstall" -> Recommendation.UNINSTALL
                    "quarantine" -> Recommendation.QUARANTINE
                    "ignore" -> Recommendation.IGNORE
                    else -> Recommendation.REVIEW
                }

                val matchSingle: String?
                val matchList: List<String>
                if (type == SignatureType.PERMISSION_COMBO) {
                    val jArr = obj.getJSONArray("match")
                    matchList = (0 until jArr.length()).map { jArr.getString(it) }
                    matchSingle = null
                } else {
                    matchSingle = obj.getString("match")
                    matchList = emptyList()
                }

                list.add(
                    ThreatSignature(
                        id = obj.getString("id"),
                        type = type,
                        matchSingle = matchSingle,
                        matchList = matchList,
                        severity = Severity.from(obj.optString("severity", "low")),
                        family = obj.optString("family", "Unknown"),
                        description = obj.optString("description", ""),
                        recommendation = recommendation
                    )
                )
            }
            return ThreatDatabase(list, version, updated)
        }
    }
}
