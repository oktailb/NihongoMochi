package org.nihongo.mochi.ui.writingrecap

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.databinding.FragmentWritingRecapBinding
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.ui.ScoreUiUtils

class WritingRecapFragment : Fragment() {

    private var _binding: FragmentWritingRecapBinding? = null
    private val binding get() = _binding!!
    private val args: WritingRecapFragmentArgs by navArgs()

    private var kanjiList: List<KanjiEntry> = emptyList()
    private var currentPage = 0
    private val pageSize = 80 // 8 columns * 10 rows

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

        val level = args.level
        binding.textLevelTitle.text = level

        kanjiList = loadKanjiForLevel(level)

        binding.buttonPlay.setOnClickListener {
            if (level == "user_custom_list") {
                val characters = kanjiList.map { it.character }.toTypedArray()
                val action = WritingRecapFragmentDirections.actionWritingRecapToWritingGame(null, characters)
                findNavController().navigate(action)
            } else {
                val action = WritingRecapFragmentDirections.actionWritingRecapToWritingGame(level, null)
                findNavController().navigate(action)
            }
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
        // Reload list in case scores changed (for user list)
        if (args.level == "user_custom_list") {
            kanjiList = loadKanjiForLevel(args.level)
        }
        updateUi()
    }

    private fun updateUi() {
        if (kanjiList.isEmpty()) {
            binding.gridKanji.removeAllViews()
            binding.textPagination.text = "0..0 / 0"
            binding.buttonPlay.isEnabled = false
            return
        }

        binding.buttonPlay.isEnabled = true

        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(kanjiList.size)

        binding.gridKanji.removeAllViews()
        for (i in startIndex until endIndex) {
            val kanjiEntry = kanjiList[i]
            val score = ScoreManager.getScore(kanjiEntry.character, ScoreManager.ScoreType.WRITING)

            val textView = TextView(context).apply {
                text = kanjiEntry.character
                textSize = 24f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                
                val typedValue = TypedValue()
                context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
                setTextColor(ContextCompat.getColor(context, typedValue.resourceId))

                setBackgroundColor(ScoreUiUtils.getScoreColor(requireContext(), score))
                
                val params = android.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }
                layoutParams = params
                
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

    private fun loadKanjiForLevel(levelName: String): List<KanjiEntry> {
        if (levelName == "user_custom_list") {
            val scores = ScoreManager.getAllScores(ScoreManager.ScoreType.WRITING)
            val userKanjiChars = scores.filter { (_, score) -> (score.successes - score.failures) < 10 }.keys
            // We need the full KanjiEntry, not just the character. So we get them from the repo.
            val allKanji = MochiApplication.kanjiRepository.getAllKanji()
            return allKanji.filter { userKanjiChars.contains(it.character) }
        }

        val (type, value) = when {
            levelName.startsWith("N") -> "jlpt" to levelName
            levelName.startsWith("Grade ") -> {
                val grade = levelName.removePrefix("Grade ")
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
