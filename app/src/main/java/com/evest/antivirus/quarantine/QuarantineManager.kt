package com.evest.antivirus.quarantine

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.evest.antivirus.data.AppDatabase
import com.evest.antivirus.data.ThreatEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Честная реализация "лечения" в рамках того, что Android разрешает сторонним приложениям
 * без root:
 *
 *  - Для файла (например, APK в Downloads): реально перемещаем файл в приватную
 *    песочницу приложения (недоступную другим приложениям) — это настоящий карантин.
 *  - Для установленного приложения: Android не позволяет одному приложению удалить
 *    другое без подтверждения пользователя. Поэтому мы формируем системный Intent
 *    ACTION_DELETE_PACKAGE, который откроет стандартный диалог удаления. Мы не
 *    притворяемся, что удалили что-то в тихую.
 */
class QuarantineManager(private val context: Context) {

    private val quarantineDir: File
        get() = File(context.filesDir, "quarantine").apply { if (!exists()) mkdirs() }

    sealed class ActionResult {
        object Success : ActionResult()
        data class RequiresUserConfirmation(val intent: Intent) : ActionResult()
        data class Failure(val reason: String) : ActionResult()
    }

    suspend fun quarantineFile(threat: ThreatEntity): ActionResult = withContext(Dispatchers.IO) {
        val path = threat.targetPath ?: return@withContext ActionResult.Failure("Путь к файлу не найден")
        val source = File(path)
        if (!source.exists()) return@withContext ActionResult.Failure("Файл уже не существует")

        return@withContext try {
            val dest = File(quarantineDir, "${System.currentTimeMillis()}_${source.name}")
            source.copyTo(dest, overwrite = true)
            source.delete()
            markStatus(threat, "QUARANTINED")
            ActionResult.Success
        } catch (e: Exception) {
            ActionResult.Failure(e.message ?: "Не удалось поместить файл в карантин")
        }
    }

    /**
     * Возвращает Intent для запуска системного диалога удаления приложения.
     * Активность должна вызвать startActivity(intent) — сам удалить приложение
     * в обход пользователя невозможно, и приложение честно об этом сообщает в UI.
     */
    fun buildUninstallIntent(packageName: String): Intent {
        return Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
    }

    suspend fun markStatus(threat: ThreatEntity, status: String) = withContext(Dispatchers.IO) {
        val dao = AppDatabase.get(context).threatDao()
        dao.update(threat.copy(status = status))
    }

    fun listQuarantinedFiles(): List<File> = quarantineDir.listFiles()?.toList() ?: emptyList()

    fun deleteFromQuarantinePermanently(file: File): Boolean = file.delete()
}
