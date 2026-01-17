package org.nihongo.mochi.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
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
            // Requirement: Always Japanese voice for this app's main functionality
            val targetLocale = Locale.JAPANESE
            engine.language = targetLocale
            engine.setPitch(config.pitch)
            engine.setSpeechRate(config.rate)

            // Get available Japanese voices
            val japaneseVoices = engine.voices?.filter { 
                it.locale.language == targetLocale.language 
            } ?: emptyList()

            // 1/ Arabe => voix homme japonais dans tout les cas
            // 2/ Pas arabe => le genre choisi dans les settings
            val appLocale = context.resources.configuration.locale
            val isArabic = appLocale.language == "ar"
            val effectiveGender = if (isArabic) VoiceGender.MALE else config.gender

            var selectedVoice: Voice? = null

            // 1. Try explicit voiceId if provided AND NOT in forced Arabic mode
            if (!isArabic && !config.voiceId.isNullOrBlank()) {
                selectedVoice = japaneseVoices.firstOrNull { it.name == config.voiceId }
            }

            // 2. Fallback to gender-based search
            if (selectedVoice == null) {
                selectedVoice = japaneseVoices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    if (effectiveGender == VoiceGender.MALE) {
                        name.contains("male") || name.contains("low") || 
                        name.contains("-m-") || name.contains("-m") || 
                        name.contains("guy") || name.contains("man") || 
                        name.contains("jad-local")
                    } else {
                        name.contains("female") || name.contains("high") || 
                        name.contains("-f-") || name.contains("-f") || 
                        name.contains("girl") || name.contains("woman") || 
                        name.contains("jab-local")
                    }
                }
            }

            // 3. Ultimate fallback to first available Japanese voice
            if (selectedVoice == null) {
                selectedVoice = japaneseVoices.firstOrNull()
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
