package org.oktail.kanjimori.ui.writinggame

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.data.ScoreManager.ScoreType
import org.oktail.kanjimori.databinding.FragmentWritingGameBinding
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.text.Normalizer
import kotlin.math.max

data class Reading(val value: String, val type: String, val frequency: Int)
data class KanjiDetail(val id: String, val character: String, val meanings: List<String>, val readings: List<Reading>)
data class KanjiProgress(var meaningSolved: Boolean = false, var readingSolved: Boolean = false)
enum class GameStatus { NOT_ANSWERED, PARTIAL, CORRECT, INCORRECT }
enum class QuestionType { MEANING, READING }

class WritingGameFragment : Fragment() {

    private var _binding: FragmentWritingGameBinding? = null
    private val binding get() = _binding!!
    private val args: WritingGameFragmentArgs by navArgs()

    private val allKanjiDetailsXml = mutableListOf<KanjiDetail>()
    private val allKanjiDetails = mutableListOf<KanjiDetail>()
    private var currentKanjiSet = mutableListOf<KanjiDetail>()
    private var revisionList = mutableListOf<KanjiDetail>()
    private val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    private val kanjiProgress = mutableMapOf<KanjiDetail, KanjiProgress>()
    private var kanjiListPosition = 0
    private lateinit var currentKanji: KanjiDetail
    private var currentQuestionType: QuestionType = QuestionType.MEANING
    private var isAnswerProcessing = false

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (s == null) return
            // Only convert to kana if we are in READING mode
            if (currentQuestionType == QuestionType.READING) {
                val input = s.toString()
                val replacement = RomajiToKana.checkReplacement(input)
                if (replacement != null) {
                    val (suffixLen, kana) = replacement
                    val start = input.length - suffixLen
                    val end = input.length
                    
                    // Prevent infinite loop by removing listener during modification
                    binding.editTextAnswer.removeTextChangedListener(this)
                    s.replace(start, end, kana)
                    binding.editTextAnswer.addTextChangedListener(this)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWritingGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fix: Handle nullable args.level
        val level = args.level ?: ""

        loadAllKanjiDetails()
        val kanjiForLevel = loadKanjiForLevel(level)

        allKanjiDetails.clear()
        allKanjiDetails.addAll(allKanjiDetailsXml.filter { kanjiForLevel.contains(it.character) })
        allKanjiDetails.shuffle()
        kanjiListPosition = 0

        if (allKanjiDetails.isNotEmpty()) {
            startNewSet()
        } else {
            Log.e("WritingGameFragment", "No kanji loaded for level: $level. Check XML files.")
            findNavController().popBackStack()
        }

        binding.buttonSubmitAnswer.setOnClickListener { checkAnswer() }
        binding.editTextAnswer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer()
                true
            } else {
                false
            }
        }
        
