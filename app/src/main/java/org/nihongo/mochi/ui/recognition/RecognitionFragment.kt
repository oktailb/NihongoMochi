package org.nihongo.mochi.ui.recognition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType
import org.nihongo.mochi.databinding.FragmentRecognitionBinding
import org.nihongo.mochi.domain.kana.KanaType

data class LevelInfo(val button: Button, val levelKey: String, val stringResId: Int)

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
        // levelKey maps to what we will ask the repository.
        // For XML legacy, it was the "name" attribute in kanji_levels.xml.
        // For JSON, we will adapt in getKanjiForLevel.
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
            // Mapping for higher grades might need adjustment depending on JSON content.
            // Assuming continuity 7, 8, 9, 10 for middle/high school.
            LevelInfo(binding.buttonTest4, "Grade 7", R.string.level_high_school_1),
            LevelInfo(binding.buttonTest3, "Grade 8", R.string.level_high_school_2),
            LevelInfo(binding.buttonTestPre2, "Grade 9", R.string.level_high_school_3),
            LevelInfo(binding.buttonTest2, "Grade 10", R.string.level_high_school_4),
            LevelInfo(binding.buttonHiragana, "Hiragana", R.string.level_hiragana),
            LevelInfo(binding.buttonKatakana, "Katakana", R.string.level_katakana)
        )
    }

    private fun updateAllButtonPercentages() {
        for (info in levelInfos) {
            val charactersForLevel = getCharactersForLevel(info.levelKey)
            val masteryPercentage = calculateMasteryPercentage(charactersForLevel)
            updateButtonText(info, masteryPercentage)
        }
    }
    
    private fun getCharactersForLevel(levelKey: String): List<String> {
        return when (levelKey) {
            "Hiragana" -> MochiApplication.kanaRepository.getKanaEntries(KanaType.HIRAGANA).map { it.character }
            "Katakana" -> MochiApplication.kanaRepository.getKanaEntries(KanaType.KATAKANA).map { it.character }
            else -> getKanjiCharactersForLevel(levelKey)
        }
    }

    private fun getKanjiCharactersForLevel(levelKey: String): List<String> {
        // Map UI levelKey to JSON properties
        val (type, value) = when {
            levelKey.startsWith("N") -> "jlpt" to levelKey
            levelKey.startsWith("Grade ") -> {
                val grade = levelKey.removePrefix("Grade ")
                "grade" to grade
            }
             // For "Grade 7" -> "7" etc. assuming JSON has 7 for college
            else -> return emptyList()
        }
        
        return MochiApplication.kanjiRepository.getKanjiByLevel(type, value).map { it.character }
    }

    private fun calculateMasteryPercentage(characterList: List<String>): Double {
        if (characterList.isEmpty()) return 0.0

        val totalMasteryPoints = characterList.sumOf { character ->
            val score = ScoreManager.getScore(character, ScoreType.RECOGNITION)
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
                when (info.levelKey) {
                    "Hiragana" -> findNavController().navigate(R.id.action_nav_recognition_to_nav_hiragana)
                    "Katakana" -> findNavController().navigate(R.id.action_nav_recognition_to_nav_katakana)
                    else -> navigateToRecap(info.levelKey)
                }
            }
        }
    }

    private fun navigateToRecap(levelKey: String) {
        // We pass the levelKey (e.g., "N5", "Grade 1") to the next fragment.
        // The GameRecapFragment will also need to be updated to use the repository.
        val bundle = Bundle().apply { putString("level", levelKey) }
        findNavController().navigate(R.id.action_nav_recognition_to_game_recap, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
