package com.evest.antivirus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.evest.antivirus.R
import com.evest.antivirus.ui.theme.NeonPurple
import com.evest.antivirus.ui.theme.ObsidianSurfaceElevated

/**
 * Аватар-талисман приложения. Картинку положи в
 * app/src/main/res/drawable/avatar_evesteven.png (см. assets_for_user/avatar/README.md)
 */
@Composable
fun MascotBubble(message: String?, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .background(ObsidianSurfaceElevated, RoundedCornerShape(20.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(NeonPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val avatarRes = androidx.compose.ui.res.painterResource(id = mascotResId())
                androidx.compose.foundation.Image(
                    painter = avatarRes,
                    contentDescription = "Евестевень",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(52.dp).clip(CircleShape)
                )
            }
            Text(
                text = message ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

/**
 * Если пользователь ещё не положил свою аватарку в drawable/avatar_evesteven.png,
 * используем встроенную иконку щита, чтобы приложение не падало при сборке.
 */
@androidx.annotation.DrawableRes
private fun mascotResId(): Int = R.drawable.avatar_evesteven
