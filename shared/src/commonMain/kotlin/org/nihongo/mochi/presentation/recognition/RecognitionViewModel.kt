package org.nihongo.mochi.presentation.recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.models.LevelInfoState
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.statistics.StatisticsType

class RecognitionViewModel(
    private val levelContentProvider: LevelContentProvider,
    private val statisticsEngine: StatisticsEngine
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<List<RecognitionCategory>>(emptyList())
    val categoriesState: StateFlow<List<RecognitionCategory>> = _categoriesState.asStateFlow()

    // Keep this for backward compatibility if needed, but it's better to migrate UI to categories
    private val _levelInfosState = MutableStateFlow<List<LevelInfoState>>(emptyList())
    val levelInfosState: StateFlow<List<LevelInfoState>> = _levelInfosState.asStateFlow()

    fun refreshData(nameProvider: (String) -> String) {
        viewModelScope.launch {
            statisticsEngine.loadLevelDefinitions()
            val allStats = statisticsEngine.getAllStatistics()
            
            // Filter only RECOGNITION activities
            val recogStats = allStats.filter { it.type == StatisticsType.RECOGNITION }
            
            val categories = recogStats
                .groupBy { it.category }
                .map { (categoryKey, stats) ->
                    val levels = stats
                        .sortedBy { it.sortOrder }
                        .map { stat ->
                            LevelInfoState(
                                levelKey = stat.xmlName,
                                displayName = nameProvider(stat.title), // stat.title is the resource key (e.g. level_n5)
                                percentage = stat.percentage
                            )
                        }
                    RecognitionCategory(
                        name = categoryKey, // categoryKey is the resource key (e.g. section_jlpt)
                        levels = levels
                    )
                }
            
            _categoriesState.update { categories }
            
            // Flatten for legacy views if any
            _levelInfosState.update { categories.flatMap { it.levels } }
        }
    }
}
