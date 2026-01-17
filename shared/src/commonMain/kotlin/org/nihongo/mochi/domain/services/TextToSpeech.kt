package org.nihongo.mochi.domain.services

import kotlinx.coroutines.flow.StateFlow

enum class VoiceGender { MALE, FEMALE }

data class VoiceConfig(
    val pitch: Float = 1.0f,
    val rate: Float = 1.0f,
    val gender: VoiceGender = VoiceGender.FEMALE,
    val voiceId: String? = null
)

interface TextToSpeech {
    fun speak(text: String, language: String = "ja", config: VoiceConfig = VoiceConfig())
    fun stop()
    fun release()
    /**
     * Observable flow of available voice IDs for the current engine.
     */
    val availableVoices: StateFlow<List<String>>
}
