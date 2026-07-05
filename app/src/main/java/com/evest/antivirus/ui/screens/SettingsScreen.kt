package com.evest.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evest.antivirus.battery.BatteryOptimizationHelper
import com.evest.antivirus.data.SettingsRepository
import com.evest.antivirus.ui.theme.EmberOrangeSoft
import com.evest.antivirus.ui.theme.NeonPurple
import com.evest.antivirus.ui.theme.ObsidianBlack
import com.evest.antivirus.ui.theme.ObsidianSurface
import com.evest.antivirus.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var intervalText by remember(state.intervalMinutes) { mutableStateOf(state.intervalMinutes.toString()) }
    var dbUrlText by remember(state.updateDbUrl) { mutableStateOf(state.updateDbUrl) }

    Column(modifier = Modifier.fillMaxSize().background(ObsidianBlack).padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Настройки", style = MaterialTheme.typography.headlineMedium, color = EmberOrangeSoft)
            OutlinedButton(onClick = onBack) { Text("Назад", color = NeonPurple) }
        }

        SettingsCard(title = "Автоматическая проверка") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Включена", color = EmberOrangeSoft)
                Switch(
                    checked = state.autoScanEnabled,
                    onCheckedChange = { viewModel.setAutoScanEnabled(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = NeonPurple)
                )
            }
            Text(
                "Минимальный интервал в Android для фоновых проверок — 15 минут (это ограничение системы WorkManager, не приложения).",
                color = EmberOrangeSoft.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { intervalText = it.filter { c -> c.isDigit() } },
                    label = { Text("Интервал, мин") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = state.autoScanEnabled,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { intervalText.toIntOrNull()?.let { viewModel.setIntervalMinutes(it) } },
                    enabled = state.autoScanEnabled,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    modifier = Modifier.padding(start = 8.dp)
                ) { Text("Сохранить", color = ObsidianBlack) }
            }
        }

        SettingsCard(title = "Работа в фоне") {
            Text(
                "Чтобы автопроверка не отключалась при экономии заряда, разреши приложению работать в фоне без ограничений.",
                color = EmberOrangeSoft.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = { context.startActivity(BatteryOptimizationHelper.buildRequestIntent(context)) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                modifier = Modifier.padding(top = 10.dp).fillMaxWidth()
            ) { Text("Отключить оптимизацию батареи", color = ObsidianBlack) }
        }

        SettingsCard(title = "Обновление базы угроз") {
            Text(
                "Укажи ссылку на JSON с сигнатурами в том же формате, что и signatures.json (например, на свой репозиторий на GitHub).",
                color = EmberOrangeSoft.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = dbUrlText,
                onValueChange = { dbUrlText = it },
                label = { Text("URL базы сигнатур") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = { viewModel.updateThreatDatabase(dbUrlText) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                enabled = dbUrlText.isNotBlank()
            ) { Text("Обновить базу", color = ObsidianBlack) }

            message?.let {
                Text(it, color = NeonPurple, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(ObsidianSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(title, color = NeonPurple, style = MaterialTheme.typography.titleMedium)
        Column(modifier = Modifier.padding(top = 10.dp)) { content() }
    }
}
