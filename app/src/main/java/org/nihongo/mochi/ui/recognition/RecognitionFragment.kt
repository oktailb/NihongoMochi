package org.nihongo.mochi.ui.recognition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.ui.theme.AppTheme

data class LevelDef(val levelKey: String, val stringResId: Int? = null)

class RecognitionFragment : Fragment() {

    private val levelContentProvider: LevelContentProvider by inject()

    private val levelDefs = listOf(
        LevelDef("N5", R.string.level_n5),
        LevelDef("N4", R.string.level_n4),
        LevelDef("N3", R.string.level_n3),
        LevelDef("N2", R.string.level_n2),
        LevelDef("N1", R.string.level_n1),
        LevelDef("Grade 1", R.string.level_class_1),
        LevelDef("Grade 2", R.string.level_class_2),
        LevelDef("Grade 3", R.string.level_class_3),
        LevelDef("Grade 4", R.string.level_class_4),
        LevelDef("Grade 5", R.string.level_class_5),
        LevelDef("Grade 6", R.string.level_class_6),
        LevelDef("Grade 7", R.string.level_high_school_1),
        LevelDef("Grade 8", R.string.level_high_school_2),
        LevelDef("Grade 9", R.string.level_high_school_3),
        LevelDef("Grade 10", R.string.level_high_school_4),
        LevelDef("Hiragana", R.string.level_hiragana),
        LevelDef("Katakana", R.string.level_katakana),
        LevelDef("Native Challenge", R.string.challenge_native),
        LevelDef("No Reading", R.string.challenge_no_reading),
        LevelDef("No Meaning", R.string.challenge_no_meaning)
    )

    private val _levelInfosState = MutableStateFlow<List<LevelInfoState>>(emptyList())
    private val levelInfosState: StateFlow<List<LevelInfoState>> = _levelInfosState.asStateFlow()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val levelInfos by levelInfosState.collectAsState()
                    RecognitionScreen(
                        levelInfos = levelInfos,
                        onLevelClick = { levelKey ->
                            onLevelClicked(levelKey)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAllButtonPercentages()
    }

    private fun updateAllButtonPercentages() {
        val newStates = levelDefs.map { def ->
            val charactersForLevel = levelContentProvider.getCharactersForLevel(def.levelKey)
            val masteryPercentage = calculateMasteryPercentage(charactersForLevel)
            
            // For challenges, we might want to use the string resource directly as display name 
            // instead of levelKey if it wasn't a challenge button in the original.
            // But looking at original code:
            // Native Challenge uses R.string.challenge_native as text
            // No Reading uses R.string.challenge_no_reading
            // etc.
            
            val displayName = def.stringResId?.let { getString(it) } ?: def.levelKey
            
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
            val score = ScoreManager.getScore(character, ScoreType.RECOGNITION)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = characterList.size * 10.0
        if (maxPossiblePoints == 0.0) return 0.0

        return (totalMasteryPoints / maxPossiblePoints) * 100
    }

    private fun onLevelClicked(levelKey: String) {
        when (levelKey) {
            "Hiragana" -> findNavController().navigate(R.id.action_nav_recognition_to_nav_hiragana)
            "Katakana" -> findNavController().navigate(R.id.action_nav_recognition_to_nav_katakana)
            else -> navigateToRecap(levelKey)
        }
    }

    private fun navigateToRecap(levelKey: String) {
        val bundle = Bundle().apply { putString("level", levelKey) }
        findNavController().navigate(R.id.action_nav_recognition_to_game_recap, bundle)
    }
}
