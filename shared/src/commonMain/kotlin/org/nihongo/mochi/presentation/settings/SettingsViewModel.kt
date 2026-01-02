package org.nihongo.mochi.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.statistics.StatisticsEngine

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val statisticsEngine: StatisticsEngine
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
            // Load dynamic modes from levels.json
            statisticsEngine.loadLevelDefinitions()
            // We use the keys of the sections map as modes (e.g., "jlpt", "school", "challenge")
            // Or better, use the display names if available, but for internal mode key we might want the ID.
            // The previous hardcoded list was ["JLPT", "School", "Challenge"].
            // levels.json has keys: "fundamentals", "jlpt", "school", "challenge".
            // Let's exclude "fundamentals" if it's not a selectable mode in the old logic, 
            // or include it if it makes sense. The user said "JLPT/SCHOOL, ...".
            
            // StatisticsEngine doesn't expose sections directly publicly in the previous edits, 
            // but we can add a method or just use getAllStatistics().
            // However, getAllStatistics returns flattened levels.
            
            // Let's rely on getAllStatistics().groupBy { it.category }.keys
            // This gives us the display names of sections (e.g. "JLPT", "École Japonaise", "Challenge pour natif").
            // But we need to map back to the keys stored in preferences if they differ.
            // In SettingsRepository, setMode stores a string.
            
            // If the user wants the dropdown to use the categories from levels.json:
            val allStats = statisticsEngine.getAllStatistics()
            val categories = allStats.map { it.category }.distinct().sorted()
            
            // If the list is empty (parsing fail), fallback to defaults?
            val modes = if (categories.isNotEmpty()) categories else listOf("JLPT", "School", "Challenge")

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
