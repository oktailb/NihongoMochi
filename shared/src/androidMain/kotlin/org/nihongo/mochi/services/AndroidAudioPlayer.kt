package org.nihongo.mochi.services

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import org.nihongo.mochi.domain.services.AudioPlayer
import java.io.IOException

class AndroidAudioPlayer(private val context: Context) : AudioPlayer {

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
        val soundId = soundMap[resourcePath]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            // Dans un vrai projet KMP avec Compose Resources, le resourcePath 
            // devrait correspondre à un asset ou une ressource brute.
            // Pour simplifier ici, on suppose que resourcePath est le nom de l'asset.
            try {
                val assetDescriptor = context.assets.openFd(resourcePath)
                val newSoundId = soundPool.load(assetDescriptor, 1)
                soundMap[resourcePath] = newSoundId
                
                // On attend que le son soit chargé avant de le jouer la première fois
                soundPool.setOnLoadCompleteListener { _, loadedId, status ->
                    if (status == 0 && loadedId == newSoundId) {
                        soundPool.play(loadedId, 1f, 1f, 1, 0, 1f)
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
