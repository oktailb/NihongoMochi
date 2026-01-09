package org.nihongo.mochi.ui.recognitiongame

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.nihongo.mochi.domain.game.QuestionDirection
import org.nihongo.mochi.domain.game.RecognitionGameViewModel
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.settings.PRONUNCIATION_PREF_KEY

class RecognitionGameFragment : Fragment() {

    private val args: RecognitionGameFragmentArgs by navArgs()
    
    private val viewModel: RecognitionGameViewModel by viewModels {
        viewModelFactory {
            initializer {
                RecognitionGameViewModel(
                    kanjiRepository = get(),
                    meaningRepository = get(),
                    levelContentProvider = get(),
                    settingsRepository = get()
                )
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
                val buttonStates by viewModel.buttonStates.collectAsState()
                
                // Observe other non-stateflow properties using side effects if needed, 
                // but since we want reactive UI, we should rely on state.
                
                // We need to construct the UI data based on the ViewModel's current state
                // Ideally, ViewModel should expose a single UI state object, but we can map it here for now
                
                val currentKanji = viewModel.currentKanji
                val direction = viewModel.currentDirection
                
                val questionText = if (direction == QuestionDirection.NORMAL) {
                    currentKanji.character
                } else {
                    if (viewModel.gameMode == "meaning") {
                        currentKanji.meanings.take(3).joinToString("\n")
                    } else {
                        viewModel.getFormattedReadings(currentKanji)
                    }
                }
                
                // Map the kanji statuses for the progress bar
                // This logic might need to be reactive if kanjiStatus changes aren't emitted via state
                // However, viewModel.state emissions usually trigger recompiles.
                // NOTE: viewModel.kanjiStatus is a MutableMap, changes might not trigger recomposition unless
                // we observe it or force it. For now, assuming renderState logic covers main flow.
                // A better approach in refactoring would be to have `gameStatus` as a StateFlow in VM.
                val gameStatuses = (0 until 10).map { i ->
                     if (i < viewModel.currentKanjiSet.size) {
                         viewModel.kanjiStatus[viewModel.currentKanjiSet[i]] ?: GameStatus.NOT_ANSWERED
                     } else {
                         GameStatus.NOT_ANSWERED
                     }
                }

                RecognitionGameScreen(
                    kanji = currentKanji,
                    questionText = questionText,
                    gameStatus = gameStatuses,
                    answers = viewModel.currentAnswers,
                    buttonStates = buttonStates,
                    buttonsEnabled = viewModel.areButtonsEnabled, 
                    direction = direction,
                    gameMode = viewModel.gameMode,
                    onAnswerClick = { index, answer ->
                        viewModel.submitAnswer(answer, index)
                    }
                )
                
                // Handle navigation for Finished state
                if (state is GameState.Finished) {
                    // Use a side effect to navigate only once
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pass pronunciation mode to ViewModel
        val pronunciationMode = sharedPreferences.getString(PRONUNCIATION_PREF_KEY, "Hiragana") ?: "Hiragana"
        viewModel.updatePronunciationMode(pronunciationMode)
        
        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        viewModel.setAnimationSpeed(animationSpeed)

        if (!viewModel.isGameInitialized) {
            val customWordList = args.customWordList?.toList()
            val success = viewModel.initializeGame(args.gameMode, args.readingMode, args.level, customWordList)
            if (!success) {
                findNavController().popBackStack()
                return
            }
        }
    }
}
