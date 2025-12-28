package org.nihongo.mochi.ui.recognitiongame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import org.nihongo.mochi.databinding.FragmentRecognitionGameBinding
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.settings.PRONUNCIATION_PREF_KEY
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.Reading
import java.util.Locale

class RecognitionGameFragment : Fragment() {

    private var _binding: FragmentRecognitionGameBinding? = null
    private val binding get() = _binding!!
    private val args: RecognitionGameFragmentArgs by navArgs()
    private val viewModel: RecognitionGameViewModel by viewModels()

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
        viewModel.gameMode = args.gameMode
        viewModel.readingMode = args.readingMode
        val level = args.level
        val customWordList = args.customWordList?.toList() ?: emptyList()

        loadAllKanjiDetails()

        val kanjiCharsForLevel: List<String> = if (customWordList.isNotEmpty()) {
            customWordList
        } else {
            loadKanjiCharsForLevel(level)
        }

        viewModel.allKanjiDetails.clear()
        viewModel.allKanjiDetails.addAll(
            viewModel.allKanjiDetailsXml.filter {
                kanjiCharsForLevel.contains(it.character) && it.meanings.isNotEmpty()
            }
        )
        viewModel.allKanjiDetails.shuffle()
        viewModel.kanjiListPosition = 0

        if (viewModel.allKanjiDetails.isNotEmpty()) {
            startNewSet()
        } else {
            Log.e("RecognitionGameFragment", "No kanji loaded for level: $level.")
            findNavController().popBackStack()
        }
    }

    private fun setupUI() {
        if (viewModel.allKanjiDetails.isEmpty()) return

        displayQuestionText()
        updateProgressBar()

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEachIndexed { index, button ->
            if (viewModel.currentAnswers.size > index) {
                val answerText = viewModel.currentAnswers[index]
                button.text = answerText
                updateButtonSize(button, answerText)
            }
            if (viewModel.buttonColors.size > index) {
                val colorRes = viewModel.buttonColors[index]
                if (colorRes != 0) {
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_background))
                }
            }
        }
        answerButtons.forEach { it.isEnabled = viewModel.areButtonsEnabled }
    }
    
    private fun updateButtonSize(button: Button, text: String) {
        if (viewModel.currentDirection == QuestionDirection.REVERSE) {
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
        } else {
            val length = text.length
            val newSize = when {
                length > 40 -> 10f
                length > 20 -> 12f
                else -> 14f
            }
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
        }
    }

    private fun startNewSet() {
        val hasNext = viewModel.startNewSet()

        if (!hasNext) {
            findNavController().popBackStack()
            return
        }

        updateProgressBar()
        displayQuestion()
    }

    private fun loadKanjiCharsForLevel(levelName: String): List<String> {
        val (type, value) = when {
            levelName.startsWith("N") -> "jlpt" to levelName
            levelName.startsWith("Grade ") -> {
                val grade = levelName.removePrefix("Grade ")
                "grade" to grade
            }
            else -> return emptyList()
        }
        
        return MochiApplication.kanjiRepository.getKanjiByLevel(type, value).map { it.character }
    }

    private fun loadAllKanjiDetails() {
        val locale = MochiApplication.settingsRepository.getAppLocale()
        val meanings = MochiApplication.meaningRepository.getMeanings(locale)
        val allKanjiEntries = MochiApplication.kanjiRepository.getAllKanji()
        
        viewModel.allKanjiDetailsXml.clear()
        
        for (entry in allKanjiEntries) {
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            
            val kanjiDetail = KanjiDetail(id, character, kanjiMeanings, readingsList)
            viewModel.allKanjiDetailsXml.add(kanjiDetail)
        }
    }

    private fun displayQuestion() {
        if (viewModel.revisionList.isEmpty()) {
            startNewSet()
            return
        }

        viewModel.nextQuestion()

        displayQuestionText()

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        val answers = viewModel.currentAnswers

        viewModel.buttonColors.clear()
        repeat(4) { viewModel.buttonColors.add(R.color.button_background) }

        answerButtons.zip(answers).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_background))
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

            val length = questionText.length
            val lineCount = questionText.count { it == '\n' } + 1

            val newSize = when {
                lineCount > 7 || length > 100 -> 14f
                lineCount > 5 || length > 70 -> 18f
                lineCount > 3 || length > 40 -> 24f
                lineCount > 1 || length > 15 -> 32f
                else -> 48f
            }
            binding.textKanjiToGuess.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
        }
    }

    private fun onAnswerClicked(button: Button) {
        val selectedAnswer = button.text.toString()
        val isCorrect = viewModel.submitAnswer(selectedAnswer)

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        val buttonIndex = answerButtons.indexOf(button)

        if (isCorrect) {
            viewModel.buttonColors[buttonIndex] = R.color.answer_correct
            
            // Check if partial (one direction solved), color stays neutral?
            val status = viewModel.kanjiStatus[viewModel.currentKanji]
            if (status == GameStatus.PARTIAL) {
                 viewModel.buttonColors[buttonIndex] = R.color.answer_neutral
            }

        } else {
            viewModel.buttonColors[buttonIndex] = R.color.answer_incorrect
        }

        button.setBackgroundColor(ContextCompat.getColor(requireContext(), viewModel.buttonColors[buttonIndex]))

        updateProgressBar()

        answerButtons.forEach { it.isEnabled = false }
        viewModel.areButtonsEnabled = false

        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        val delay = (1000 * animationSpeed).toLong()

        Handler(Looper.getMainLooper()).postDelayed({
            displayQuestion()
        }, delay)
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
