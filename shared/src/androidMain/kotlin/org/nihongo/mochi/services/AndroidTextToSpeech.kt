package org.nihongo.mochi.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.nihongo.mochi.domain.services.VoiceConfig
import org.nihongo.mochi.domain.services.VoiceGender
import org.nihongo.mochi.domain.services.TextToSpeech as MochiTextToSpeech

class AndroidTextToSpeech(private val context: Context) : MochiTextToSpeech, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false
    private var pendingSpeech: Triple<String, String, VoiceConfig>? = null

    private val _availableVoices = MutableStateFlow<List<String>>(emptyList())
    override val availableVoices: StateFlow<List<String>> = _availableVoices.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.JAPANESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("MochiTTS", "Japanese language is not supported or data is missing")
            } else {
                isInitialized = true
                updateVoicesList()
                pendingSpeech?.let { (text, lang, config) ->
                    speak(text, lang, config)
                    pendingSpeech = null
                }
            }
        } else {
            Log.e("MochiTTS", "Initialization failed")
        }
    }

    private fun updateVoicesList() {
        val voices = tts?.voices?.filter { 
            it.locale.language == Locale.JAPANESE.language 
        }?.map { it.name } ?: emptyList()
        
        Log.d("MochiTTS", "Updating voices list: ${voices.size} voices found")
        _availableVoices.value = voices
    }

    override fun speak(text: String, language: String, config: VoiceConfig) {
        if (!isInitialized) {
            pendingSpeech = Triple(text, language, config)
            return
        }

        tts?.let { engine ->
            val targetLocale = if (language == "ja") Locale.JAPANESE else Locale(language)
            engine.language = targetLocale
            engine.setPitch(config.pitch)
            engine.setSpeechRate(config.rate)

            val voices = engine.voices
            val languageVoices = voices?.filter { 
                it.locale.language == targetLocale.language 
            } ?: emptyList()

            // 1. Try to find by explicit voiceId if provided
            var selectedVoice = if (config.voiceId != null) {
                languageVoices.firstOrNull { it.name == config.voiceId }
            } else null

            // 2. Fallback to gender-based search if no voiceId or not found
            if (selectedVoice == null) {
                selectedVoice = languageVoices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    when (config.gender) {
                        VoiceGender.MALE -> {
                            name.contains("male") || name.contains("low") || 
                            name.contains("-m-") || name.contains("guy") || 
                            name.contains("man")
                        }
                        VoiceGender.FEMALE -> {
                            name.contains("female") || name.contains("high") || 
                            name.contains("-f-") || name.contains("girl") || 
                            name.contains("woman")
                        }
                    }
                }
            }

            // 3. Last fallback to first available language voice
            if (selectedVoice == null) {
                selectedVoice = languageVoices.firstOrNull()
            }

            if (selectedVoice != null) {
                engine.voice = selectedVoice
            }

            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MochiTTS")
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
