package org.nihongo.mochi.ui.recognitiongame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
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
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentRecognitionGameBinding
import org.nihongo.mochi.domain.game.QuestionDirection
import org.nihongo.mochi.domain.game.RecognitionGameViewModel
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.util.TextSizeCalculator
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.settings.PRONUNCIATION_PREF_KEY

class RecognitionGameFragment : Fragment() {

    private var _binding: FragmentRecognitionGameBinding? = null
    private val binding get() = _binding!!
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
        _binding = FragmentRecognitionGameBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return binding.root
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

        setupStateObservation()

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEachIndexed { index, button ->
            button.setOnClickListener { onAnswerClicked(button, index) }
        }
    }

    private fun setupStateObservation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        renderState(state)
                    }
                }
                launch {
                    viewModel.buttonStates.collect { states ->
                        updateButtonStates(states)
                    }
                }
            }
        }
    }

    private fun renderState(state: GameState) {
        when(state) {
            GameState.Loading -> { 
                // Could show loading indicator
            }
            GameState.WaitingForAnswer -> {
                displayQuestion()
            }
            is GameState.ShowingResult -> {
                // Button colors are updated via buttonStates flow
                val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
                answerButtons.forEach { it.isEnabled = false }
                updateProgressBar()
            }
            GameState.Finished -> {
                findNavController().popBackStack()
            }
        }
    }
    
    private fun updateButtonStates(states: List<AnswerButtonState>) {
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        
        states.forEachIndexed { index, state ->
            if (index < answerButtons.size) {
                val colorRes = when(state) {
                    AnswerButtonState.DEFAULT -> R.color.button_background
                    AnswerButtonState.CORRECT -> R.color.answer_correct
                    AnswerButtonState.INCORRECT -> R.color.answer_incorrect
                    AnswerButtonState.NEUTRAL -> R.color.answer_neutral
                }
                answerButtons[index].setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))
            }
        }
    }

    private fun updateButtonSize(button: Button, text: String) {
        val newSize = TextSizeCalculator.calculateButtonTextSize(text.length, viewModel.currentDirection)
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
    }

    private fun displayQuestion() {
        displayQuestionText()
        updateProgressBar()

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        val answers = viewModel.currentAnswers

        answerButtons.zip(answers).forEach { (button, answerText) ->
            button.text = answerText
            button.isEnabled = true
            updateButtonSize(button, answerText)
        }
        viewModel.areButtonsEnabled = true
    }

    private fun displayQuestionText() {
        if (viewModel.currentDirection == QuestionDirection.NORMAL) {
            binding.textKanjiToGuess.text = viewModel.currentKanji.character
            binding.textKanjiToGuess.setTextSize(TypedValue.COMPLEX_UNIT_SP, 120f)
        } else {
            val questionText = if (viewModel.gameMode == "meaning") {
                viewModel.currentKanji.meanings.joinToString("\n")
            } else {
                viewModel.getFormattedReadings(viewModel.currentKanji)
            }
            binding.textKanjiToGuess.text = questionText

            val lineCount = questionText.count { it == '\n' } + 1
            val newSize = TextSizeCalculator.calculateQuestionTextSize(questionText.length, lineCount, viewModel.currentDirection)
            
            binding.textKanjiToGuess.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
        }
    }

    private fun onAnswerClicked(button: Button, index: Int) {
        val selectedAnswer = button.text.toString()
        viewModel.submitAnswer(selectedAnswer, index)
        // Disable buttons immediately to prevent double clicks
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEach { it.isEnabled = false }
    }
    
    private fun updateProgressBar() {
        val progressIndicators = (0 until binding.progressBarGame.childCount).map {
            binding.progressBarGame.getChildAt(it) as ImageView
        }

        for (i in 0 until 10) {
            if (i < viewModel.currentKanjiSet.size) {
                val kanji = viewModel.currentKanjiSet[i]
                val status = viewModel.kanjiStatus[kanji]
                val indicator = progressIndicators[i]
                indicator.visibility = View.VISIBLE

                indicator.clearColorFilter()

                when (status) {
                    GameStatus.CORRECT -> indicator.setImageResource(android.R.drawable.presence_online)
                    GameStatus.INCORRECT -> indicator.setImageResource(android.R.drawable.ic_delete)
                    GameStatus.PARTIAL -> {
                        indicator.setImageResource(android.R.drawable.ic_menu_recent_history)
                        indicator.setColorFilter(ContextCompat.getColor(requireContext(), R.color.answer_neutral))
                    }
                    else -> indicator.setImageResource(android.R.drawable.checkbox_off_background)
                }
            } else {
                progressIndicators[i].visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
