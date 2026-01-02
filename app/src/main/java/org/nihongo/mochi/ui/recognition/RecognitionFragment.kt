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

class RecognitionFragment : Fragment() {

    private val viewModel: RecognitionViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val categories by viewModel.categoriesState.collectAsState()
                    RecognitionScreen(
                        categories = categories,
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
        viewModel.refreshData { resourceKey ->
            val resId = resources.getIdentifier(resourceKey, "string", requireContext().packageName)
            if (resId != 0) getString(resId) else resourceKey
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
