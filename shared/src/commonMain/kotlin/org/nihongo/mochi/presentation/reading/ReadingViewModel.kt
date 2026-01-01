package org.nihongo.mochi.presentation.reading

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.util.LevelContentProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// This model is already in shared: org.nihongo.mochi.presentation.models.ReadingLevelInfoState
import org.nihongo.mochi.presentation.models.ReadingLevelInfoState

data class ReadingScreenState(
    val jlptLevels: List<ReadingLevelInfoState> = emptyList(),
    val wordLevels: List<ReadingLevelInfoState> = emptyList(),
    val userListInfo: ReadingLevelInfoState = ReadingLevelInfoState("user_custom_list", "", 0)
)

class ReadingViewModel(
    private val levelContentProvider: LevelContentProvider
) : ViewModel() {

    private val _state = MutableStateFlow(ReadingScreenState())
    val state: StateFlow<ReadingScreenState> = _state.asStateFlow()

    fun refreshData(userListNameProvider: () -> String) {
        // This logic is based on the old ReadingViewModel from the domain
        _state.update {
            it.copy(
                jlptLevels = listOf(
                    createLevelState("jlpt_wordlist_n5", "N5"),
                    createLevelState("jlpt_wordlist_n4", "N4"),
                    createLevelState("jlpt_wordlist_n3", "N3"),
                    createLevelState("jlpt_wordlist_n2", "N2"),
                    createLevelState("jlpt_wordlist_n1", "N1")
                ),
                wordLevels = listOf(
                    createLevelState("bccwj_wordlist_1000", "1000"),
                    createLevelState("bccwj_wordlist_2000", "2000"),
                    createLevelState("bccwj_wordlist_3000", "3000"),
                    createLevelState("bccwj_wordlist_4000", "4000"),
                    createLevelState("bccwj_wordlist_5000", "5000"),
                    createLevelState("bccwj_wordlist_6000", "6000"),
                    createLevelState("bccwj_wordlist_7000", "7000"),
                    createLevelState("bccwj_wordlist_8000", "8000")
                ),
                userListInfo = createLevelState("user_custom_list", userListNameProvider())
            )
        }
    }

    private fun createLevelState(levelId: String, displayName: String): ReadingLevelInfoState {
        val wordList = levelContentProvider.getCharactersForLevel(levelId)
        val percentage = calculateMasteryPercentage(wordList)
        return ReadingLevelInfoState(levelId, displayName, percentage.toInt())
    }

    private fun calculateMasteryPercentage(wordList: List<String>): Double {
        if (wordList.isEmpty()) return 0.0

        val totalMasteryPoints = wordList.sumOf { word ->
            val score = ScoreManager.getScore(word, ScoreManager.ScoreType.READING)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = wordList.size * 10.0
        return if (maxPossiblePoints > 0) (totalMasteryPoints / maxPossiblePoints) * 100 else 0.0
    }
}
