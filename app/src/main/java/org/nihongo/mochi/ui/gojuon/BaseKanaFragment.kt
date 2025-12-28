package org.nihongo.mochi.ui.gojuon

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.databinding.FragmentKanaBinding
import org.nihongo.mochi.domain.kana.KanaEntry
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.presentation.ScorePresentationUtils

abstract class BaseKanaFragment : Fragment() {

    private var _binding: FragmentKanaBinding? = null
    protected val binding get() = _binding!!

    abstract val kanaType: KanaFragmentType

    private var charactersByLine: Map<Int, List<KanaEntry>> = emptyMap()
    private var lineKeys: List<Int> = emptyList()
    private var currentPage = 0
    private var pageSize = 16 // Default to portrait
    
    // Injected repository using Koin
    private val kanaRepository: KanaRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textLevelTitle.text = getString(kanaType.titleRes)

        // Dynamically set page size based on orientation
        val orientation = resources.configuration.orientation
        pageSize = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 16

        val allCharacters = kanaRepository.getKanaEntries(kanaType.domainType)
        charactersByLine = allCharacters.groupBy { it.line }.toSortedMap()
        lineKeys = charactersByLine.keys.toList()

        binding.buttonPlayQuiz.setOnClickListener {
            findNavController().navigate(kanaType.navigationActionId)
        }

        binding.buttonNextPage.setOnClickListener {
            val totalPages = (lineKeys.size + pageSize - 1) / pageSize
            if (currentPage < totalPages - 1) {
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

        // Reset current page if it's now out of bounds after orientation change
        val totalPages = (lineKeys.size + pageSize - 1) / pageSize
        if (currentPage >= totalPages) {
            currentPage = (totalPages - 1).coerceAtLeast(0)
        }

        updateUi()
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    private fun updateUi() {
        binding.gridKana.removeAllViews()

        val totalPages = (lineKeys.size + pageSize - 1) / pageSize
        if (lineKeys.isEmpty()) {
            binding.textPagination.text = "0 / 0"
            binding.buttonPrevPage.isEnabled = false
            binding.buttonNextPage.isEnabled = false
            return
        }

        val startLineIndex = currentPage * pageSize
        val endLineIndex = (startLineIndex + pageSize).coerceAtMost(lineKeys.size)
        val linesToShow = lineKeys.subList(startLineIndex, endLineIndex)

        // Pre-fetch base color
        val baseColor = ContextCompat.getColor(requireContext(), R.color.recap_grid_base_color)

        for (lineKey in linesToShow) {
            val charsInLine = charactersByLine[lineKey] ?: continue
            for (kana in charsInLine) {
                val score = ScoreManager.getScore(kana.character, ScoreManager.ScoreType.RECOGNITION)
                val textView = TextView(context).apply {
                    text = kana.character
                    textSize = 24f
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    setTextColor(Color.BLACK)
                    setBackgroundColor(ScorePresentationUtils.getScoreColor(score, baseColor))
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        setMargins(4, 4, 4, 4)
                    }
                }
                binding.gridKana.addView(textView)
            }

            val spacersNeeded = 5 - charsInLine.size
            if (spacersNeeded > 0) {
                for (i in 1..spacersNeeded) {
                    val spacer = Space(context).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = 0
                            height = GridLayout.LayoutParams.WRAP_CONTENT
                            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        }
                    }
                    binding.gridKana.addView(spacer)
                }
            }
        }

        binding.textPagination.text = "${currentPage + 1} / $totalPages"
        binding.buttonPrevPage.isEnabled = currentPage > 0
        binding.buttonPrevPage.alpha = if (currentPage > 0) 1.0f else 0.5f
        binding.buttonNextPage.isEnabled = currentPage < totalPages - 1
        binding.buttonNextPage.alpha = if (currentPage < totalPages - 1) 1.0f else 0.5f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

enum class KanaFragmentType(val domainType: org.nihongo.mochi.domain.kana.KanaType, val titleRes: Int, val navigationActionId: Int) {
    HIRAGANA(org.nihongo.mochi.domain.kana.KanaType.HIRAGANA, R.string.level_hiragana, R.id.action_nav_hiragana_to_hiragana_quiz),
    KATAKANA(org.nihongo.mochi.domain.kana.KanaType.KATAKANA, R.string.level_katakana, R.id.action_nav_katakana_to_katakana_quiz)
}
