package org.nihongo.mochi.ui.writingrecap

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType
import org.nihongo.mochi.databinding.FragmentWritingRecapBinding
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.ui.ScoreUiUtils

class WritingRecapFragment : Fragment() {

    private var _binding: FragmentWritingRecapBinding? = null
    private val binding get() = _binding!!
    private val args: WritingRecapFragmentArgs by navArgs()

    private var kanjiList: List<KanjiEntry> = emptyList()
    private var currentPage = 0
    private val pageSize = 80

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWritingRecapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textGameTitle.text = getString(R.string.title_game_recap)

        val level = args.level
        binding.textLevelTitle.text = level

        // Load Kanji list for the level
        kanjiList = loadKanjiForLevel(level)
        
        // Initial UI Update
        updateUi()

        binding.buttonPlay.setOnClickListener {
            val action = WritingRecapFragmentDirections.actionWritingRecapToWritingGame(level, null)
            findNavController().navigate(action)
        }
        
        binding.buttonPlay.isEnabled = true

        // Pagination controls
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
            // Use WRITING score type here
            val score = ScoreManager.getScore(kanjiEntry.character, ScoreType.WRITING)

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
                
                // Add click listener to see details
                setOnClickListener {
                    val action = WritingRecapFragmentDirections.actionWritingRecapToKanjiDetail(kanjiEntry.id)
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
            else -> return emptyList()
        }
        
        return MochiApplication.kanjiRepository.getKanjiByLevel(type, value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
