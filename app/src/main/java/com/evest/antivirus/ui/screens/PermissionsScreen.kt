package com.evest.antivirus.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.evest.antivirus.battery.BatteryOptimizationHelper
import com.evest.antivirus.permissions.PermissionManager
import com.evest.antivirus.ui.theme.EmberOrangeSoft
import com.evest.antivirus.ui.theme.NeonPurple
import com.evest.antivirus.ui.theme.ObsidianBlack
import com.evest.antivirus.ui.theme.ObsidianSurface

@Composable
fun PermissionsScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val permissions = remember { PermissionManager.runtimePermissionsToRequest() }
    var grantedCount by remember { mutableStateOf(0) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedCount++ }

    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .padding(20.dp)
    ) {
        Text(
            "Прежде чем начать защиту",
            style = MaterialTheme.typography.headlineMedium,
            color = EmberOrangeSoft
        )
        Text(
            "Evest Antivirus запрашивает только то, что реально нужно для работы. Ниже — честное объяснение каждого пункта.",
            style = MaterialTheme.typography.bodyMedium,
            color = EmberOrangeSoft.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(permissions) { perm ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianSurface, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(perm.title, style = MaterialTheme.typography.titleMedium, color = NeonPurple)
                    Text(
                        perm.reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmberOrangeSoft,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianSurface, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text("Работа в фоне без ограничений", style = MaterialTheme.typography.titleMedium, color = NeonPurple)
                    Text(
                        "Чтобы плановые проверки не отключались системой при экономии заряда, приложение попросит исключение из оптимизации батареи. Это стандартный диалог Android, ты всегда можешь отказать или отключить это позже в настройках.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmberOrangeSoft,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                val toRequest = permissions.map { it.manifestPermission }.filter {
                    !PermissionManager.isGranted(context, it)
                }
                if (toRequest.isNotEmpty()) {
                    launcher.launch(toRequest.toTypedArray())
                }
                if (!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)) {
                    batteryLauncher.launch(BatteryOptimizationHelper.buildRequestIntent(context))
                }
                onDone()
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Продолжить", color = ObsidianBlack)
        }
    }
}
