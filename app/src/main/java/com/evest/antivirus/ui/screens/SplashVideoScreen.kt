package com.evest.antivirus.ui.screens

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.evest.antivirus.R
import com.evest.antivirus.ui.theme.EmberOrangeSoft
import com.evest.antivirus.ui.theme.ObsidianBlack

/**
 * Экран первого запуска. Проигрывает app/src/main/res/raw/intro_video.mp4 один раз,
 * поверх видео показывает английское сообщение, затем переходит дальше.
 * Если видеофайл ещё не добавлен пользователем — просто показываем сообщение
 * на чёрном фоне 3 секунды, ничего не падает.
 */
@OptIn(UnstableApi::class)
@Composable
fun SplashVideoScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var videoAvailable by remember { mutableStateOf(true) }

    val videoResId = remember {
        context.resources.getIdentifier("intro_video", "raw", context.packageName)
    }

    if (videoResId == 0) {
        // Видео ещё не положено — честно пропускаем через таймер, ничего не ломаем
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2500)
            onFinished()
        }
        Box(
            modifier = Modifier.fillMaxSize().background(ObsidianBlack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResourceSafe(),
                color = EmberOrangeSoft,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse("android.resource://${context.packageName}/$videoResId")))
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        onFinished()
                    }
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = stringResourceSafe(),
                color = EmberOrangeSoft,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun stringResourceSafe(): String =
    androidx.compose.ui.res.stringResource(id = R.string.first_run_message)
