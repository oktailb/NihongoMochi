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
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.nihongo.mochi.R
import org.nihongo.mochi.presentation.recognition.RecognitionViewModel
import org.nihongo.mochi.ui.theme.AppTheme
// No explicit import of LevelInfoState needed if RecognitionScreen is correct

class RecognitionFragment : Fragment() {

    private val viewModel: RecognitionViewModel by viewModel()

    private val nameIdMap = mapOf(
        0 to R.string.level_n5,
        1 to R.string.level_n4,
        2 to R.string.level_n3,
        3 to R.string.level_n2,
        4 to R.string.level_n1,
        5 to R.string.level_class_1,
        6 to R.string.level_class_2,
        7 to R.string.level_class_3,
        8 to R.string.level_class_4,
        9 to R.string.level_class_5,
        10 to R.string.level_class_6,
        11 to R.string.level_high_school_1,
        12 to R.string.level_high_school_2,
        13 to R.string.level_high_school_3,
        14 to R.string.level_high_school_4,
        15 to R.string.level_hiragana,
        16 to R.string.level_katakana,
        17 to R.string.challenge_native,
        18 to R.string.challenge_no_reading,
        19 to R.string.challenge_no_meaning
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val levelInfos by viewModel.levelInfosState.collectAsState()
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
        viewModel.updateAllButtonPercentages { identifier ->
            val resId = nameIdMap[identifier] ?: 0
            if (resId != 0) getString(resId) else ""
        }
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
