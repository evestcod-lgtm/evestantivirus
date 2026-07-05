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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evest.antivirus.data.ProtectionEvent
import com.evest.antivirus.ui.theme.EmberOrangeSoft
import com.evest.antivirus.ui.theme.NeonPurple
import com.evest.antivirus.ui.theme.ObsidianBlack
import com.evest.antivirus.ui.theme.ObsidianSurface
import com.evest.antivirus.ui.viewmodel.ProtectionLogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProtectionLogScreen(viewModel: ProtectionLogViewModel, onBack: () -> Unit) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val formatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().background(ObsidianBlack).padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Журнал защиты", style = MaterialTheme.typography.headlineMedium, color = EmberOrangeSoft)
            OutlinedButton(onClick = onBack) { Text("Назад", color = NeonPurple) }
        }

        if (events.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("События появятся здесь после первой проверки", color = EmberOrangeSoft.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(events, key = { it.id }) { event -> EventRow(event, formatter) }
            }
        }
    }
}

@Composable
private fun EventRow(event: ProtectionEvent, formatter: SimpleDateFormat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianSurface, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(event.title, color = EmberOrangeSoft, style = MaterialTheme.typography.titleMedium)
            Text(
                formatter.format(Date(event.timestamp)),
                color = EmberOrangeSoft.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            event.details,
            color = EmberOrangeSoft.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            "Статус: ${statusRu(event.status)}",
            color = NeonPurple,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun statusRu(status: String) = when (status) {
    "CLEANED" -> "очищено"
    "REMOVED" -> "удалено"
    "QUARANTINED" -> "в карантине"
    "IGNORED" -> "проигнорировано"
    "ACTION_REQUIRED" -> "требует действия"
    else -> status
}
