package org.nihongo.mochi.presentation.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.util.LevelContentProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.statistics.StatisticsType
import org.nihongo.mochi.presentation.models.ReadingLevelInfoState

data class ReadingScreenState(
    val categories: List<ReadingCategory> = emptyList(),
    val userListInfo: ReadingLevelInfoState = ReadingLevelInfoState("user_custom_list", "", 0)
)

class ReadingViewModel(
    private val levelContentProvider: LevelContentProvider,
    private val statisticsEngine: StatisticsEngine
) : ViewModel() {

    private val _state = MutableStateFlow(ReadingScreenState())
    val state: StateFlow<ReadingScreenState> = _state.asStateFlow()

    fun refreshData(userListNameProvider: () -> String) {
        viewModelScope.launch {
            // Ensure levels are loaded
            statisticsEngine.loadLevelDefinitions()
            val allStats = statisticsEngine.getAllStatistics()
            
            // Filter only READING activities
            val readingStats = allStats.filter { it.type == StatisticsType.READING }
            
            // Group by category dynamically based on levels.json
            val grouped = readingStats
                .groupBy { it.category }
                .map { (categoryName, stats) ->
                    val levels = stats
                        .sortedBy { it.sortOrder }
                        .map { createLevelState(it.xmlName, it.title, it.percentage) }
                    
                    ReadingCategory(categoryName, levels)
                }
                // Optional: Sort categories if needed, or rely on order in map (usually insertion order in some cases but not guaranteed)
                // For now, let's sort them to ensure consistent order if possible, or maybe sort by a predefined order if we had one.
                // Assuming alphabetical or no specific sort for dynamic categories.
                .sortedBy { it.name }
            
            // User List is special, keep it separate or add as a category?
            // The requirement says "la présentation est esclave de la configuration", but user list is dynamic.
            // Let's keep user list separate in state, but UI can decide where to put it.
            
            val userListState = createLevelState("user_custom_list", userListNameProvider(), 
                calculateUserListPercentage().toInt())

            _state.update {
                it.copy(
                    categories = grouped,
                    userListInfo = userListState
                )
            }
        }
    }
    
    // Helper to create state from pre-calculated data
    private fun createLevelState(levelId: String, displayName: String, percentage: Int): ReadingLevelInfoState {
        return ReadingLevelInfoState(levelId, displayName, percentage)
    }

    private fun calculateUserListPercentage(): Double {
        val scores = ScoreManager.getAllScores(ScoreManager.ScoreType.READING)
        if (scores.isEmpty()) return 0.0

        val totalEncountered = scores.size
        val mastered = scores.count { (_, score) -> (score.successes - score.failures) >= 10 }

        return if (totalEncountered > 0) {
            (mastered.toDouble() / totalEncountered.toDouble()) * 100.0
        } else {
            0.0
        }
    }
}
