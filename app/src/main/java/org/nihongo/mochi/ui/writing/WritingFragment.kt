package org.nihongo.mochi.ui.writing

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
import org.nihongo.mochi.presentation.writing.WritingViewModel
import org.nihongo.mochi.ui.theme.AppTheme

class WritingFragment : Fragment() {

    private val viewModel: WritingViewModel by viewModel()

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
        15 to R.string.writing_user_lists,
        16 to R.string.challenge_native,
        17 to R.string.challenge_no_reading,
        18 to R.string.challenge_no_meaning
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
                    WritingScreen(
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

    private fun onLevelClicked(level: String) {
        val action = WritingFragmentDirections.actionNavWritingToWritingRecap(level)
        findNavController().navigate(action)
    }
}
