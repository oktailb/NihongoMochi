package org.nihongo.mochi.ui.writinggame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.android.ext.android.get
import org.nihongo.mochi.domain.game.WritingGameViewModel
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY

class WritingGameFragment : Fragment() {

    private val args: WritingGameFragmentArgs by navArgs()
    
    private val viewModel: WritingGameViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WritingGameViewModel(
                    kanjiRepository = get(),
                    meaningRepository = get(),
                    levelContentProvider = get(),
                    settingsRepository = get(),
                    textNormalizer = AndroidTextNormalizer()
                ) as T
            }
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        
        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by viewModel.state.collectAsState()
                
                // We access engine properties directly (not as StateFlow) for some fields, which is acceptable 
                // if they are stable or if parent 'state' change triggers recomposition.
                // However, for best reactivity, we rely on viewModel.state driving the main UI refresh.
                // The fields like currentKanji etc are updated before state emission in engine.
                
                // Since WritingGameViewModel exposes simple getters for engine properties, 
                // we should make sure we read them inside the Composable scope where recomposition happens.
                
                if (state is GameState.Loading) {
                    // Show loading or empty
                } else {
                    WritingGameScreen(
                        kanji = if (viewModel.isGameInitialized) viewModel.currentKanji else null,
                        questionType = viewModel.currentQuestionType,
                        gameStatus = (0 until 10).map { i ->
                             if (i < viewModel.currentKanjiSet.size) {
                                 viewModel.kanjiStatus[viewModel.currentKanjiSet[i]] ?: org.nihongo.mochi.domain.models.GameStatus.NOT_ANSWERED
                             } else {
                                 org.nihongo.mochi.domain.models.GameStatus.NOT_ANSWERED
                             }
                        },
                        onSubmitAnswer = { answer ->
                            viewModel.submitAnswer(answer)
                        },
                        showCorrection = viewModel.showCorrectionFeedback,
                        isCorrect = viewModel.lastAnswerStatus,
                        processingAnswer = viewModel.isAnswerProcessing
                    )
                }

                if (state is GameState.Finished) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        viewModel.setAnimationSpeed(animationSpeed)
        
        val level = args.level ?: ""
        if (!viewModel.isGameInitialized) {
            viewModel.initializeGame(level)
        }
    }
}
