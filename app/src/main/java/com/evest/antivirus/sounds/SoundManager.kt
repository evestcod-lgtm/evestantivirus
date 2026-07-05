package com.evest.antivirus.sounds

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.evest.antivirus.R

/**
 * Управляет звуковыми эффектами. Файлы должны лежать в app/src/main/res/raw/
 * со следующими именами (см. README и папку assets_for_user/sounds):
 *   notification.mp3, virus.mp3, error.mp3, scan.mp3, check.mp3
 *
 * Если файл отсутствует на момент сборки — SoundManager не падает,
 * а просто пропускает воспроизведение (см. try/catch и optional resource lookup).
 */
enum class SoundEffect(val rawName: String) {
    NOTIFICATION("notification"),
    VIRUS("virus"),
    ERROR("error"),
    SCAN("scan"),
    CHECK("check")
}

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSounds = mutableMapOf<SoundEffect, Int>()

    fun preload() {
        for (effect in SoundEffect.values()) {
            val resId = context.resources.getIdentifier(effect.rawName, "raw", context.packageName)
            if (resId != 0) {
                loadedSounds[effect] = soundPool.load(context, resId, 1)
            }
        }
    }

    fun play(effect: SoundEffect, volume: Float = 0.8f) {
        val soundId = loadedSounds[effect] ?: return
        soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
