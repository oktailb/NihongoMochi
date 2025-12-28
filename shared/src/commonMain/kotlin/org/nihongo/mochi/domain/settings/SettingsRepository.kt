package org.nihongo.mochi.domain.settings

import com.russhwolf.settings.Settings
import org.nihongo.mochi.settings.ADD_WRONG_ANSWERS_PREF_KEY
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.settings.PRONUNCIATION_PREF_KEY
import org.nihongo.mochi.settings.REMOVE_GOOD_ANSWERS_PREF_KEY
import org.nihongo.mochi.settings.TEXT_SIZE_PREF_KEY
import org.nihongo.mochi.settings.THEME_PREF_KEY

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
    
    fun getAppLocale(): String = settings.getString("AppLocale", "en_GB")
    fun setAppLocale(value: String) = settings.putString("AppLocale", value)
}
