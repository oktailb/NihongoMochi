package org.nihongo.mochi.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.services.VoiceGender
import org.nihongo.mochi.domain.services.TextToSpeech
import org.nihongo.mochi.domain.services.VoiceConfig
import org.nihongo.mochi.domain.services.LanguagePackManager
import org.nihongo.mochi.domain.services.DownloadStatus

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository,
    private val tts: TextToSpeech,
    private val languagePackManager: LanguagePackManager
) : ViewModel() {

    data class SettingsUiState(
        val currentLocaleCode: String = "",
        val pronunciation: String = "Roman",
        val addWrongAnswers: Boolean = false,
        val removeGoodAnswers: Boolean = false,
        val textSize: Float = 1.0f,
        val animationSpeed: Float = 1.0f,
        val isDarkMode: Boolean = false,
        val currentMode: String = "JLPT",
        val availableModes: List<String> = emptyList(),
        val ttsGender: VoiceGender = VoiceGender.FEMALE,
        val ttsRate: Float = 1.0f,
        val availableVoices: List<String> = emptyList(),
        val selectedVoiceId: String? = null,
        val downloadStatus: DownloadStatus = DownloadStatus.IDLE
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialSettings()
        observeVoices()
        observeLanguagePacks()
    }

    private fun loadInitialSettings() {
        viewModelScope.launch {
            val defs = levelsRepository.loadLevelDefinitions()
            val modes = defs.sections.keys.toList().sorted()
            val currentLocale = settingsRepository.getAppLocale()

            _uiState.update {
                it.copy(
                    currentLocaleCode = currentLocale,
                    pronunciation = settingsRepository.getPronunciation(),
                    addWrongAnswers = settingsRepository.shouldAddWrongAnswers(),
                    removeGoodAnswers = settingsRepository.shouldRemoveGoodAnswers(),
                    textSize = settingsRepository.getTextSize(),
                    animationSpeed = settingsRepository.getAnimationSpeed(),
                    isDarkMode = settingsRepository.getTheme() == "dark",
                    currentMode = settingsRepository.getMode(),
                    availableModes = modes,
                    ttsGender = settingsRepository.getTtsGender(currentLocale),
                    ttsRate = settingsRepository.getTtsRate(),
                    selectedVoiceId = settingsRepository.getTtsVoiceId(),
                    downloadStatus = if (languagePackManager.isPackDownloaded(currentLocale)) DownloadStatus.SUCCESS else DownloadStatus.IDLE
                )
            }
        }
    }

    private fun observeVoices() {
        viewModelScope.launch {
            tts.availableVoices.collect { voices ->
                val filteredVoices = voices.filter { 
                    it.contains("jab-local") || it.contains("jad-local") 
                }
                _uiState.update { it.copy(availableVoices = filteredVoices) }
            }
        }
    }

    private fun observeLanguagePacks() {
        viewModelScope.launch {
            languagePackManager.status.collect { statusMap ->
                val current = _uiState.value.currentLocaleCode
                if (current.isNotEmpty()) {
                    val status = statusMap[current] ?: if (languagePackManager.isPackDownloaded(current)) DownloadStatus.SUCCESS else DownloadStatus.IDLE
                    _uiState.update { it.copy(downloadStatus = status) }
                }
            }
        }
    }

    fun onLocaleChanged(newLocaleCode: String) {
        if (newLocaleCode == _uiState.value.currentLocaleCode) return
        
        settingsRepository.setAppLocale(newLocaleCode)
        val newGender = settingsRepository.getTtsGender(newLocaleCode)
        
        _uiState.update { 
            it.copy(
                currentLocaleCode = newLocaleCode,
                ttsGender = newGender
            ) 
        }

        // Auto-download language pack if not English
        if (newLocaleCode != "en_GB" && !languagePackManager.isPackDownloaded(newLocaleCode)) {
            viewModelScope.launch {
                languagePackManager.downloadPack(newLocaleCode)
            }
        }
    }

    fun forceUpdateLanguagePack() {
        val current = _uiState.value.currentLocaleCode
        if (current.isNotEmpty() && current != "en_GB") {
            viewModelScope.launch {
                languagePackManager.downloadPack(current)
            }
        }
    }

    fun onPronunciationChanged(newPronunciation: String) {
        settingsRepository.setPronunciation(newPronunciation)
        _uiState.update { it.copy(pronunciation = newPronunciation) }
    }

    fun onAddWrongAnswersChanged(enabled: Boolean) {
        settingsRepository.setAddWrongAnswers(enabled)
        _uiState.update { it.copy(addWrongAnswers = enabled) }
    }

    fun onRemoveGoodAnswersChanged(enabled: Boolean) {
        settingsRepository.setRemoveGoodAnswers(enabled)
        _uiState.update { it.copy(removeGoodAnswers = enabled) }
    }

    fun onTextSizeChanged(newSize: Float) {
        settingsRepository.setTextSize(newSize)
        _uiState.update { it.copy(textSize = newSize) }
    }

    fun onAnimationSpeedChanged(newSpeed: Float) {
        settingsRepository.setAnimationSpeed(newSpeed)
        _uiState.update { it.copy(animationSpeed = newSpeed) }
    }

    fun onThemeChanged(isDark: Boolean) {
        val theme = if (isDark) "dark" else "light"
        settingsRepository.setTheme(theme)
        _uiState.update { it.copy(isDarkMode = isDark) }
    }
    
    fun onModeChanged(newMode: String) {
        settingsRepository.setMode(newMode)
        _uiState.update { it.copy(currentMode = newMode) }
    }

    fun onTtsGenderChanged(gender: VoiceGender) {
        if (_uiState.value.currentLocaleCode.startsWith("ar")) {
            return
        }
        settingsRepository.setTtsGender(gender)
        _uiState.update { it.copy(ttsGender = gender) }
    }

    fun onTtsRateChanged(rate: Float) {
        settingsRepository.setTtsRate(rate)
        _uiState.update { it.copy(ttsRate = rate) }
    }

    fun onTtsVoiceSelected(voiceId: String?) {
        if (_uiState.value.currentLocaleCode.startsWith("ar")) return
        settingsRepository.setTtsVoiceId(voiceId)
        _uiState.update { it.copy(selectedVoiceId = voiceId) }
    }

    fun testSpeak() {
        val config = VoiceConfig(
            rate = _uiState.value.ttsRate,
            gender = _uiState.value.ttsGender,
            voiceId = _uiState.value.selectedVoiceId
        )
        tts.speak("日本語を勉強しています", "ja", config)
    }

    override fun onCleared() {
        super.onCleared()
        tts.stop()
    }
}
