package org.nihongo.mochi.ui.recognitiongame

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
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
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType
import org.nihongo.mochi.databinding.FragmentRecognitionGameBinding
import org.nihongo.mochi.domain.kana.KanaToRomaji
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.settings.PRONUNCIATION_PREF_KEY
import org.nihongo.mochi.ui.game.GameStatus
import org.nihongo.mochi.ui.game.KanjiDetail
import org.nihongo.mochi.ui.game.KanjiProgress
import org.nihongo.mochi.ui.game.Reading
import org.xmlpull.v1.XmlPullParser
import java.io.IOException

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

        // Load all kanji details from Shared Repository and XML Meanings
        loadAllKanjiDetails()

        val kanjiCharsForLevel: List<String> = if (customWordList.isNotEmpty()) {
            customWordList
        } else {
            loadKanjiCharsForLevel(level)
        }

        viewModel.allKanjiDetails.clear()
        // Filter loaded details to keep only those in the current level/list
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

                if (viewModel.currentDirection == QuestionDirection.REVERSE) {
                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
                } else {
                    val length = answerText.length
                    val newSize = when {
                        length > 40 -> 10f
                        length > 20 -> 12f
                        else -> 14f
                    }
                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
                }
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


    private fun startNewSet() {
        viewModel.revisionList.clear()
        viewModel.kanjiStatus.clear()
        viewModel.kanjiProgress.clear()

        if (viewModel.kanjiListPosition >= viewModel.allKanjiDetails.size) {
            findNavController().popBackStack()
            return
        }

        val nextSet = viewModel.allKanjiDetails.drop(viewModel.kanjiListPosition).take(10)
        viewModel.kanjiListPosition += nextSet.size

        viewModel.currentKanjiSet.clear()
        viewModel.currentKanjiSet.addAll(nextSet)
        viewModel.revisionList.addAll(nextSet)
        viewModel.currentKanjiSet.forEach {
            viewModel.kanjiStatus[it] = GameStatus.NOT_ANSWERED
            viewModel.kanjiProgress[it] = KanjiProgress()
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
            // Add custom mapping if needed for "Grade 7" -> "7" etc.
            else -> return emptyList()
        }
        
        return MochiApplication.kanjiRepository.getKanjiByLevel(type, value).map { it.character }
    }

    private fun loadMeanings(): Map<String, List<String>> {
        val meaningsMap = mutableMapOf<String, MutableList<String>>()
        try {
            val parser = resources.getXml(R.xml.meanings)
            var currentId: String? = null
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "kanji") {
                            currentId = parser.getAttributeValue(null, "id")
                            if (currentId != null) {
                                meaningsMap.putIfAbsent(currentId, mutableListOf())
                            }
                        } else if (parser.name == "meaning" && currentId != null) {
                            meaningsMap[currentId]?.add(parser.nextText())
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "kanji") {
                            currentId = null
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("RecognitionGameFragment", "meanings.xml not found.", e)
        } catch (e: Exception) {
            Log.e("RecognitionGameFragment", "Error parsing meanings.xml", e)
        }
        return meaningsMap
    }

    private fun loadAllKanjiDetails() {
        // Load meanings from XML (Legacy)
        val meanings = loadMeanings()
        
        // Load details from Shared Repository (JSON)
        val allKanjiEntries = MochiApplication.kanjiRepository.getAllKanji()
        
        viewModel.allKanjiDetailsXml.clear()
        
        for (entry in allKanjiEntries) {
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            // Map JSON readings to UI Readings
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            
            val kanjiDetail = KanjiDetail(id, character, kanjiMeanings, readingsList)
            viewModel.allKanjiDetailsXml.add(kanjiDetail)
        }
    }

    private fun hiraganaToKatakana(s: String): String {
        return s.map { c ->
            if (c in '\u3041'..'\u3096') {
                (c + 0x60)
            } else {
                c
            }
        }.joinToString("")
    }

    private fun getFormattedReadings(kanji: KanjiDetail): String {
        val pronunciationMode = sharedPreferences.getString(PRONUNCIATION_PREF_KEY, "Hiragana")

        val onReadings = kanji.readings.filter { it.type == "on" }
        val kunReadings = kanji.readings.filter { it.type == "kun" }

        val selectedOn = if (viewModel.readingMode == "common") {
            onReadings.sortedByDescending { it.frequency }
        } else {
            onReadings.shuffled()
        }.take(2)

        val selectedKun = if (viewModel.readingMode == "common") {
            kunReadings.sortedByDescending { it.frequency }
        } else {
            kunReadings.shuffled()
        }.take(2)

        val onStrings = selectedOn.map {
            if (pronunciationMode == "Roman") KanaToRomaji.convert(it.value).uppercase() else hiraganaToKatakana(it.value)
        }
        val kunStrings = selectedKun.map {
            if (pronunciationMode == "Roman") KanaToRomaji.convert(it.value) else it.value
        }

        return (onStrings + kunStrings).joinToString("\n")
    }

    private fun displayQuestion() {
        if (viewModel.revisionList.isEmpty()) {
            startNewSet()
            return
        }

        viewModel.currentKanji = viewModel.revisionList.random()
        val progress = viewModel.kanjiProgress[viewModel.currentKanji]!!

        // Determine direction
        viewModel.currentDirection = when {
            !progress.normalSolved && !progress.reverseSolved -> if (Math.random() < 0.5) QuestionDirection.NORMAL else QuestionDirection.REVERSE
            !progress.normalSolved -> QuestionDirection.NORMAL
            else -> QuestionDirection.REVERSE
        }

        displayQuestionText()

        val answers = generateAnswers(viewModel.currentKanji)
        viewModel.currentAnswers = answers
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)

        viewModel.buttonColors.clear()
        repeat(4) { viewModel.buttonColors.add(R.color.button_background) }

        answerButtons.zip(answers).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_background))
            button.isEnabled = true

            if (viewModel.currentDirection == QuestionDirection.REVERSE) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            } else {
                val length = answerText.length
                val newSize = when {
                    length > 40 -> 10f
                    length > 20 -> 12f
                    else -> 14f
                }
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
            }
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
                getFormattedReadings(viewModel.currentKanji)
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

    private fun generateAnswers(correctKanji: KanjiDetail): List<String> {
        if (viewModel.currentDirection == QuestionDirection.NORMAL) {
            val correctButtonText: String
            if (viewModel.gameMode == "meaning") {
                viewModel.correctAnswer = correctKanji.meanings.firstOrNull() ?: ""
                correctButtonText = correctKanji.meanings.take(3).joinToString("\n")
            } else { // reading mode
                correctButtonText = getFormattedReadings(correctKanji)
                viewModel.correctAnswer = correctButtonText.lines().firstOrNull() ?: ""
            }

            if (viewModel.correctAnswer.isEmpty()) return listOf("", "", "", "")

            val incorrectPool = viewModel.allKanjiDetails
                .asSequence()
                .filter { it.id != correctKanji.id }
                .map { detail ->
                    if (viewModel.gameMode == "meaning") {
                        detail.meanings.take(3).joinToString("\n")
                    } else {
                        getFormattedReadings(detail)
                    }
                }
                .filter { it.isNotEmpty() }
                .distinct()
                .shuffled()
                .take(3)
                .toList()

            return (incorrectPool + correctButtonText).shuffled()
        } else {
            viewModel.correctAnswer = correctKanji.character
            val correctButtonText = correctKanji.character

            val incorrectPool = viewModel.allKanjiDetails
                .asSequence()
                .filter { it.id != correctKanji.id }
                .map { it.character }
                .distinct()
                .shuffled()
                .take(3)
                .toList()

            return (incorrectPool + correctButtonText).shuffled()
        }
    }

    private fun onAnswerClicked(button: Button) {
        val selectedAnswers = button.text.lines()

        val isCorrect = if (viewModel.currentDirection == QuestionDirection.NORMAL) {
            selectedAnswers.any { it.equals(viewModel.correctAnswer, ignoreCase = true) }
        } else {
            button.text.toString() == viewModel.correctAnswer
        }

        ScoreManager.saveScore(viewModel.currentKanji.character, isCorrect, ScoreType.RECOGNITION)

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        val buttonIndex = answerButtons.indexOf(button)

        if (isCorrect) {
            viewModel.buttonColors[buttonIndex] = R.color.answer_correct

            val progress = viewModel.kanjiProgress[viewModel.currentKanji]!!
            if (viewModel.currentDirection == QuestionDirection.NORMAL) progress.normalSolved = true
            else progress.reverseSolved = true

            if (progress.normalSolved && progress.reverseSolved) {
                viewModel.kanjiStatus[viewModel.currentKanji] = GameStatus.CORRECT
                viewModel.revisionList.remove(viewModel.currentKanji)
            } else {
                viewModel.kanjiStatus[viewModel.currentKanji] = GameStatus.PARTIAL
                viewModel.buttonColors[buttonIndex] = R.color.answer_neutral
            }
        } else {
            viewModel.buttonColors[buttonIndex] = R.color.answer_incorrect
            viewModel.kanjiStatus[viewModel.currentKanji] = GameStatus.INCORRECT
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
