package org.nihongo.mochi.presentation.recognition

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.models.LevelInfoState

/**
 * Represents a level definition, connecting a logical key to a display name identifier.
 * The identifier is an Int, but in common code it has no specific meaning (could be a string ID for Android, etc.)
 * This is a placeholder for a proper KMP resource system for strings.
 */
data class LevelDef(val levelKey: String, val stringIdentifier: Int)

class RecognitionViewModel(
    private val levelContentProvider: LevelContentProvider,
) : ViewModel() {

    // In a real KMP app, these would come from a shared resource system.
    // For now, we define them here. The Fragment will be responsible for providing the string names.
    val levelDefs = listOf(
        LevelDef("N5", 0), // Placeholder IDs
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
        LevelDef("Hiragana", 15),
        LevelDef("Katakana", 16),
        LevelDef("Native Challenge", 17),
        LevelDef("No Reading", 18),
        LevelDef("No Meaning", 19)
    )

    private val _levelInfosState = MutableStateFlow<List<LevelInfoState>>(emptyList())
    val levelInfosState: StateFlow<List<LevelInfoState>> = _levelInfosState.asStateFlow()

    /**
     * Updates the mastery percentages for all levels.
     * This should be called when the screen becomes visible.
     * @param nameProvider A function that takes the identifier from LevelDef and returns the display name.
     */
    fun updateAllButtonPercentages(nameProvider: (Int) -> String) {
        val newStates = levelDefs.map { def ->
            val charactersForLevel = levelContentProvider.getCharactersForLevel(def.levelKey)
            val masteryPercentage = calculateMasteryPercentage(charactersForLevel)
            val displayName = nameProvider(def.stringIdentifier)
            
            LevelInfoState(
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
            val score = ScoreManager.getScore(character, ScoreManager.ScoreType.RECOGNITION)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = characterList.size * 10.0
        if (maxPossiblePoints == 0.0) return 0.0

        return (totalMasteryPoints / maxPossiblePoints) * 100
    }
}
