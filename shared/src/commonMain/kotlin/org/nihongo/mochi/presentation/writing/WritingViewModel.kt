package org.nihongo.mochi.presentation.writing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.util.LevelContentProvider

// Using the shared model from presentation.models
import org.nihongo.mochi.presentation.models.WritingLevelInfoState

data class LevelDef(val levelKey: String, val stringIdentifier: Int)

class WritingViewModel(
    private val levelContentProvider: LevelContentProvider
) : ViewModel() {

    val levelDefs = listOf(
        LevelDef("N5", 0),
        LevelDef("N4", 1),
        LevelDef("N3", 2),
        LevelDef("N2", 3),
        LevelDef("N1", 4),
        LevelDef("Grade 1", 5),
        LevelDef("Grade 2", 6),
        LevelDef("Grade 3", 7),
        LevelDef("Grade 4", 8),
        LevelDef("Grade 5", 9),
        LevelDef("Grade 6", 10),
        LevelDef("Grade 7", 11),
        LevelDef("Grade 8", 12),
        LevelDef("Grade 9", 13),
        LevelDef("Grade 10", 14),
        LevelDef("user_custom_list", 15),
        LevelDef("Native Challenge", 16),
        LevelDef("No Reading", 17),
        LevelDef("No Meaning", 18)
    )

    private val _levelInfosState = MutableStateFlow<List<WritingLevelInfoState>>(emptyList())
    val levelInfosState: StateFlow<List<WritingLevelInfoState>> = _levelInfosState.asStateFlow()

    fun updateAllButtonPercentages(nameProvider: (Int) -> String) {
        val newStates = levelDefs.map { def ->
            val charactersForLevel = levelContentProvider.getCharactersForLevel(def.levelKey)
            val masteryPercentage = calculateMasteryPercentage(charactersForLevel)
            val displayName = nameProvider(def.stringIdentifier)

            WritingLevelInfoState(
                levelKey = def.levelKey,
                displayName = displayName,
                percentage = masteryPercentage.toInt()
            )
        }
        _levelInfosState.update { newStates }
    }

    private fun calculateMasteryPercentage(characterList: List<String>): Double {
        if (characterList.isEmpty()) return 0.0

        val totalMasteryPoints = characterList.sumOf { character ->
            val score = ScoreManager.getScore(character, ScoreManager.ScoreType.WRITING)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = characterList.size * 10.0
        return if (maxPossiblePoints > 0) (totalMasteryPoints / maxPossiblePoints) * 100 else 0.0
    }
}
