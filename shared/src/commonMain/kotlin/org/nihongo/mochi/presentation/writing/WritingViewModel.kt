package org.nihongo.mochi.presentation.writing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.statistics.StatisticsType
import org.nihongo.mochi.presentation.models.WritingLevelInfoState

class WritingViewModel(
    private val statisticsEngine: StatisticsEngine
) : ViewModel() {

    private val _writingCategories = MutableStateFlow<List<WritingCategory>>(emptyList())
    val writingCategories: StateFlow<List<WritingCategory>> = _writingCategories.asStateFlow()

    private val _userListInfo = MutableStateFlow(
        WritingLevelInfoState("user_list", "User List", 0)
    )
    val userListInfo: StateFlow<WritingLevelInfoState> = _userListInfo.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            statisticsEngine.loadLevelDefinitions()
            refreshStatistics()
        }
    }

    fun refreshStatistics() {
        val allStats = statisticsEngine.getAllStatistics()
        
        // Group by Category
        val groupedStats = allStats
            .filter { it.type == StatisticsType.WRITING }
            .groupBy { it.category }
            
        // Map to UI model
        val categories = groupedStats.map { (categoryName, levels) ->
            WritingCategory(
                name = categoryName,
                levels = levels.sortedBy { it.sortOrder }.map { level ->
                    WritingLevelInfoState(
                        levelKey = level.xmlName,
                        displayName = level.title,
                        percentage = level.percentage
                    )
                }
            )
        }
        
        // Handle User List separately (if present in stats or manually)
        val userListStat = allStats.find { it.type == StatisticsType.WRITING && it.xmlName == "user_list" }
        if (userListStat != null) {
            _userListInfo.value = WritingLevelInfoState(
                levelKey = "user_list",
                displayName = "User List",
                percentage = userListStat.percentage
            )
        } else {
             // Force refresh for user list even if not in standard levels
             // This might need a specific call if "user_list" isn't returned by getAllStatistics normally
             // For now, let's assume getAllStatistics handles it or we default to 0
        }

        _writingCategories.value = categories
    }
}