        binding.editTextAnswer.addTextChangedListener(textWatcher)
    }

    private fun startNewSet() {
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()

        if (kanjiListPosition >= allKanjiDetails.size) {
            findNavController().popBackStack()
            return
        }

        val nextSet = allKanjiDetails.drop(kanjiListPosition).take(10)
        kanjiListPosition += nextSet.size

        currentKanjiSet.clear()
        currentKanjiSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentKanjiSet.forEach {
            kanjiStatus[it] = GameStatus.NOT_ANSWERED
            kanjiProgress[it] = KanjiProgress()
        }

        updateProgressBar()
        displayQuestion()
    }

    private fun displayQuestion() {
        if (revisionList.isEmpty()) {
            startNewSet()
            return
        }

        isAnswerProcessing = false
        currentKanji = revisionList.random()
        binding.textKanjiToGuess.text = currentKanji.character
        binding.editTextAnswer.text.clear()
        // Ensure editable state
        binding.editTextAnswer.isEnabled = true
        binding.buttonSubmitAnswer.isEnabled = true
        binding.buttonSubmitAnswer.setBackgroundColor(resources.getColor(R.color.leaf_green, null))
        binding.layoutCorrectionFeedback.visibility = View.INVISIBLE

        // Determine question type based on what is missing
        val progress = kanjiProgress[currentKanji]!!
        currentQuestionType = when {
            !progress.meaningSolved && !progress.readingSolved -> if (Math.random() < 0.5) QuestionType.MEANING else QuestionType.READING
            !progress.meaningSolved -> QuestionType.MEANING
            else -> QuestionType.READING // reading not solved
        }

        binding.textQuestionLabel.text = if (currentQuestionType == QuestionType.MEANING) {
            getString(R.string.game_writing_label_meaning)
        } else {
            getString(R.string.game_writing_label_reading)
        }

        // request focus and show keyboard if possible
        binding.editTextAnswer.requestFocus()
    }

    private fun checkAnswer() {
        if (isAnswerProcessing) return
        isAnswerProcessing = true

        val userAnswer = binding.editTextAnswer.text.toString().trim()
        val isCorrect = if (currentQuestionType == QuestionType.MEANING) {
            checkMeaning(userAnswer)
        } else {
            checkReading(userAnswer)
        }

        ScoreManager.saveScore(requireContext(), currentKanji.character, isCorrect, ScoreType.WRITING)

        // Do NOT disable editTextAnswer to keep keyboard open
        binding.buttonSubmitAnswer.isEnabled = false

        if (isCorrect) {
            val progress = kanjiProgress[currentKanji]!!
            if (currentQuestionType == QuestionType.MEANING) progress.meaningSolved = true
            else progress.readingSolved = true

            if (progress.meaningSolved && progress.readingSolved) {
                // Fully solved
                binding.buttonSubmitAnswer.setBackgroundColor(Color.GREEN)
                kanjiStatus[currentKanji] = GameStatus.CORRECT
                revisionList.remove(currentKanji)
            } else {
                // Partially solved
                binding.buttonSubmitAnswer.setBackgroundColor(Color.parseColor("#FFA500")) // Orange
                kanjiStatus[currentKanji] = GameStatus.PARTIAL
            }

            Handler(Looper.getMainLooper()).postDelayed({
                displayQuestion()
            }, 1000)

        } else {
            binding.buttonSubmitAnswer.setBackgroundColor(Color.RED)
            kanjiStatus[currentKanji] = GameStatus.INCORRECT

            // Setup Correction Feedback
            var delayMs = 3000L
            
            if (currentQuestionType == QuestionType.MEANING) {
                // Show simple meaning text
                binding.textCorrectionSimple.visibility = View.VISIBLE
                binding.layoutReadingsColumns.visibility = View.GONE
                
                val meaningsText = currentKanji.meanings.joinToString(", ")
                binding.textCorrectionSimple.text = meaningsText
                
                // Calculate delay based on length
                delayMs = max(2000L, meaningsText.length * 100L)
            } else {
                // Show Readings Columns
                binding.textCorrectionSimple.visibility = View.GONE
                binding.layoutReadingsColumns.visibility = View.VISIBLE
                
                val onReadings = currentKanji.readings.filter { it.type == "on" }.joinToString("\n") { hiraganaToKatakana(it.value) }
                val kunReadings = currentKanji.readings.filter { it.type == "kun" }.joinToString("\n") { it.value }
                
                binding.textReadingsOn.text = if (onReadings.isNotEmpty()) onReadings else "-"
                binding.textReadingsKun.text = if (kunReadings.isNotEmpty()) kunReadings else "-"
                
                // Calculate delay
                val totalLength = onReadings.length + kunReadings.length
                delayMs = max(2000L, totalLength * 150L)
            }
            
            // Limit max delay to avoid waiting too long (e.g., 6 seconds max)
            delayMs = delayMs.coerceAtMost(6000L)
            
            binding.layoutCorrectionFeedback.visibility = View.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({
                displayQuestion()
            }, delayMs)
        }

        updateProgressBar()
    }

    private fun unaccent(s: String): String {
        val temp = Normalizer.normalize(s, Normalizer.Form.NFD)
        return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(temp, "")
    }

    private fun checkMeaning(answer: String): Boolean {
        val normalizedAnswer = unaccent(answer)
        // is the answer contained in any of the meanings (case insensitive, accent insensitive)
        return currentKanji.meanings.any { unaccent(it).equals(normalizedAnswer, ignoreCase = true) }
    }
    
    // Converts hiragana characters to katakana
    private fun hiraganaToKatakana(s: String): String {
        return s.map { c ->
            if (c in '\u3041'..'\u3096') {
                (c + 0x60)
            } else {
                c
            }
        }.joinToString("")
    }
    
    // Converts katakana characters to hiragana
    private fun katakanaToHiragana(s: String): String {
        return s.map { c ->
            if (c in '\u30A1'..'\u30F6') {
                (c - 0x60)
            } else {
                c
            }
        }.joinToString("")
    }

    private fun checkReading(answer: String): Boolean {
        // Normalize everything to hiragana for comparison
        val normalizedAnswer = katakanaToHiragana(answer)
        
        return currentKanji.readings.any { 
            val normalizedReading = katakanaToHiragana(it.value)
            normalizedReading == normalizedAnswer 
        }
    }

    private fun updateProgressBar() {
        val progressIndicators = (0 until binding.progressBarGame.childCount).map {
            binding.progressBarGame.getChildAt(it) as ImageView
        }

        for (i in 0 until 10) {
            if (i < currentKanjiSet.size) {
                val kanji = currentKanjiSet[i]
                val status = kanjiStatus[kanji]
                val indicator = progressIndicators[i]
                indicator.visibility = View.VISIBLE
                
                // Reset basic state
                indicator.clearColorFilter()
                
                when (status) {
                    GameStatus.CORRECT -> indicator.setImageResource(android.R.drawable.presence_online)
                    GameStatus.INCORRECT -> indicator.setImageResource(android.R.drawable.ic_delete)
                    GameStatus.PARTIAL -> {
                        // Use a clock icon to symbolize "in progress" / hourglass, tinted Orange
                        indicator.setImageResource(android.R.drawable.ic_menu_recent_history)
                        indicator.setColorFilter(Color.parseColor("#FFA500"))
                    }
                    else -> indicator.setImageResource(android.R.drawable.checkbox_off_background)
                }
            } else {
                progressIndicators[i].visibility = View.GONE
            }
        }
    }

    // --- Loading Data (Similar to RecognitionGameFragment) ---

    private fun loadKanjiForLevel(levelName: String): List<String> {
        val allKanji = mutableMapOf<String, String>()
        val levelKanjiIds = mutableListOf<String>()
        val parser = resources.getXml(R.xml.kanji_levels)

        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.name == "kanji") {
                        val id = parser.getAttributeValue(null, "id")
                        val character = parser.nextText()
                        if (id != null) {
                            allKanji[id] = character
                        }
                    } else if (parser.name == "level" && parser.getAttributeValue(null, "name") == levelName) {
                        parseKanjiIdsForLevel(parser, levelKanjiIds)
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return levelKanjiIds.mapNotNull { allKanji[it] }
    }

    private fun parseKanjiIdsForLevel(parser: XmlPullParser, levelKanjiIds: MutableList<String>) {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_TAG || parser.name != "level") {
            if (eventType == XmlPullParser.START_TAG && parser.name == "kanji_id") {
                levelKanjiIds.add(parser.nextText())
            }
            eventType = parser.next()
        }
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
            Log.e("WritingGameFragment", "meanings.xml not found", e)
        } catch (e: XmlPullParserException) {
            Log.e("WritingGameFragment", "Error parsing meanings.xml", e)
        } catch (e: IOException) {
            Log.e("WritingGameFragment", "IO error reading meanings.xml", e)
        }
        return meaningsMap
    }

    private fun loadAllKanjiDetails() {
        val meanings = loadMeanings()
        val parser = resources.getXml(R.xml.kanji_details)

        try {
            var eventType = parser.eventType
            var currentKanjiDetail: KanjiDetail? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "kanji") {
                            val id = parser.getAttributeValue(null, "id")
                            val character = parser.getAttributeValue(null, "character")
                            if (id != null && character != null) {
                                val kanjiMeanings = meanings[id] ?: emptyList()
                                currentKanjiDetail = KanjiDetail(id, character, kanjiMeanings, mutableListOf())
                                allKanjiDetailsXml.add(currentKanjiDetail)
                            }
                        } else if (parser.name == "reading" && currentKanjiDetail != null) {
                            val type = parser.getAttributeValue(null, "type")
                            val frequency = parser.getAttributeValue(null, "frequency").toInt()
                            val value = parser.nextText()
                            (currentKanjiDetail.readings as MutableList).add(Reading(value, type, frequency))
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "kanji") {
                            currentKanjiDetail = null
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}