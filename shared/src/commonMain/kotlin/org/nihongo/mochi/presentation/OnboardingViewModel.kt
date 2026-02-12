package org.nihongo.mochi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.services.LanguagePackManager
import org.nihongo.mochi.domain.services.DownloadStatus

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository,
    private val languagePackManager: LanguagePackManager
) : ViewModel() {

    data class OnboardingUiState(
        val isFirstRun: Boolean = false,
        val currentLocaleCode: String = "en_GB",
        val currentMode: String = "JLPT",
        val availableModes: List<String> = emptyList(),
        val downloadStatus: DownloadStatus = DownloadStatus.IDLE,
        val currentStep: Int = 0 // 0: Language, 1: Mode, 2: Level Selector Hint
    )

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val isFirstRun = settingsRepository.isFirstRun()
        if (isFirstRun) {
            viewModelScope.launch {
                val defs = levelsRepository.loadLevelDefinitions()
                val modes = defs.sections.keys.toList().sorted()
                val currentLocale = settingsRepository.getAppLocale()
                
                _uiState.update {
                    it.copy(
                        isFirstRun = true,
                        currentLocaleCode = currentLocale,
                        currentMode = settingsRepository.getMode(),
                        availableModes = modes,
                        downloadStatus = if (languagePackManager.isPackDownloaded(currentLocale)) DownloadStatus.SUCCESS else DownloadStatus.IDLE
                    )
                }
            }
            observeLanguagePacks()
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
        settingsRepository.setAppLocale(newLocaleCode)
        _uiState.update { it.copy(currentLocaleCode = newLocaleCode) }

        if (newLocaleCode != "en_GB" && !languagePackManager.isPackDownloaded(newLocaleCode)) {
            viewModelScope.launch {
                languagePackManager.downloadPack(newLocaleCode)
            }
        }
    }

    fun onModeChanged(newMode: String) {
        settingsRepository.setMode(newMode)
        _uiState.update { it.copy(currentMode = newMode) }
    }

    fun nextStep() {
        val next = _uiState.value.currentStep + 1
        if (next > 2) {
            finishOnboarding()
        } else {
            _uiState.update { it.copy(currentStep = next) }
        }
    }

    fun finishOnboarding() {
        settingsRepository.setFirstRunCompleted()
        _uiState.update { it.copy(isFirstRun = false) }
    }
}
