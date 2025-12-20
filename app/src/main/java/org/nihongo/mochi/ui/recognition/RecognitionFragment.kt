package org.nihongo.mochi.ui.recognition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType
import org.nihongo.mochi.databinding.FragmentRecognitionBinding
import org.xmlpull.v1.XmlPullParser

data class LevelInfo(val button: Button, val xmlName: String, val stringResId: Int)

class RecognitionFragment : Fragment() {

    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!

    private lateinit var levelInfos: List<LevelInfo>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeLevelInfos()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateAllButtonPercentages()
    }

    private fun initializeLevelInfos() {
        levelInfos = listOf(
            LevelInfo(binding.buttonN5, "N5", R.string.level_n5),
            LevelInfo(binding.buttonN4, "N4", R.string.level_n4),
            LevelInfo(binding.buttonN3, "N3", R.string.level_n3),
            LevelInfo(binding.buttonN2, "N2", R.string.level_n2),
            LevelInfo(binding.buttonN1, "N1", R.string.level_n1),
            LevelInfo(binding.buttonClass1, "Grade 1", R.string.level_class_1),
            LevelInfo(binding.buttonClass2, "Grade 2", R.string.level_class_2),
            LevelInfo(binding.buttonClass3, "Grade 3", R.string.level_class_3),
            LevelInfo(binding.buttonClass4, "Grade 4", R.string.level_class_4),
            LevelInfo(binding.buttonClass5, "Grade 5", R.string.level_class_5),
            LevelInfo(binding.buttonClass6, "Grade 6", R.string.level_class_6),
            LevelInfo(binding.buttonTest4, "Grade 7", R.string.level_high_school_1),
            LevelInfo(binding.buttonTest3, "Grade 8", R.string.level_high_school_2),
            LevelInfo(binding.buttonTestPre2, "Grade 9", R.string.level_high_school_3),
            LevelInfo(binding.buttonTest2, "Grade 10", R.string.level_high_school_4),
            LevelInfo(binding.buttonHiragana, "Hiragana", R.string.level_hiragana),
            LevelInfo(binding.buttonKatakana, "Katakana", R.string.level_katakana)
        )
    }

    private fun updateAllButtonPercentages() {
        val allKanji = loadAllKanji()

        for (info in levelInfos) {
            val charactersForLevel = when (info.xmlName) {
                "Hiragana" -> loadCharacters(R.xml.hiragana)
                "Katakana" -> loadCharacters(R.xml.katakana)
                else -> getKanjiForLevel(info.xmlName, allKanji)
            }
            val masteryPercentage = calculateMasteryPercentage(charactersForLevel)
            updateButtonText(info, masteryPercentage)
        }
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
            val score = ScoreManager.getScore(requireContext(), character, ScoreType.RECOGNITION)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = characterList.size * 10.0
        if (maxPossiblePoints == 0.0) return 0.0

        return (totalMasteryPoints / maxPossiblePoints) * 100
    }

    private fun updateButtonText(info: LevelInfo, percentage: Double) {
        val displayName = getString(info.stringResId)
        val formattedPercentage = "${percentage.toInt()}%"
        info.button.text = "$displayName\n$formattedPercentage"
    }

    private fun setupClickListeners() {
        for (info in levelInfos) {
            info.button.setOnClickListener { 
                when (info.xmlName) {
                    "Hiragana" -> findNavController().navigate(R.id.action_nav_recognition_to_nav_hiragana)
                    "Katakana" -> findNavController().navigate(R.id.action_nav_recognition_to_nav_katakana)
                    else -> navigateToRecap(info.xmlName)
                }
            }
        }
    }

    private fun navigateToRecap(levelXmlName: String) {
        val bundle = Bundle().apply { putString("level", levelXmlName) }
        findNavController().navigate(R.id.action_nav_recognition_to_game_recap, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}