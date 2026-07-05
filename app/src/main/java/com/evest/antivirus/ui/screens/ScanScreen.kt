package com.evest.antivirus.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evest.antivirus.sounds.SoundEffect
import com.evest.antivirus.sounds.SoundManager
import com.evest.antivirus.ui.theme.DangerRed
import com.evest.antivirus.ui.theme.EmberOrangeSoft
import com.evest.antivirus.ui.theme.NeonPurple
import com.evest.antivirus.ui.theme.ObsidianBlack
import com.evest.antivirus.ui.theme.SafeGreen
import com.evest.antivirus.ui.viewmodel.ScanViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember

@Composable
fun ScanScreen(viewModel: ScanViewModel, onBack: () -> Unit, onOpenThreats: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context).apply { preload() } }

    LaunchedEffect(state.running) {
        if (state.running) soundManager.play(SoundEffect.SCAN)
    }
    LaunchedEffect(state.report) {
        state.report?.let {
            soundManager.play(if (it.threatCount > 0) SoundEffect.VIRUS else SoundEffect.CHECK)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.running) {
                ScanRadar()
                Text(
                    state.progressLabel,
                    color = EmberOrangeSoft,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else if (state.report != null) {
                val report = state.report!!
                val color = if (report.threatCount == 0) SafeGreen else DangerRed
                Text(
                    if (report.threatCount == 0) "Угроз не найдено" else "Найдено угроз: ${report.threatCount}",
                    color = color,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "Проверено объектов: ${report.itemsScanned}",
                    color = EmberOrangeSoft.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
                if (report.threatCount > 0) {
                    Button(
                        onClick = onOpenThreats,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Просмотреть журнал угроз", color = ObsidianBlack) }
                } else {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Готово", color = ObsidianBlack) }
                }
            }
        }
    }
}

@Composable
private fun ScanRadar() {
    val infinite = rememberInfiniteTransition(label = "radar")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "rotation"
    )
    Canvas(modifier = Modifier.size(220.dp)) {
        val radius = size.minDimension / 2f
        drawCircle(color = NeonPurple.copy(alpha = 0.15f), radius = radius)
        drawCircle(color = NeonPurple.copy(alpha = 0.3f), radius = radius * 0.7f)
        rotate(rotation) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(NeonPurple.copy(alpha = 0f), NeonPurple)
                ),
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = true,
                topLeft = Offset(size.width / 2f - radius, size.height / 2f - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }
    }
}
