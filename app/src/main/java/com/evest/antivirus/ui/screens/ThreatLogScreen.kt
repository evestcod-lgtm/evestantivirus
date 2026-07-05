package com.evest.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evest.antivirus.data.ThreatEntity
import com.evest.antivirus.threatdb.Severity
import com.evest.antivirus.ui.theme.CriticalRed
import com.evest.antivirus.ui.theme.DangerRed
import com.evest.antivirus.ui.theme.EmberOrangeSoft
import com.evest.antivirus.ui.theme.NeonPurple
import com.evest.antivirus.ui.theme.ObsidianBlack
import com.evest.antivirus.ui.theme.ObsidianSurface
import com.evest.antivirus.ui.theme.WarnYellow
import com.evest.antivirus.ui.viewmodel.ThreatLogViewModel

@Composable
fun ThreatLogScreen(viewModel: ThreatLogViewModel, onBack: () -> Unit) {
    val threats by viewModel.threats.collectAsStateWithLifecycle()
    val pendingIntent by viewModel.pendingUninstallIntent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(pendingIntent) {
        pendingIntent?.let {
            context.startActivity(it)
            viewModel.clearPendingIntent()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(ObsidianBlack).padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Журнал угроз", style = MaterialTheme.typography.headlineMedium, color = EmberOrangeSoft)
            OutlinedButton(onClick = onBack) { Text("Назад", color = NeonPurple) }
        }

        if (threats.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Угроз ещё не обнаружено", color = EmberOrangeSoft.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(threats, key = { it.id }) { threat ->
                    ThreatCard(
                        threat = threat,
                        onQuarantine = { viewModel.quarantine(threat) },
                        onUninstall = { viewModel.requestUninstall(threat) },
                        onIgnore = { viewModel.ignore(threat) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreatCard(
    threat: ThreatEntity,
    onQuarantine: () -> Unit,
    onUninstall: () -> Unit,
    onIgnore: () -> Unit
) {
    val severityColor = when (Severity.from(threat.severity)) {
        Severity.CRITICAL -> CriticalRed
        Severity.HIGH -> DangerRed
        Severity.MEDIUM -> WarnYellow
        Severity.LOW -> NeonPurple
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(threat.targetLabel, style = MaterialTheme.typography.titleMedium, color = EmberOrangeSoft)
            Text(threat.severity, color = severityColor, style = MaterialTheme.typography.labelLarge)
        }
        Text(
            "Семейство: ${threat.family} · Источник: ${sourceLabel(threat.source)}",
            color = EmberOrangeSoft.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            threat.description,
            color = EmberOrangeSoft.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (threat.status == "NEW") {
            Row(
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (threat.targetPath != null) {
                    Button(
                        onClick = onQuarantine,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.weight(1f)
                    ) { Text("Карантин", color = ObsidianBlack) }
                }
                if (threat.targetPackage != null) {
                    Button(
                        onClick = onUninstall,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier.weight(1f)
                    ) { Text("Удалить", color = ObsidianBlack) }
                }
                OutlinedButton(onClick = onIgnore, modifier = Modifier.weight(1f)) {
                    Text("Игнорировать", color = NeonPurple)
                }
            }
        } else {
            Text(
                "Статус: ${statusLabel(threat.status)}",
                color = EmberOrangeSoft.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

private fun sourceLabel(source: String) = when (source) {
    "APP_SCAN" -> "проверка приложений"
    "FILE_SCAN" -> "проверка файлов"
    "PERMISSION_HEURISTIC" -> "эвристика разрешений"
    else -> source
}

private fun statusLabel(status: String) = when (status) {
    "QUARANTINED" -> "в карантине"
    "REMOVED" -> "удалено"
    "IGNORED" -> "проигнорировано"
    else -> status
}
