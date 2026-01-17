package org.nihongo.mochi.domain.settings

import com.russhwolf.settings.Settings
import org.nihongo.mochi.domain.services.VoiceGender
import org.nihongo.mochi.settings.*

class SettingsRepository(private val settings: Settings) {

    fun getPronunciation(): String = settings.getString(PRONUNCIATION_PREF_KEY, "Hiragana")
    fun setPronunciation(value: String) = settings.putString(PRONUNCIATION_PREF_KEY, value)

    fun getAnimationSpeed(): Float = settings.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
    fun setAnimationSpeed(value: Float) = settings.putFloat(ANIMATION_SPEED_PREF_KEY, value)

    fun getTextSize(): Float = settings.getFloat(TEXT_SIZE_PREF_KEY, 1.0f)
    fun setTextSize(value: Float) = settings.putFloat(TEXT_SIZE_PREF_KEY, value)

    fun shouldAddWrongAnswers(): Boolean = settings.getBoolean(ADD_WRONG_ANSWERS_PREF_KEY, true)
    fun setAddWrongAnswers(value: Boolean) = settings.putBoolean(ADD_WRONG_ANSWERS_PREF_KEY, value)

    fun shouldRemoveGoodAnswers(): Boolean = settings.getBoolean(REMOVE_GOOD_ANSWERS_PREF_KEY, true)
    fun setRemoveGoodAnswers(value: Boolean) = settings.putBoolean(REMOVE_GOOD_ANSWERS_PREF_KEY, value)
    
    fun getTheme(): String = settings.getString(THEME_PREF_KEY, "light")
    fun setTheme(value: String) = settings.putString(THEME_PREF_KEY, value)
    
    fun getAppLocale(): String = settings.getString(APP_LOCALE_PREF_KEY, "en_GB")
    fun setAppLocale(value: String) = settings.putString(APP_LOCALE_PREF_KEY, value)

    fun getMode(): String = settings.getString(MODE_PREF_KEY, "JLPT")
    fun setMode(value: String) = settings.putString(MODE_PREF_KEY, value)

    fun getSelectedLevel(): String = settings.getString(SELECTED_LEVEL_PREF_KEY, "")
    fun setSelectedLevel(value: String) = settings.putString(SELECTED_LEVEL_PREF_KEY, value)

    // TTS Settings
    fun getTtsGender(locale: String): VoiceGender {
        val defaultGender = if (locale.startsWith("ar")) VoiceGender.MALE.name else VoiceGender.FEMALE.name
        val genderName = settings.getString(TTS_GENDER_PREF_KEY, defaultGender)
        return try {
            VoiceGender.valueOf(genderName)
        } catch (e: Exception) {
            VoiceGender.valueOf(defaultGender)
        }
    }
    
    fun setTtsGender(gender: VoiceGender) = settings.putString(TTS_GENDER_PREF_KEY, gender.name)

    fun getTtsRate(): Float = settings.getFloat(TTS_RATE_PREF_KEY, 1.0f)
    fun setTtsRate(rate: Float) = settings.putFloat(TTS_RATE_PREF_KEY, rate)

    fun getTtsVoiceId(): String? = settings.getStringOrNull(TTS_VOICE_ID_PREF_KEY)
    fun setTtsVoiceId(voiceId: String?) = settings.putString(TTS_VOICE_ID_PREF_KEY, voiceId ?: "")
}
