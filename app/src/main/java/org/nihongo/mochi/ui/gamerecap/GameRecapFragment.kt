package org.nihongo.mochi.ui.gamerecap

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType
import org.nihongo.mochi.databinding.FragmentGameRecapBinding
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.ui.ScoreUiUtils

class GameRecapFragment : Fragment() {

    private var _binding: FragmentGameRecapBinding? = null
    private val binding get() = _binding!!
    private val args: GameRecapFragmentArgs by navArgs()

    private var kanjiList: List<KanjiEntry> = emptyList()
    private var currentPage = 0
    private val pageSize = 80 // 8 columns * 10 rows

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameRecapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val levelKey = args.level
        // Use levelKey as display title if resource ID is not handy, or map it.
        // For simplicity we show the key (e.g. "N5", "Grade 1")
        binding.textLevelTitle.text = levelKey

        kanjiList = loadKanjiForLevel(levelKey)

        binding.radioGroupGameMode.setOnCheckedChangeListener { _, checkedId ->
            binding.radioGroupReadingOptions.isVisible = checkedId == R.id.radio_button_reading
        }

        binding.buttonPlay.setOnClickListener {
            val selectedModeId = binding.radioGroupGameMode.checkedRadioButtonId
            val gameMode = if (selectedModeId == R.id.radio_button_reading) "reading" else "meaning"

            val selectedReadingOptionId = binding.radioGroupReadingOptions.checkedRadioButtonId
            val readingMode = if (selectedReadingOptionId == R.id.radio_button_random) "random" else "common"

            val action = GameRecapFragmentDirections.actionGameRecapToRecognitionGame(levelKey, gameMode, readingMode, null)
            findNavController().navigate(action)
        }

        binding.buttonNextPage.setOnClickListener {
            if ((currentPage + 1) * pageSize < kanjiList.size) {
                currentPage++
                updateUi()
            }
        }

        binding.buttonPrevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateUi()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    private fun updateUi() {
        if (kanjiList.isEmpty()) {
            binding.gridKanji.removeAllViews()
            binding.textPagination.text = "0..0 / 0"
            return
        }

        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(kanjiList.size)

        binding.gridKanji.removeAllViews()
        for (i in startIndex until endIndex) {
            val kanjiEntry = kanjiList[i]
            val score = ScoreManager.getScore(kanjiEntry.character, ScoreType.RECOGNITION)

            val textView = TextView(context).apply {
                text = kanjiEntry.character
                textSize = 24f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTextColor(Color.BLACK)
                setBackgroundColor(ScoreUiUtils.getScoreColor(requireContext(), score))
                val params = android.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }
                layoutParams = params
                
                // Add click listener
                setOnClickListener {
                    val action = GameRecapFragmentDirections.actionGameRecapToKanjiDetail(kanjiEntry.id)
                    findNavController().navigate(action)
                }
            }
            binding.gridKanji.addView(textView)
        }

        binding.textPagination.text = "${startIndex + 1}..${endIndex} / ${kanjiList.size}"
        binding.buttonPrevPage.isEnabled = currentPage > 0
        binding.buttonPrevPage.alpha = if (currentPage > 0) 1.0f else 0.5f
        binding.buttonNextPage.isEnabled = endIndex < kanjiList.size
        binding.buttonNextPage.alpha = if (endIndex < kanjiList.size) 1.0f else 0.5f
    }

    private fun loadKanjiForLevel(levelKey: String): List<KanjiEntry> {
        val (type, value) = when {
            levelKey.startsWith("N") -> "jlpt" to levelKey
            levelKey.startsWith("Grade ") -> {
                val grade = levelKey.removePrefix("Grade ")
                "grade" to grade
            }
            // Add other cases if needed (e.g. secondary school)
            else -> return emptyList()
        }
        
        return MochiApplication.kanjiRepository.getKanjiByLevel(type, value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
