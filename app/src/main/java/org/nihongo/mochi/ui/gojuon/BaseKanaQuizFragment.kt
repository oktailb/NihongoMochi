package org.nihongo.mochi.ui.gojuon

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.databinding.FragmentKanaQuizBinding
import org.nihongo.mochi.domain.kana.AndroidResourceLoader
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanaCharacter
import org.nihongo.mochi.domain.models.KanaProgress
import org.nihongo.mochi.domain.models.KanaQuestionDirection

abstract class BaseKanaQuizFragment : Fragment() {

    private var _binding: FragmentKanaQuizBinding? = null
    protected val binding get() = _binding!!
    protected val viewModel: KanaQuizViewModel by viewModels()

    protected lateinit var sharedPreferences: SharedPreferences

    // Abstract property to specify Kana Type (Hiragana or Katakana)
    abstract val kanaType: org.nihongo.mochi.domain.kana.KanaType

    abstract fun getQuizModeArgument(): String
    abstract fun getLevelArgument(): String

    // Lazy init of repository
    private val kanaRepository by lazy {
        KanaRepository(AndroidResourceLoader(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanaQuizBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isGameInitialized) {
            initializeGame()
        }

        setupUI()

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEach { button ->
            button.setOnClickListener { onAnswerClicked(button) }
        }
    }

    private fun initializeGame() {
        viewModel.resetState()
        val modeArg = getQuizModeArgument()
        viewModel.quizMode = if (modeArg == "Kana -> Romaji") QuizMode.KANA_TO_ROMAJI else QuizMode.ROMAJI_TO_KANA

        val allCharacters = loadKana(kanaType)
        val level = getLevelArgument()
        viewModel.allKana = filterCharactersForLevel(allCharacters, level).shuffled()

        if (viewModel.allKana.isNotEmpty()) {
            startNewSet()
            viewModel.isGameInitialized = true
        } else {
            findNavController().popBackStack()
        }
    }

    private fun startNewSet() {
        viewModel.revisionList.clear()
        viewModel.kanaStatus.clear()
        viewModel.kanaProgress.clear()

        if (viewModel.kanaListPosition >= viewModel.allKana.size) {
            findNavController().popBackStack()
            return
        }

        val nextSet = viewModel.allKana.drop(viewModel.kanaListPosition).take(10)
        viewModel.kanaListPosition += nextSet.size

        viewModel.currentKanaSet.clear()
        viewModel.currentKanaSet.addAll(nextSet)
        viewModel.revisionList.addAll(nextSet)
        viewModel.currentKanaSet.forEach {
            viewModel.kanaStatus[it] = GameStatus.NOT_ANSWERED
            viewModel.kanaProgress[it] = KanaProgress()
        }

        updateProgressBar()
        displayQuestion()
    }

    private fun loadKana(type: org.nihongo.mochi.domain.kana.KanaType): List<KanaCharacter> {
        return kanaRepository.getKanaEntries(type).map { entry ->
            KanaCharacter(entry.character, entry.romaji, entry.category)
        }
    }

    private fun filterCharactersForLevel(allCharacters: List<KanaCharacter>, level: String): List<KanaCharacter> {
        return when (level) {
            "Gojūon" -> allCharacters.filter { it.category == "gojuon" }
            "Dakuon" -> allCharacters.filter { it.category == "dakuon" || it.category == "handakuon" }
            "Yōon" -> allCharacters.filter { it.category == "yoon" }
            else -> allCharacters
        }
    }

    private fun setupUI() {
        if (viewModel.allKana.isEmpty()) return

        displayQuestionText()

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEachIndexed { index, button ->
            if (viewModel.currentAnswers.size > index) {
                button.text = viewModel.currentAnswers[index]
            }
            if (viewModel.buttonColors.size > index) {
                val colorRes = viewModel.buttonColors[index]
                if (colorRes != 0) {
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_background))
                }
            }
            
            // Restore text size based on direction
            if (viewModel.currentDirection == KanaQuestionDirection.REVERSE) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            } else {
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            }
        }
        answerButtons.forEach { it.isEnabled = viewModel.areButtonsEnabled }

        updateProgressBar()
    }

    private fun updateProgressBar() {
        val progressIndicators = (0 until binding.progressBarGame.childCount).map {
            binding.progressBarGame.getChildAt(it) as ImageView
        }

        for (i in 0 until 10) {
            if (i < viewModel.currentKanaSet.size) {
                val kana = viewModel.currentKanaSet[i]
                val status = viewModel.kanaStatus[kana]
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

    private fun displayQuestion() {
        if (viewModel.revisionList.isEmpty()) {
            startNewSet()
            return
        }

        viewModel.currentQuestion = viewModel.revisionList.random()
        val progress = viewModel.kanaProgress[viewModel.currentQuestion]!!

        viewModel.currentDirection = when {
            !progress.normalSolved && !progress.reverseSolved -> if (Math.random() < 0.5) KanaQuestionDirection.NORMAL else KanaQuestionDirection.REVERSE
            !progress.normalSolved -> KanaQuestionDirection.NORMAL
            else -> KanaQuestionDirection.REVERSE
        }

        displayQuestionText()

        val answers = generateAnswers(viewModel.currentQuestion)
        viewModel.currentAnswers = answers
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)

        viewModel.buttonColors.clear()
        repeat(4) { viewModel.buttonColors.add(R.color.button_background) }

        answerButtons.zip(answers).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_background))

            if (viewModel.currentDirection == KanaQuestionDirection.REVERSE) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            } else {
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            }
        }
        viewModel.areButtonsEnabled = true
        answerButtons.forEach { it.isEnabled = true }
    }

    private fun displayQuestionText() {
        if (!::sharedPreferences.isInitialized) return // Guard against uninitialized state during rotation
        if (!viewModel.isGameInitialized) return
        
        if (viewModel.currentDirection == KanaQuestionDirection.NORMAL) {
            binding.textKanaCharacter.text = viewModel.currentQuestion.kana
            binding.textKanaCharacter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 120f)
        } else {
            binding.textKanaCharacter.text = viewModel.currentQuestion.romaji
            binding.textKanaCharacter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 80f)
        }
    }

    private fun generateAnswers(correctChar: KanaCharacter): List<String> {
        val isNormal = viewModel.currentDirection == KanaQuestionDirection.NORMAL
        val correctAnswer = if (isNormal) correctChar.romaji else correctChar.kana
        val incorrectAnswers = viewModel.allKana
            .asSequence()
            .map { if (isNormal) it.romaji else it.kana }
            .distinct()
            .filter { it != correctAnswer }
            .shuffled()
            .take(3)
            .toList()
        return (incorrectAnswers + correctAnswer).shuffled()
    }

    private fun onAnswerClicked(button: Button) {
        val selectedAnswer = button.text.toString()
        val isNormal = viewModel.currentDirection == KanaQuestionDirection.NORMAL
        val correctAnswerText = if (isNormal) viewModel.currentQuestion.romaji else viewModel.currentQuestion.kana
        val isCorrect = selectedAnswer == correctAnswerText

        ScoreManager.saveScore(viewModel.currentQuestion.kana, isCorrect, ScoreManager.ScoreType.RECOGNITION)

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        val buttonIndex = answerButtons.indexOf(button)

        if (isCorrect) {
            val progress = viewModel.kanaProgress[viewModel.currentQuestion]!!
            if (isNormal) progress.normalSolved = true else progress.reverseSolved = true

            if (progress.normalSolved && progress.reverseSolved) {
                viewModel.kanaStatus[viewModel.currentQuestion] = GameStatus.CORRECT
                viewModel.revisionList.remove(viewModel.currentQuestion)
                viewModel.buttonColors[buttonIndex] = R.color.answer_correct
            } else {
                viewModel.kanaStatus[viewModel.currentQuestion] = GameStatus.PARTIAL
                viewModel.buttonColors[buttonIndex] = R.color.answer_neutral
            }
        } else {
            viewModel.kanaStatus[viewModel.currentQuestion] = GameStatus.INCORRECT
            viewModel.buttonColors[buttonIndex] = R.color.answer_incorrect
            // Highlight correct answer
            val correctButtonIndex = answerButtons.indexOfFirst { it.text == correctAnswerText }
            if (correctButtonIndex != -1) {
                answerButtons[correctButtonIndex].setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_correct))
            }
        }
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), viewModel.buttonColors[buttonIndex]))

        // Immediate update of progress bar
        updateProgressBar()

        viewModel.areButtonsEnabled = false
        answerButtons.forEach { it.isEnabled = false }

        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        val delay = (1000 * animationSpeed).toLong()

        Handler(Looper.getMainLooper()).postDelayed({ displayQuestion() }, delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
