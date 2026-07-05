package com.evest.antivirus.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

data class AppPermission(
    val manifestPermission: String,
    val title: String,
    val reason: String,
    val required: Boolean
)

object PermissionManager {

    fun runtimePermissionsToRequest(): List<AppPermission> {
        val list = mutableListOf<AppPermission>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(
                AppPermission(
                    Manifest.permission.POST_NOTIFICATIONS,
                    "Уведомления",
                    "Чтобы сообщать о результатах сканирования и найденных угрозах вовремя.",
                    required = true
                )
            )
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            list.add(
                AppPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    "Доступ к файлам",
                    "Чтобы проверять APK-файлы в папке Загрузки на вредоносные признаки.",
                    required = false
                )
            )
        } else {
            list.add(
                AppPermission(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    "Доступ к медиафайлам",
                    "Нужно для проверки загруженных файлов на устройстве.",
                    required = false
                )
            )
        }

        return list
    }

    fun isGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
