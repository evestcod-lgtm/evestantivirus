package com.evest.antivirus.scan

import android.content.Context
import android.os.Environment
import com.evest.antivirus.threatdb.ThreatDatabase
import com.evest.antivirus.util.HashUtil
import java.io.File

/**
 * Сканирует файлы, доступные приложению без специальных прав (Downloads, кэш самого приложения,
 * файлы, переданные через Storage Access Framework). Считает SHA-256 и сверяет с базой хэшей.
 *
 * Честное ограничение: без MANAGE_EXTERNAL_STORAGE (которое Google Play жёстко ограничивает
 * для не-файловых менеджеров) приложение не может обойти всю файловую систему. Поэтому мы
 * сканируем публичную папку Downloads — это разрешено на API 26-32 через READ_EXTERNAL_STORAGE,
 * и на новых версиях то, что видно через MediaStore/SAF.
 */
class FileScanner(private val context: Context, private val db: ThreatDatabase) {

    fun scanDownloads(): List<DetectedThreat> {
        val results = mutableListOf<DetectedThreat>()
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists() || !downloadsDir.canRead()) return results

        val candidateFiles = downloadsDir.listFiles { f ->
            f.isFile && (f.extension.equals("apk", true) || f.extension.equals("apks", true))
        } ?: return results

        for (file in candidateFiles) {
            val hash = try {
                HashUtil.sha256(file)
            } catch (e: Exception) {
                continue
            }
            db.hashSignatures[hash.lowercase()]?.let { sig ->
                results.add(
                    DetectedThreat(
                        signatureId = sig.id,
                        family = sig.family,
                        severity = sig.severity,
                        targetLabel = file.name,
                        targetPackage = null,
                        targetPath = file.absolutePath,
                        source = "FILE_SCAN",
                        description = sig.description,
                        recommendation = sig.recommendation
                    )
                )
            }
        }
        return results
    }

    fun scannableFileCount(): Int {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return dir.listFiles()?.size ?: 0
    }
}
