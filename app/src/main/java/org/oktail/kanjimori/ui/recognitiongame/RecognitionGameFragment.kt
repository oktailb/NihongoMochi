package org.oktail.kanjimori.ui.recognitiongame

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.databinding.FragmentRecognitionGameBinding
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

data class Reading(val value: String, val type: String, val frequency: Int)
data class KanjiDetail(val id: String, val character: String, val meanings: List<String>, val readings: List<Reading>)
enum class GameStatus { NOT_ANSWERED, CORRECT, INCORRECT }

class RecognitionGameFragment : Fragment() {

    private var _binding: FragmentRecognitionGameBinding? = null
    private val binding get() = _binding!!
    private val args: RecognitionGameFragmentArgs by navArgs()

    private val allKanjiDetailsXml = mutableListOf<KanjiDetail>()
    private val allKanjiDetails = mutableListOf<KanjiDetail>()
    private var currentKanjiSet = mutableListOf<KanjiDetail>()
    private var revisionList = mutableListOf<KanjiDetail>()
    private val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    private var kanjiListPosition = 0
    private lateinit var currentKanji: KanjiDetail
    private lateinit var correctAnswer: String
    private lateinit var gameMode: String
    private lateinit var readingMode: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecognitionGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameMode = args.gameMode
        readingMode = args.readingMode
        val level = args.level
        val customWordList = args.customWordList?.toList() ?: emptyList()

        loadAllKanjiDetails()

        val kanjiForLevel = if (customWordList.isNotEmpty()) {
            customWordList
        } else {
            loadKanjiForLevel(level)
        }

        allKanjiDetails.clear()
        allKanjiDetails.addAll(allKanjiDetailsXml.filter { kanjiForLevel.contains(it.character) && it.meanings.isNotEmpty() })
        allKanjiDetails.shuffle()
        kanjiListPosition = 0

        if (allKanjiDetails.isNotEmpty()) {
            startNewSet()
        } else {
            Log.e("RecognitionGameFragment", "No kanji loaded for level: $level. Check XML files.")
            findNavController().popBackStack()
        }

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEach { button ->
            button.setOnClickListener { onAnswerClicked(button) }
        }
    }

    private fun startNewSet() {
        revisionList.clear()
        kanjiStatus.clear()

        if (kanjiListPosition >= allKanjiDetails.size) {
            findNavController().popBackStack()
            return
        }

        val nextSet = allKanjiDetails.drop(kanjiListPosition).take(10)
        kanjiListPosition += nextSet.size

        currentKanjiSet.clear()
        currentKanjiSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentKanjiSet.forEach { kanjiStatus[it] = GameStatus.NOT_ANSWERED }

        updateProgressBar()
        displayQuestion()
    }

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
            Log.e("RecognitionGameFragment", "meanings.xml not found for current locale. Fallback should occur.", e)
        } catch (e: XmlPullParserException) {
            Log.e("RecognitionGameFragment", "Error parsing meanings.xml", e)
        } catch (e: IOException) {
            Log.e("RecognitionGameFragment", "IO error reading meanings.xml", e)
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

    private fun displayQuestion() {
        if (revisionList.isEmpty()) {
            startNewSet()
            return
        }

        currentKanji = revisionList.random()
        binding.textKanjiToGuess.text = currentKanji.character

        val answers = generateAnswers(currentKanji)
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)

        answerButtons.zip(answers).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(Color.LTGRAY)
            button.isEnabled = true
        }
    }

    private fun generateAnswers(correctKanji: KanjiDetail): List<String> {
        val correctButtonText: String
        if (gameMode == "meaning") {
            correctAnswer = correctKanji.meanings.firstOrNull() ?: ""
            correctButtonText = correctKanji.meanings.take(3).joinToString("\n")
        } else { // reading mode
            val readings = if (readingMode == "common") {
                correctKanji.readings.sortedByDescending { it.frequency }
            } else {
                correctKanji.readings.shuffled()
            }
            correctAnswer = readings.firstOrNull()?.value ?: ""
            correctButtonText = readings.take(3).map { it.value }.joinToString("\n")
        }

        if (correctAnswer.isEmpty()) return listOf("", "", "", "")

        val incorrectPool = allKanjiDetails
            .asSequence()
            .filter { it.id != correctKanji.id }
            .map { detail ->
                if (gameMode == "meaning") {
                    detail.meanings.take(3).joinToString("\n")
                } else {
                    detail.readings.shuffled().take(3).map { it.value }.joinToString("\n")
                }
            }
            .filter { it.isNotEmpty() }
            .distinct()
            .shuffled()
            .take(3)
            .toList()

        return (incorrectPool + correctButtonText).shuffled()
    }

    private fun onAnswerClicked(button: Button) {
        val selectedAnswers = button.text.lines()
        val isCorrect = selectedAnswers.any { it == correctAnswer }

        ScoreManager.saveScore(requireContext(), currentKanji.character, isCorrect)

        if (isCorrect) {
            button.setBackgroundColor(Color.GREEN)
            kanjiStatus[currentKanji] = GameStatus.CORRECT
            revisionList.remove(currentKanji)
        } else {
            button.setBackgroundColor(Color.RED)
            kanjiStatus[currentKanji] = GameStatus.INCORRECT
        }

        updateProgressBar()

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEach { it.isEnabled = false }

        Handler(Looper.getMainLooper()).postDelayed({
            displayQuestion()
        }, 1000)
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
