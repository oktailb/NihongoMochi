package org.nihongo.mochi.ui.wordquiz

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
import androidx.navigation.fragment.navArgs
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType
import org.nihongo.mochi.databinding.FragmentWordQuizBinding
import org.nihongo.mochi.domain.kana.KanaToRomaji
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.Word
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.settings.PRONUNCIATION_PREF_KEY

class WordQuizFragment : Fragment() {

    private var _binding: FragmentWordQuizBinding? = null
    private val binding get() = _binding!!
    private val args: WordQuizFragmentArgs by navArgs()
    private val viewModel: WordQuizViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordQuizBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isGameInitialized) {
            initializeGame()
            viewModel.isGameInitialized = true
        }

        setupUI()

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEach { button ->
            button.setOnClickListener { onAnswerClicked(button) }
        }
    }

    private fun initializeGame() {
        val customWordList = args.customWordList?.toList() ?: emptyList()

        // Load words
        val allWords = if (customWordList.isNotEmpty()) {
            // Re-fetch from repository to get phonetics if possible, or build basic words
            // Since customWordList is just strings, we might miss phonetics unless we look them up
            // For now, let's assume we can fetch them or create simple objects
            // Optimized approach: Fetch all entries and filter
            val allEntries = MochiApplication.wordRepository.getAllWordEntries()
            allEntries.filter { customWordList.contains(it.text) }.map { Word(it.text, it.phonetics) }
        } else {
             // Fallback or empty
             emptyList()
        }
        
        viewModel.allWords = allWords.shuffled().toMutableList()
        viewModel.wordListPosition = 0

        if (viewModel.allWords.isNotEmpty()) {
            startNewSet()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun startNewSet() {
        viewModel.revisionList.clear()
        viewModel.wordStatus.clear()
        // No complex progress tracking for words yet, just correct/incorrect
        
        if (viewModel.wordListPosition >= viewModel.allWords.size) {
            findNavController().popBackStack()
            return
        }

        val nextSet = viewModel.allWords.drop(viewModel.wordListPosition).take(10)
        viewModel.wordListPosition += nextSet.size

        viewModel.currentWordSet.clear()
        viewModel.currentWordSet.addAll(nextSet)
        viewModel.revisionList.addAll(nextSet)
        viewModel.currentWordSet.forEach {
            viewModel.wordStatus[it] = GameStatus.NOT_ANSWERED
        }

        updateProgressBar()
        displayQuestion()
    }

    private fun setupUI() {
        if (viewModel.allWords.isEmpty()) return

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
            
            // Adjust text size
             button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        }
        answerButtons.forEach { it.isEnabled = viewModel.areButtonsEnabled }
        updateProgressBar()
    }

    private fun displayQuestion() {
        if (viewModel.revisionList.isEmpty()) {
            startNewSet()
            return
        }

        viewModel.currentWord = viewModel.revisionList.random()
        displayQuestionText()

        val answers = generateAnswers(viewModel.currentWord)
        viewModel.currentAnswers = answers
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)

        viewModel.buttonColors.clear()
        repeat(4) { viewModel.buttonColors.add(R.color.button_background) }

        answerButtons.zip(answers).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_background))
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        }
        viewModel.areButtonsEnabled = true
        answerButtons.forEach { it.isEnabled = true }
    }

    private fun displayQuestionText() {
        binding.textWordToGuess.text = viewModel.currentWord.text
        
        // Adjust size based on length
        val length = viewModel.currentWord.text.length
        val newSize = when {
             length > 10 -> 40f
             length > 5 -> 60f
             else -> 80f
        }
        binding.textWordToGuess.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
    }

    private fun generateAnswers(correctWord: Word): List<String> {
        val pronunciationMode = sharedPreferences.getString(PRONUNCIATION_PREF_KEY, "Hiragana")
        
        val getReading: (Word) -> String = { word ->
            if (word.phonetics.isNotEmpty()) {
                if (pronunciationMode == "Roman") KanaToRomaji.convert(word.phonetics) 
                else word.phonetics // Default to Hiragana (phonetics in JSON are usually hiragana/katakana mix, but often hiragana for readings)
            } else {
                "?"
            }
        }
        
        val correctAnswer = getReading(correctWord)
        
        val incorrectAnswers = viewModel.allWords
            .asSequence()
            .filter { it.text != correctWord.text }
            .map { getReading(it) }
            .distinct()
            .filter { it != correctAnswer && it != "?" }
            .shuffled()
            .take(3)
            .toList()

        return (incorrectAnswers + correctAnswer).shuffled()
    }

    private fun onAnswerClicked(button: Button) {
        val selectedAnswer = button.text.toString()
        val pronunciationMode = sharedPreferences.getString(PRONUNCIATION_PREF_KEY, "Hiragana")
        
        val correctReading = if (viewModel.currentWord.phonetics.isNotEmpty()) {
             if (pronunciationMode == "Roman") KanaToRomaji.convert(viewModel.currentWord.phonetics)
             else viewModel.currentWord.phonetics
        } else "?"
        
        val isCorrect = selectedAnswer == correctReading

        ScoreManager.saveScore(viewModel.currentWord.text, isCorrect, ScoreType.READING)

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        val buttonIndex = answerButtons.indexOf(button)

        if (isCorrect) {
            viewModel.wordStatus[viewModel.currentWord] = GameStatus.CORRECT
            viewModel.revisionList.remove(viewModel.currentWord)
            viewModel.buttonColors[buttonIndex] = R.color.answer_correct
        } else {
            viewModel.wordStatus[viewModel.currentWord] = GameStatus.INCORRECT
            viewModel.buttonColors[buttonIndex] = R.color.answer_incorrect
            
            // Highlight correct
            val correctButtonIndex = answerButtons.indexOfFirst { it.text == correctReading }
            if (correctButtonIndex != -1) {
                 answerButtons[correctButtonIndex].setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_correct))
            }
        }
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), viewModel.buttonColors[buttonIndex]))

        updateProgressBar()

        viewModel.areButtonsEnabled = false
        answerButtons.forEach { it.isEnabled = false }

        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        val delay = (1000 * animationSpeed).toLong()

        Handler(Looper.getMainLooper()).postDelayed({ displayQuestion() }, delay)
    }

    private fun updateProgressBar() {
        val progressIndicators = (0 until binding.progressBarGame.childCount).map {
            binding.progressBarGame.getChildAt(it) as ImageView
        }

        for (i in 0 until 10) {
            if (i < viewModel.currentWordSet.size) {
                val word = viewModel.currentWordSet[i]
                val status = viewModel.wordStatus[word]
                val indicator = progressIndicators[i]
                indicator.visibility = View.VISIBLE
                indicator.clearColorFilter()
                
                when (status) {
                    GameStatus.CORRECT -> indicator.setImageResource(android.R.drawable.presence_online)
                    GameStatus.INCORRECT -> indicator.setImageResource(android.R.drawable.ic_delete)
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
