package org.nihongo.mochi.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.compose.koinViewModel
import org.nihongo.mochi.ui.games.taquin.TaquinGameScreen
import org.nihongo.mochi.ui.games.taquin.TaquinSetupScreen
import org.nihongo.mochi.ui.games.taquin.TaquinViewModel
import org.nihongo.mochi.ui.theme.AppTheme

class TaquinFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val viewModel: TaquinViewModel = koinViewModel()
                    var isPlaying by remember { mutableStateOf(false) }

                    if (!isPlaying) {
                        TaquinSetupScreen(
                            viewModel = viewModel,
                            onBackClick = { findNavController().popBackStack() },
                            onStartGame = { isPlaying = true }
                        )
                    } else {
                        TaquinGameScreen(
                            viewModel = viewModel,
                            onBackClick = { isPlaying = false }
                        )
                    }
                }
            }
        }
    }
}
