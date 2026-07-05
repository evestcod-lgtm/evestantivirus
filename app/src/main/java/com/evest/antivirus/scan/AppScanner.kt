package com.evest.antivirus.scan

import android.content.Context
import android.content.pm.PackageManager
import com.evest.antivirus.threatdb.SignatureType
import com.evest.antivirus.threatdb.ThreatDatabase

/**
 * Сканирует установленные приложения:
 *  - сверяет package name со списком известных вредоносных пакетов
 *  - анализирует запрошенные разрешения на подозрительные комбинации
 *  - отмечает источник установки, если он неизвестен
 *
 * Это ровно то, что Android реально позволяет сделать без root.
 */
class AppScanner(private val context: Context, private val db: ThreatDatabase) {

    fun scan(deep: Boolean): List<DetectedThreat> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val results = mutableListOf<DetectedThreat>()

        for (pkgInfo in packages) {
            val packageName = pkgInfo.packageName
            if (packageName == context.packageName) continue // не сканируем сами себя

            val appLabel = try {
                pm.getApplicationLabel(pkgInfo.applicationInfo!!).toString()
            } catch (e: Exception) { packageName }

            // 1. Проверка по имени пакета
            db.packageSignatures[packageName]?.let { sig ->
                results.add(
                    DetectedThreat(
                        signatureId = sig.id,
                        family = sig.family,
                        severity = sig.severity,
                        targetLabel = appLabel,
                        targetPackage = packageName,
                        targetPath = null,
                        source = "APP_SCAN",
                        description = sig.description,
                        recommendation = sig.recommendation
                    )
                )
            }

            // 2. Эвристика по комбинациям разрешений (только при глубоком скане либо всегда — дёшево по ресурсам)
            val requested = pkgInfo.requestedPermissions?.toSet() ?: emptySet()
            if (requested.isNotEmpty()) {
                for (sig in db.permissionComboSignatures) {
                    if (sig.matchList.isNotEmpty() && requested.containsAll(sig.matchList)) {
                        results.add(
                            DetectedThreat(
                                signatureId = sig.id,
                                family = sig.family,
                                severity = sig.severity,
                                targetLabel = appLabel,
                                targetPackage = packageName,
                                targetPath = null,
                                source = "PERMISSION_HEURISTIC",
                                description = sig.description,
                                recommendation = sig.recommendation
                            )
                        )
                    }
                }
            }

            // 3. Источник установки — только при глубоком сканировании (чуть дороже по вызовам)
            if (deep) {
                val installerSig = db.installSourceSignatures.firstOrNull()
                if (installerSig != null) {
                    val installer = try {
                        pm.getInstallSourceInfo(packageName).installingPackageName
                    } catch (e: Exception) { null }

                    if (installer == null && !isSystemApp(pkgInfo)) {
                        results.add(
                            DetectedThreat(
                                signatureId = installerSig.id,
                                family = installerSig.family,
                                severity = installerSig.severity,
                                targetLabel = appLabel,
                                targetPackage = packageName,
                                targetPath = null,
                                source = "APP_SCAN",
                                description = installerSig.description,
                                recommendation = installerSig.recommendation
                            )
                        )
                    }
                }
            }
        }
        return results
    }

    private fun isSystemApp(pkgInfo: android.content.pm.PackageInfo): Boolean {
        val flags = pkgInfo.applicationInfo?.flags ?: 0
        return (flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
    }

    fun installedAppCount(): Int = context.packageManager.getInstalledPackages(0).size
}
