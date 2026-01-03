package org.nihongo.mochi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.levels.LevelDefinition
import org.nihongo.mochi.domain.statistics.StatisticsType

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository
) : ViewModel() {

    data class HomeUiState(
        val availableLevels: List<LevelDefinition> = emptyList(),
        val selectedLevelId: String = "",
        val isRecognitionEnabled: Boolean = true,
        val isReadingEnabled: Boolean = true,
        val isWritingEnabled: Boolean = true
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

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
            
            // Load saved selection from settings
            val savedSelection = settingsRepository.getSelectedLevel()

            _uiState.update { currentState ->
                // First try to use saved selection from settings
                // If invalid or empty, fallback to current state selection or first level
                val selectionToUse = if (levels.any { it.id == savedSelection }) {
                    savedSelection
                } else if (levels.any { it.id == currentState.selectedLevelId }) {
                    currentState.selectedLevelId
                } else {
                    levels.firstOrNull()?.id ?: ""
                }
                
                // Ensure consistency: If we had to change selection, save it
                if (selectionToUse != savedSelection && selectionToUse.isNotEmpty()) {
                    settingsRepository.setSelectedLevel(selectionToUse)
                }

                // Calculate enabled activities for the selected level
                val selectedLevel = levels.find { it.id == selectionToUse }
                val recognitionEnabled = selectedLevel?.activities?.get(StatisticsType.RECOGNITION)?.enabled == true
                val readingEnabled = selectedLevel?.activities?.get(StatisticsType.READING)?.enabled == true
                val writingEnabled = selectedLevel?.activities?.get(StatisticsType.WRITING)?.enabled == true

                currentState.copy(
                    availableLevels = levels,
                    selectedLevelId = selectionToUse,
                    isRecognitionEnabled = recognitionEnabled,
                    isReadingEnabled = readingEnabled,
                    isWritingEnabled = writingEnabled
                )
            }
        }
    }
    
    fun onLevelSelected(levelId: String) {
        settingsRepository.setSelectedLevel(levelId)
        
        // When level changes, update enabled states immediately
        val selectedLevel = _uiState.value.availableLevels.find { it.id == levelId }
        val recognitionEnabled = selectedLevel?.activities?.get(StatisticsType.RECOGNITION)?.enabled == true
        val readingEnabled = selectedLevel?.activities?.get(StatisticsType.READING)?.enabled == true
        val writingEnabled = selectedLevel?.activities?.get(StatisticsType.WRITING)?.enabled == true

        _uiState.update { 
            it.copy(
                selectedLevelId = levelId,
                isRecognitionEnabled = recognitionEnabled,
                isReadingEnabled = readingEnabled,
                isWritingEnabled = writingEnabled
            ) 
        }
    }
}
