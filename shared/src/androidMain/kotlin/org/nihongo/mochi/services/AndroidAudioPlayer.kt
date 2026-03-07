package org.nihongo.mochi.services

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.settings.SettingsRepository
import java.io.IOException

class AndroidAudioPlayer(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : AudioPlayer {

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(audioAttributes)
        .build()

    private val soundMap = mutableMapOf<String, Int>()

    override fun playSound(resourcePath: String) {
        val volume = settingsRepository.getAudioVolume()
        val soundId = soundMap[resourcePath]
        if (soundId != null) {
            soundPool.play(soundId, volume, volume, 1, 0, 1f)
        } else {
            try {
                val assetDescriptor = context.assets.openFd(resourcePath)
                val newSoundId = soundPool.load(assetDescriptor, 1)
                soundMap[resourcePath] = newSoundId
                
                soundPool.setOnLoadCompleteListener { _, loadedId, status ->
                    if (status == 0 && loadedId == newSoundId) {
                        val currentVolume = settingsRepository.getAudioVolume()
                        soundPool.play(loadedId, currentVolume, currentVolume, 1, 0, 1f)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun stopAll() {
        soundMap.values.forEach { soundPool.stop(it) }
    }

    override fun release() {
        soundPool.release()
        soundMap.clear()
    }
}
