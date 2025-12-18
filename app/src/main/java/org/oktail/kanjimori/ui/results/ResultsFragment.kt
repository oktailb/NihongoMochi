package org.oktail.kanjimori.ui.results

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.databinding.FragmentResultsBinding
import org.xmlpull.v1.XmlPullParser

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCollapsibleSections()
        updateAllPercentages()
    }

    private fun setupCollapsibleSections() {
        // Main Sections
        setupCollapsibleSection(binding.headerRecognitionMain, binding.arrowRecognitionMain, binding.containerRecognitionMain, "recognition_main_expanded")
        setupCollapsibleSection(binding.headerReadingMain, binding.arrowReadingMain, binding.containerReadingMain, "reading_main_expanded")
        setupCollapsibleSection(binding.headerWritingMain, binding.arrowWritingMain, binding.containerWritingMain, "writing_main_expanded")

        // Recognition Sub-sections
        setupCollapsibleSection(binding.headerRecognitionKanas, binding.arrowRecognitionKanas, binding.containerRecognitionKanas, "recognition_kanas_expanded")
        setupCollapsibleSection(binding.headerRecognitionJlpt, binding.arrowRecognitionJlpt, binding.containerRecognitionJlpt, "recognition_jlpt_expanded")
        setupCollapsibleSection(binding.headerRecognitionSchool, binding.arrowRecognitionSchool, binding.containerRecognitionSchool, "recognition_school_expanded")

        // Reading Sub-sections
        setupCollapsibleSection(binding.headerReadingJlpt, binding.arrowReadingJlpt, binding.containerReadingJlpt, "reading_jlpt_expanded")
        setupCollapsibleSection(binding.headerReadingFreq, binding.arrowReadingFreq, binding.containerReadingFreq, "reading_freq_expanded")

        // Writing Sub-sections
        setupCollapsibleSection(binding.headerWritingJlpt, binding.arrowWritingJlpt, binding.containerWritingJlpt, "writing_jlpt_expanded")
        setupCollapsibleSection(binding.headerWritingSchool, binding.arrowWritingSchool, binding.containerWritingSchool, "writing_school_expanded")
    }

    private fun setupCollapsibleSection(header: View, arrow: ImageView, container: ViewGroup, preferenceKey: String) {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val isExpanded = sharedPreferences.getBoolean(preferenceKey, true)

        container.visibility = if (isExpanded) View.VISIBLE else View.GONE
        arrow.rotation = if (isExpanded) 0f else -90f

        header.setOnClickListener {
            val newExpandedState = container.visibility == View.GONE
            container.visibility = if (newExpandedState) View.VISIBLE else View.GONE
            arrow.animate().rotation(if (newExpandedState) 0f else -90f).start()

            with(sharedPreferences.edit()) {
                putBoolean(preferenceKey, newExpandedState)
                apply()
            }
        }
    }

    private fun updateAllPercentages() {
        val allKanji = loadAllKanji()
        val levelInfos = initializeLevelInfos()

        for (info in levelInfos) {
            // Determine score type for calculation
            val scoreType = when {
                info.name.startsWith("Reading") -> ScoreManager.ScoreType.READING
                info.name.startsWith("Writing") -> ScoreManager.ScoreType.WRITING
                else -> ScoreManager.ScoreType.RECOGNITION
            }

            val charactersForLevel = when {
                info.xmlName == "Hiragana" -> loadCharacters(R.xml.hiragana)
                info.xmlName == "Katakana" -> loadCharacters(R.xml.katakana)
                info.xmlName.startsWith("bccwj_wordlist_") -> loadWordsFromXml(info.xmlName)
                else -> getKanjiForLevel(info.xmlName, allKanji)
            }

            // For user list, we calculate differently: mastered vs encountered
            val percentage = if (info.xmlName == "user_list") {
                calculateUserListPercentage(scoreType)
            } else {
                calculateMasteryPercentage(charactersForLevel, scoreType)
            }

            updateCategoryUI(info, percentage)
        }
    }

    private fun calculateUserListPercentage(scoreType: ScoreManager.ScoreType): Double {
        val scores = ScoreManager.getAllScores(requireContext(), scoreType)
        if (scores.isEmpty()) return 0.0

        val totalEncountered = scores.size
        val mastered = scores.count { (_, score) -> (score.successes - score.failures) >= 10 }

        return if (totalEncountered > 0) {
            (mastered.toDouble() / totalEncountered.toDouble()) * 100.0
        } else {
            0.0
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
            LevelInfo("Katakana", "Katakana"),
            LevelInfo("Reading User", "user_list"),
            LevelInfo("Reading N5", "reading_n5"),
            LevelInfo("Reading N4", "reading_n4"),
            LevelInfo("Reading N3", "reading_n3"),
            LevelInfo("Reading N2", "reading_n2"),
            LevelInfo("Reading N1", "reading_n1"),
            LevelInfo("Reading 1000", "bccwj_wordlist_1000"),
            LevelInfo("Reading 2000", "bccwj_wordlist_2000"),
            LevelInfo("Reading 3000", "bccwj_wordlist_3000"),
            LevelInfo("Reading 4000", "bccwj_wordlist_4000"),
            LevelInfo("Reading 5000", "bccwj_wordlist_5000"),
            LevelInfo("Reading 6000", "bccwj_wordlist_6000"),
            LevelInfo("Reading 7000", "bccwj_wordlist_7000"),
            LevelInfo("Reading 8000", "bccwj_wordlist_8000"),
            LevelInfo("Writing User", "user_list"),
            LevelInfo("Writing JLPT N5", "N5"),
            LevelInfo("Writing JLPT N4", "N4"),
            LevelInfo("Writing JLPT N3", "N3"),
            LevelInfo("Writing JLPT N2", "N2"),
            LevelInfo("Writing JLPT N1", "N1"),
            LevelInfo("Writing Grade 1", "Grade 1"),
            LevelInfo("Writing Grade 2", "Grade 2"),
            LevelInfo("Writing Grade 3", "Grade 3"),
            LevelInfo("Writing Grade 4", "Grade 4"),
            LevelInfo("Writing Grade 5", "Grade 5"),
            LevelInfo("Writing Grade 6", "Grade 6"),
            LevelInfo("Writing Collège", "Grade 7"),
            LevelInfo("Writing Lycée", "Grade 8")
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

    private fun loadWordsFromXml(fileName: String): List<String> {
        val wordList = mutableListOf<String>()
        val resourceId = resources.getIdentifier(fileName, "xml", requireContext().packageName)
        if (resourceId == 0) return emptyList()

        val parser = resources.getXml(resourceId)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "word") {
                    wordList.add(parser.nextText())
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return wordList
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

    private fun calculateMasteryPercentage(characterList: List<String>, scoreType: ScoreManager.ScoreType): Double {
        if (characterList.isEmpty()) return 0.0

        val totalMasteryPoints = characterList.sumOf { character ->
            val score = ScoreManager.getScore(requireContext(), character, scoreType)
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
            "Reading User" -> {
                binding.progressReadingUser.progress = percentageInt
                binding.titleReadingUser.text = getString(R.string.reading_user_list) + " - $percentageInt%"
            }
            "Reading N5" -> {
                binding.progressReadingN5.progress = percentageInt
                binding.titleReadingN5.text = getString(R.string.results_jlpt_n5, percentageInt)
            }
            "Reading N4" -> {
                binding.progressReadingN4.progress = percentageInt
                binding.titleReadingN4.text = getString(R.string.results_jlpt_n4, percentageInt)
            }
            "Reading N3" -> {
                binding.progressReadingN3.progress = percentageInt
                binding.titleReadingN3.text = getString(R.string.results_jlpt_n3, percentageInt)
            }
            "Reading N2" -> {
                binding.progressReadingN2.progress = percentageInt
                binding.titleReadingN2.text = getString(R.string.results_jlpt_n2, percentageInt)
            }
            "Reading N1" -> {
                binding.progressReadingN1.progress = percentageInt
                binding.titleReadingN1.text = getString(R.string.results_jlpt_n1, percentageInt)
            }
            "Reading 1000" -> {
                binding.progressReading1000.progress = percentageInt
                binding.titleReading1000.text = "1000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 2000" -> {
                binding.progressReading2000.progress = percentageInt
                binding.titleReading2000.text = "2000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 3000" -> {
                binding.progressReading3000.progress = percentageInt
                binding.titleReading3000.text = "3000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 4000" -> {
                binding.progressReading4000.progress = percentageInt
                binding.titleReading4000.text = "4000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 5000" -> {
                binding.progressReading5000.progress = percentageInt
                binding.titleReading5000.text = "5000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 6000" -> {
                binding.progressReading6000.progress = percentageInt
                binding.titleReading6000.text = "6000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 7000" -> {
                binding.progressReading7000.progress = percentageInt
                binding.titleReading7000.text = "7000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 8000" -> {
                binding.progressReading8000.progress = percentageInt
                binding.titleReading8000.text = "8000 mots les plus fréquents - $percentageInt%"
            }
            "Writing User" -> {
                binding.progressWritingUser.progress = percentageInt
                binding.titleWritingUser.text = getString(R.string.writing_user_lists) + " - $percentageInt%"
            }
            "Writing JLPT N5" -> {
                binding.progressWritingN5.progress = percentageInt
                binding.titleWritingN5.text = getString(R.string.results_jlpt_n5, percentageInt)
            }
            "Writing JLPT N4" -> {
                binding.progressWritingN4.progress = percentageInt
                binding.titleWritingN4.text = getString(R.string.results_jlpt_n4, percentageInt)
            }
            "Writing JLPT N3" -> {
                binding.progressWritingN3.progress = percentageInt
                binding.titleWritingN3.text = getString(R.string.results_jlpt_n3, percentageInt)
            }
            "Writing JLPT N2" -> {
                binding.progressWritingN2.progress = percentageInt
                binding.titleWritingN2.text = getString(R.string.results_jlpt_n2, percentageInt)
            }
            "Writing JLPT N1" -> {
                binding.progressWritingN1.progress = percentageInt
                binding.titleWritingN1.text = getString(R.string.results_jlpt_n1, percentageInt)
            }
            "Writing Grade 1" -> {
                binding.progressWritingGrade1.progress = percentageInt
                binding.titleWritingGrade1.text = getString(R.string.results_grade_1, percentageInt)
            }
            "Writing Grade 2" -> {
                binding.progressWritingGrade2.progress = percentageInt
                binding.titleWritingGrade2.text = getString(R.string.results_grade_2, percentageInt)
            }
            "Writing Grade 3" -> {
                binding.progressWritingGrade3.progress = percentageInt
                binding.titleWritingGrade3.text = getString(R.string.results_grade_3, percentageInt)
            }
            "Writing Grade 4" -> {
                binding.progressWritingGrade4.progress = percentageInt
                binding.titleWritingGrade4.text = getString(R.string.results_grade_4, percentageInt)
            }
            "Writing Grade 5" -> {
                binding.progressWritingGrade5.progress = percentageInt
                binding.titleWritingGrade5.text = getString(R.string.results_grade_5, percentageInt)
            }
            "Writing Grade 6" -> {
                binding.progressWritingGrade6.progress = percentageInt
                binding.titleWritingGrade6.text = getString(R.string.results_grade_6, percentageInt)
            }
            "Writing Collège" -> {
                binding.progressWritingCollege.progress = percentageInt
                binding.titleWritingCollege.text = getString(R.string.results_college, percentageInt)
            }
            "Writing Lycée" -> {
                binding.progressWritingLycee.progress = percentageInt
                binding.titleWritingLycee.text = getString(R.string.results_high_school, percentageInt)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class LevelInfo(val name: String, val xmlName: String)
}
