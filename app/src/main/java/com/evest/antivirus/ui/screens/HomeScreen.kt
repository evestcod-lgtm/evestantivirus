package com.evest.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evest.antivirus.ui.components.MascotBubble
import com.evest.antivirus.ui.components.StatusOrb
import com.evest.antivirus.ui.theme.DangerRed
import com.evest.antivirus.ui.theme.EmberOrangeSoft
import com.evest.antivirus.ui.theme.NeonPurple
import com.evest.antivirus.ui.theme.ObsidianBlack
import com.evest.antivirus.ui.viewmodel.HomeViewModel
import com.evest.antivirus.ui.viewmodel.ProtectionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenScan: () -> Unit,
    onOpenThreats: () -> Unit,
    onOpenProtectionLog: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Evest Antivirus",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EmberOrangeSoft,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(onClick = onOpenSettings) {
                    Text("Настройки", color = NeonPurple)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusOrb(status = state.status)
                Text(
                    text = statusLabel(state.status),
                    style = MaterialTheme.typography.titleMedium,
                    color = EmberOrangeSoft,
                    modifier = Modifier.padding(top = 20.dp)
                )
                if (state.activeThreatCount > 0) {
                    Text(
                        "Активных угроз: ${state.activeThreatCount}",
                        color = DangerRed,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                if (state.lastScanTimestamp > 0) {
                    val formatted = remember(state.lastScanTimestamp) {
                        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            .format(Date(state.lastScanTimestamp))
                    }
                    Text(
                        "Последняя проверка: $formatted",
                        color = EmberOrangeSoft.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Button(
                onClick = { viewModel.runManualScan(); onOpenScan() },
                enabled = !state.isScanning,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(
                    if (state.isScanning) "Идёт проверка..." else "Проверить сейчас",
                    color = ObsidianBlack,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenThreats,
                    modifier = Modifier.weight(1f)
                ) { Text("Журнал угроз", color = NeonPurple) }

                OutlinedButton(
                    onClick = onOpenProtectionLog,
                    modifier = Modifier.weight(1f)
                ) { Text("Журнал защиты", color = NeonPurple) }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            MascotBubble(message = state.mascotMessage, onDismiss = viewModel::dismissMascot)
        }
    }
}

private fun statusLabel(status: ProtectionStatus): String = when (status) {
    ProtectionStatus.SAFE -> "Устройство защищено"
    ProtectionStatus.WARNING -> "Обнаружены подозрительные объекты"
    ProtectionStatus.DANGER -> "Обнаружена серьёзная угроза"
    ProtectionStatus.SCANNING -> "Идёт проверка устройства..."
    ProtectionStatus.UNKNOWN -> "Проверка ещё не выполнялась"
}
