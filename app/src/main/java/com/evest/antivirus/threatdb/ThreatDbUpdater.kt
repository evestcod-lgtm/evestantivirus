package com.evest.antivirus.threatdb

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Обновление локальной базы сигнатур.
 *
 * Приложение НЕ подключено к какому-то конкретному чужому серверу "из коробки" —
 * вместо этого пользователь/владелец приложения может указать в настройках
 * URL на JSON в том же формате, что и signatures.json (например, свой репозиторий
 * на GitHub с обновлениями базы). Это честно и расширяемо, без скрытых серверов.
 *
 * Дополнительно поддержана опциональная проверка хэша файла через публичный
 * VirusTotal API v3 (https://developer.virustotal.com/reference/file-info),
 * если пользователь сам укажет свой API-ключ в настройках. Без ключа эта функция
 * просто не используется — никакого фейкового "облака" не показывается.
 */
class ThreatDbUpdater(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    sealed class UpdateResult {
        data class Success(val newVersion: Int, val signatureCount: Int) : UpdateResult()
        data class Failure(val reason: String) : UpdateResult()
    }

    fun updateFromUrl(url: String): UpdateResult {
        return try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return UpdateResult.Failure("Сервер вернул код ${response.code}")
                }
                val body = response.body?.string() ?: return UpdateResult.Failure("Пустой ответ")
                val db = ThreatDatabase.parse(body) // валидируем формат до сохранения

                val dir = File(context.filesDir, "threat_db")
                if (!dir.exists()) dir.mkdirs()
                File(dir, "signatures.json").writeText(body)

                UpdateResult.Success(db.version, db.signatures.size)
            }
        } catch (e: Exception) {
            Log.e("ThreatDbUpdater", "Ошибка обновления базы угроз", e)
            UpdateResult.Failure(e.message ?: "Неизвестная ошибка сети")
        }
    }

    /**
     * Опциональная проверка хэша файла через VirusTotal API (нужен пользовательский ключ).
     * Возвращает null, если ключ не задан или запрос не удался — вызывающий код должен
     * в этом случае полагаться только на локальную базу, не выдумывая результат.
     */
    fun checkHashOnline(sha256: String, apiKey: String?): Boolean? {
        if (apiKey.isNullOrBlank()) return null
        return try {
            val request = Request.Builder()
                .url("https://www.virustotal.com/api/v3/files/$sha256")
                .addHeader("x-apikey", apiKey)
                .build()
            client.newCall(request).execute().use { response ->
                when (response.code) {
                    200 -> {
                        val body = response.body?.string() ?: return null
                        val malicious = Regex("\"malicious\"\\s*:\\s*(\\d+)")
                            .find(body)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                        malicious > 0
                    }
                    404 -> false // файл неизвестен базе — не считаем это угрозой автоматически
                    else -> null
                }
            }
        } catch (e: Exception) {
            Log.e("ThreatDbUpdater", "VirusTotal lookup failed", e)
            null
        }
    }
}
