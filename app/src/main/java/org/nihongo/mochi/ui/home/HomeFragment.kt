package org.nihongo.mochi.ui.home

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
import org.nihongo.mochi.presentation.HomeViewModel
import org.nihongo.mochi.ui.theme.AppTheme
import org.nihongo.mochi.ui.gamerecap.GameRecapFragmentArgs
import org.nihongo.mochi.ui.writingrecap.WritingRecapFragmentArgs
import org.nihongo.mochi.ui.wordlist.WordListFragmentArgs

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val uiState by homeViewModel.uiState.collectAsState()
                
                AppTheme {
                    HomeScreen(
                        availableLevels = uiState.availableLevels,
                        selectedLevelId = uiState.selectedLevelId,
                        isRecognitionEnabled = uiState.isRecognitionEnabled,
                        isReadingEnabled = uiState.isReadingEnabled,
                        isWritingEnabled = uiState.isWritingEnabled,
                        onLevelSelected = homeViewModel::onLevelSelected,
                        onRecognitionClick = {
                            if (uiState.isRecognitionEnabled) {
                                when(uiState.selectedLevelId) {
                                    "hiragana" -> findNavController().navigate(R.id.action_nav_home_to_nav_hiragana)
                                    "katakana" -> findNavController().navigate(R.id.action_nav_home_to_nav_katakana)
                                    else -> {
                                        val args = Bundle().apply {
                                            putString("level", uiState.selectedLevelId)
                                        }
                                        findNavController().navigate(R.id.action_nav_home_to_game_recap, args)
                                    }
                                }
                            }
                        },
                        onReadingClick = {
                            if (uiState.isReadingEnabled) {
                                val args = Bundle().apply {
                                    // Reading expects the data file name (e.g. "jlpt_wordlist_n5"), not the level ID
                                    putString("wordList", uiState.readingDataFile ?: uiState.selectedLevelId)
                                }
                                findNavController().navigate(R.id.action_nav_home_to_word_list, args)
                            }
                        },
                        onWritingClick = {
                            if (uiState.isWritingEnabled) {
                                val args = Bundle().apply {
                                    putString("level", uiState.selectedLevelId)
                                }
                                findNavController().navigate(R.id.action_nav_home_to_writing_recap, args)
                            }
                        },
                        onDictionaryClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_dictionary)
                        },
                        onResultsClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_results)
                        },
                        onOptionsClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_options)
                        },
                        onAboutClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_about)
                        }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        homeViewModel.onResume()
    }
}
