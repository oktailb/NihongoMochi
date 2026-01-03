package org.nihongo.mochi.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.settings.SettingsRepository

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository
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
        val availableModes: List<String> = emptyList()
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialSettings()
    }

    private fun loadInitialSettings() {
        viewModelScope.launch {
            // Load dynamic modes from levels.json via repository
            val defs = levelsRepository.loadLevelDefinitions()
            
            // Use section IDs as modes (e.g. "jlpt", "school", "challenge")
            // Filtering out "fundamentals" if it shouldn't be a main mode, 
            // but based on levels.json, fundamentals is a section. 
            // However, usually it's a dependency. 
            // If the user wants to select it explicitly, we include it.
            // Sorting to keep order consistent.
            val modes = defs.sections.keys.toList().sorted()

            _uiState.update {
                it.copy(
                    currentLocaleCode = settingsRepository.getAppLocale(),
                    pronunciation = settingsRepository.getPronunciation(),
                    addWrongAnswers = settingsRepository.shouldAddWrongAnswers(),
                    removeGoodAnswers = settingsRepository.shouldRemoveGoodAnswers(),
                    textSize = settingsRepository.getTextSize(),
                    animationSpeed = settingsRepository.getAnimationSpeed(),
                    isDarkMode = settingsRepository.getTheme() == "dark",
                    currentMode = settingsRepository.getMode(),
                    availableModes = modes
                )
            }
        }
    }

    fun onLocaleChanged(newLocaleCode: String) {
        if (newLocaleCode == _uiState.value.currentLocaleCode) return
        settingsRepository.setAppLocale(newLocaleCode)
        _uiState.update { it.copy(currentLocaleCode = newLocaleCode) }
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
}
