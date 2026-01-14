package org.nihongo.mochi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.levels.LevelDefinition
import org.nihongo.mochi.domain.levels.ActivityConfig
import org.nihongo.mochi.domain.statistics.StatisticsType

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    data class HomeUiState(
        val availableLevels: List<LevelDefinition> = emptyList(),
        val selectedLevelId: String = "",
        val isRecognitionEnabled: Boolean = true,
        val isReadingEnabled: Boolean = true,
        val isWritingEnabled: Boolean = true,
        val isGrammarEnabled: Boolean = true,
        val recognitionDataFile: String? = null,
        val readingDataFile: String? = null,
        val writingDataFile: String? = null,
        val grammarDataFile: String? = null
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val REVISION_LEVEL_ID = "user_custom_list"

    init {
        loadLevelsForCurrentMode()
    }

    fun onResume() {
        loadLevelsForCurrentMode()
    }

    private fun loadLevelsForCurrentMode() {
        viewModelScope.launch {
            val defs = levelsRepository.loadLevelDefinitions()
            val currentMode = settingsRepository.getMode() // e.g., "JLPT", "School"
            
            val sectionKey = defs.sections.entries.find { 
                it.value.name.equals("section_${currentMode.lowercase()}", ignoreCase = true) ||
                it.key.equals(currentMode, ignoreCase = true)
            }?.key ?: "jlpt" // default to jlpt if not found

            val targetSection = defs.sections[sectionKey]
            val levels = targetSection?.levels?.toMutableList() ?: mutableListOf()
            
            // Add dependencies
            if (targetSection != null) {
                val dependencies = defs.sections.values.filter { 
                    it.prerequisiteFor.contains(sectionKey) 
                }.flatMap { it.levels }
                
                levels.addAll(0, dependencies)
            }

            // Add dynamic "Revision" level at the end
            val revisionLevel = LevelDefinition(
                id = REVISION_LEVEL_ID,
                name = "Revisions",
                activities = mapOf(
                    StatisticsType.RECOGNITION to ActivityConfig(enabled = true, dataFile = REVISION_LEVEL_ID),
                    StatisticsType.READING to ActivityConfig(enabled = true, dataFile = REVISION_LEVEL_ID),
                    StatisticsType.WRITING to ActivityConfig(enabled = true, dataFile = REVISION_LEVEL_ID),
                    StatisticsType.GRAMMAR to ActivityConfig(enabled = true, dataFile = REVISION_LEVEL_ID)
                )
            )
            levels.add(revisionLevel)
            
            // Load saved selection from settings
            val savedSelection = settingsRepository.getSelectedLevel()

            val selectionToUse = if (levels.any { it.id == savedSelection }) {
                savedSelection
            } else if (levels.any { it.id == _uiState.value.selectedLevelId }) {
                _uiState.value.selectedLevelId
            } else {
                levels.firstOrNull()?.id ?: ""
            }
            
            if (selectionToUse != savedSelection && selectionToUse.isNotEmpty()) {
                settingsRepository.setSelectedLevel(selectionToUse)
            }

            updateStateForLevel(levels, selectionToUse)
        }
    }
    
    fun onLevelSelected(levelId: String) {
        settingsRepository.setSelectedLevel(levelId)
        updateStateForLevel(_uiState.value.availableLevels, levelId)
    }

    private fun updateStateForLevel(levels: List<LevelDefinition>, levelId: String) {
        val selectedLevel = levels.find { it.id == levelId }
        
        var isRecognitionEnabled = selectedLevel?.activities?.get(StatisticsType.RECOGNITION)?.enabled == true
        var isReadingEnabled = selectedLevel?.activities?.get(StatisticsType.READING)?.enabled == true
        var isWritingEnabled = selectedLevel?.activities?.get(StatisticsType.WRITING)?.enabled == true
        var isGrammarEnabled = selectedLevel?.activities?.get(StatisticsType.GRAMMAR)?.enabled == true

        // For "Revision" level, we further check if the lists are actually not empty
        if (levelId == REVISION_LEVEL_ID) {
            val recEmpty = scoreRepository.getListItems(ScoreManager.RECOGNITION_LIST).isEmpty()
            val readEmpty = scoreRepository.getListItems(ScoreManager.READING_LIST).isEmpty()
            val writeEmpty = scoreRepository.getListItems(ScoreManager.WRITING_LIST).isEmpty()
            val grammarEmpty = scoreRepository.getAllScores(ScoreManager.ScoreType.GRAMMAR).isEmpty()
            
            isRecognitionEnabled = isRecognitionEnabled && !recEmpty
            isReadingEnabled = isReadingEnabled && !readEmpty
            isWritingEnabled = isWritingEnabled && !writeEmpty
            isGrammarEnabled = isGrammarEnabled && !grammarEmpty
        }

        val recognitionConfig = selectedLevel?.activities?.get(StatisticsType.RECOGNITION)
        val readingConfig = selectedLevel?.activities?.get(StatisticsType.READING)
        val writingConfig = selectedLevel?.activities?.get(StatisticsType.WRITING)
        val grammarConfig = selectedLevel?.activities?.get(StatisticsType.GRAMMAR)

        _uiState.update { 
            it.copy(
                availableLevels = levels,
                selectedLevelId = levelId,
                isRecognitionEnabled = isRecognitionEnabled,
                isReadingEnabled = isReadingEnabled,
                isWritingEnabled = isWritingEnabled,
                isGrammarEnabled = isGrammarEnabled,
                recognitionDataFile = recognitionConfig?.dataFile,
                readingDataFile = readingConfig?.dataFile,
                writingDataFile = writingConfig?.dataFile,
                grammarDataFile = grammarConfig?.dataFile
            ) 
        }
    }
}
