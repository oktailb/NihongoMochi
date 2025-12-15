package org.oktail.kanjimori.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.KanjiScore
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.databinding.FragmentResultsBinding
import org.xmlpull.v1.XmlPullParser
import org.oktail.kanjimori.data.KanjiCategoryProgress

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        updateAllPercentages()

        return root
    }

    private fun updateAllPercentages() {
        val allKanji = loadAllKanji()
        val levelInfos = initializeLevelInfos()

        for (info in levelInfos) {
            val charactersForLevel = when (info.xmlName) {
                "Hiragana" -> loadCharacters(R.xml.hiragana)
                "Katakana" -> loadCharacters(R.xml.katakana)
                else -> getKanjiForLevel(info.xmlName, allKanji)
            }
            val masteryPercentage = calculateMasteryPercentage(charactersForLevel)
            updateCategoryUI(info, masteryPercentage)
        }
    }

    private fun initializeLevelInfos(): List<LevelInfo> {
        return listOf(
            LevelInfo("JLPT N5", "N5"),
            LevelInfo("JLPT N4", "N4"),
            LevelInfo("JLPT N3", "N3"),
            LevelInfo("JLPT N2", "N2"),
            LevelInfo("JLPT N1", "N1"),
            LevelInfo("Grade 1", "Grade 1"),
            LevelInfo("Grade 2", "Grade 2"),
            LevelInfo("Grade 3", "Grade 3"),
            LevelInfo("Grade 4", "Grade 4"),
            LevelInfo("Grade 5", "Grade 5"),
            LevelInfo("Grade 6", "Grade 6"),
            LevelInfo("Collège", "Grade 7"),
            LevelInfo("Lycée", "Grade 8"),
            LevelInfo("Hiragana", "Hiragana"),
            LevelInfo("Katakana", "Katakana")
        )
    }

    private fun loadCharacters(resourceId: Int): List<String> {
        val characterList = mutableListOf<String>()
        val parser = resources.getXml(resourceId)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "character") {
                    characterList.add(parser.nextText())
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return characterList
    }

    private fun loadAllKanji(): Map<String, String> {
        val allKanji = mutableMapOf<String, String>()
        val parser = resources.getXml(R.xml.kanji_levels)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "kanji") {
                    val id = parser.getAttributeValue(null, "id")
                    val character = parser.nextText()
                    if (id != null) {
                        allKanji[id] = character
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return allKanji
    }

    private fun getKanjiForLevel(levelName: String, allKanji: Map<String, String>): List<String> {
        val levelKanjiIds = mutableListOf<String>()
        val parser = resources.getXml(R.xml.kanji_levels)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "level" && parser.getAttributeValue(null, "name") == levelName) {
                    parseKanjiIdsForLevel(parser, levelKanjiIds)
                    break // Exit after finding the level
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

    private fun calculateMasteryPercentage(characterList: List<String>): Double {
        if (characterList.isEmpty()) return 0.0

        val totalMasteryPoints = characterList.sumOf { character ->
            val score = ScoreManager.getScore(requireContext(), character)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = characterList.size * 10.0
        if (maxPossiblePoints == 0.0) return 0.0

        return (totalMasteryPoints / maxPossiblePoints) * 100
    }

    private fun updateCategoryUI(info: LevelInfo, percentage: Double) {
        val percentageInt = percentage.toInt()
        when (info.name) {
            "Hiragana" -> {
                binding.progressRecognitionHiragana.progress = percentageInt
                binding.titleRecognitionHiragana.text = getString(R.string.results_hiragana, percentageInt)
            }
            "Katakana" -> {
                binding.progressRecognitionKatakana.progress = percentageInt
                binding.titleRecognitionKatakana.text = getString(R.string.results_katakana, percentageInt)
            }
            "JLPT N5" -> {
                binding.progressRecognitionN5.progress = percentageInt
                binding.titleRecognitionN5.text = getString(R.string.results_jlpt_n5, percentageInt)
            }
            "JLPT N4" -> {
                binding.progressRecognitionN4.progress = percentageInt
                binding.titleRecognitionN4.text = getString(R.string.results_jlpt_n4, percentageInt)
            }
            "JLPT N3" -> {
                binding.progressRecognitionN3.progress = percentageInt
                binding.titleRecognitionN3.text = getString(R.string.results_jlpt_n3, percentageInt)
            }
            "JLPT N2" -> {
                binding.progressRecognitionN2.progress = percentageInt
                binding.titleRecognitionN2.text = getString(R.string.results_jlpt_n2, percentageInt)
            }
            "JLPT N1" -> {
                binding.progressRecognitionN1.progress = percentageInt
                binding.titleRecognitionN1.text = getString(R.string.results_jlpt_n1, percentageInt)
            }
            "Grade 1" -> {
                binding.progressRecognitionGrade1.progress = percentageInt
                binding.titleRecognitionGrade1.text = getString(R.string.results_grade_1, percentageInt)
            }
            "Grade 2" -> {
                binding.progressRecognitionGrade2.progress = percentageInt
                binding.titleRecognitionGrade2.text = getString(R.string.results_grade_2, percentageInt)
            }
            "Grade 3" -> {
                binding.progressRecognitionGrade3.progress = percentageInt
                binding.titleRecognitionGrade3.text = getString(R.string.results_grade_3, percentageInt)
            }
            "Grade 4" -> {
                binding.progressRecognitionGrade4.progress = percentageInt
                binding.titleRecognitionGrade4.text = getString(R.string.results_grade_4, percentageInt)
            }
            "Grade 5" -> {
                binding.progressRecognitionGrade5.progress = percentageInt
                binding.titleRecognitionGrade5.text = getString(R.string.results_grade_5, percentageInt)
            }
            "Grade 6" -> {
                binding.progressRecognitionGrade6.progress = percentageInt
                binding.titleRecognitionGrade6.text = getString(R.string.results_grade_6, percentageInt)
            }
            "Collège" -> {
                binding.progressRecognitionCollege.progress = percentageInt
                binding.titleRecognitionCollege.text = getString(R.string.results_college, percentageInt)
            }
            "Lycée" -> {
                binding.progressRecognitionLycee.progress = percentageInt
                binding.titleRecognitionLycee.text = getString(R.string.results_high_school, percentageInt)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class LevelInfo(val name: String, val xmlName: String)
}
