package com.evest.antivirus.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.evest.antivirus.ui.theme.DangerRed
import com.evest.antivirus.ui.theme.NeonPurple
import com.evest.antivirus.ui.theme.SafeGreen
import com.evest.antivirus.ui.theme.WarnYellow
import com.evest.antivirus.ui.viewmodel.ProtectionStatus

@Composable
fun StatusOrb(status: ProtectionStatus, orbSize: Dp = 180.dp) {
    val color: Color = when (status) {
        ProtectionStatus.SAFE -> SafeGreen
        ProtectionStatus.WARNING -> WarnYellow
        ProtectionStatus.DANGER -> DangerRed
        ProtectionStatus.SCANNING -> NeonPurple
        ProtectionStatus.UNKNOWN -> NeonPurple
    }

    val infinite = rememberInfiniteTransition(label = "orb_pulse")
    val pulse: Float by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(orbSize)) {
        Canvas(modifier = Modifier.size(orbSize)) {
            val radiusPx: Float = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.45f * pulse), color.copy(alpha = 0f)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = radiusPx * 1.1f
                )
            )
            drawCircle(
                color = color.copy(alpha = 0.9f),
                radius = radiusPx * 0.55f * pulse
            )
        }
        Icon(
            imageVector = when (status) {
                ProtectionStatus.SAFE -> Icons.Filled.GppGood
                ProtectionStatus.WARNING -> Icons.Filled.GppMaybe
                ProtectionStatus.DANGER -> Icons.Filled.Warning
                ProtectionStatus.SCANNING -> Icons.Filled.Sync
                ProtectionStatus.UNKNOWN -> Icons.Filled.GppMaybe
            },
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(orbSize / 2.6f)
        )
    }
}
