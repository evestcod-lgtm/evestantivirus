package com.evest.antivirus.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.evest.antivirus.MainActivity
import com.evest.antivirus.R

object NotificationHelper {
    const val CHANNEL_SCAN = "evest_scan_results"
    const val CHANNEL_THREATS = "evest_threats"
    const val CHANNEL_STATUS = "evest_status"

    private const val NOTIF_ID_SCAN = 1001
    private const val NOTIF_ID_THREAT = 1002

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SCAN, "Результаты сканирования", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Уведомления о завершении проверок устройства"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_THREATS, "Обнаруженные угрозы", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Важные уведомления об угрозах, требующих внимания"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_STATUS, "Статус защиты", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Фоновый статус автоматической защиты"
            }
        )
    }

    private fun openAppIntent(context: Context, route: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", route)
        }
        return PendingIntent.getActivity(
            context, route.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showScanCleanResult(context: Context, itemsScanned: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SCAN)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle("Телефон проверен. Угроз обнаружено: 0")
            .setContentText("Проверено объектов: $itemsScanned. Устройство защищено.")
            .setContentIntent(openAppIntent(context, "home"))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIF_ID_SCAN, notification)
    }

    fun showThreatsFound(context: Context, count: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_THREATS)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Обнаружены угрозы: $count")
            .setContentText("Нажмите, чтобы просмотреть журнал защиты и принять меры")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppIntent(context, "threats"))
            .addAction(0, "Просмотреть журнал защиты", openAppIntent(context, "protection_log"))
            .addAction(0, "Удалить угрозы", openAppIntent(context, "threats"))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIF_ID_THREAT, notification)
    }

    fun showCleanupDone(context: Context, resolvedCount: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SCAN)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle("Очистка выполнена")
            .setContentText("Обработано угроз: $resolvedCount")
            .setContentIntent(openAppIntent(context, "protection_log"))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIF_ID_SCAN + 1, notification)
    }

    fun showUpcomingScan(context: Context, minutesFromNow: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle("Скоро плановая проверка")
            .setContentText("Автоматическое сканирование запустится через $minutesFromNow мин.")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIF_ID_SCAN + 2, notification)
    }
}
