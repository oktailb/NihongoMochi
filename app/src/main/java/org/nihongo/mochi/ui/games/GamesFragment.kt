package org.nihongo.mochi.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.nihongo.mochi.ui.theme.AppTheme

class GamesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    GamesScreen(
                        onBackClick = { findNavController().popBackStack() },
                        onTaquinClick = { 
                            val action = GamesFragmentDirections.actionNavGamesToNavTaquin()
                            findNavController().navigate(action)
                        },
                        onSimonClick = { 
                            val action = GamesFragmentDirections.actionNavGamesToNavSimon()
                            findNavController().navigate(action)
                        },
                        onTetrisClick = { Toast.makeText(context, "Coming soon: Tetris", Toast.LENGTH_SHORT).show() },
                        onCrosswordsClick = { Toast.makeText(context, "Coming soon: Crosswords", Toast.LENGTH_SHORT).show() },
                        onMemorizeClick = { 
                            val action = GamesFragmentDirections.actionNavGamesToNavMemorize()
                            findNavController().navigate(action)
                        }
                    )
                }
            }
        }
    }
}
