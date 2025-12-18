package org.oktail.kanjimori.ui.writingrecap

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.KanjiScore
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.databinding.FragmentWritingRecapBinding
import org.xmlpull.v1.XmlPullParser

class WritingRecapFragment : Fragment() {

    private var _binding: FragmentWritingRecapBinding? = null
    private val binding get() = _binding!!
    private val args: WritingRecapFragmentArgs by navArgs()

    private var kanjiList: List<String> = emptyList()
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
                val action = WritingRecapFragmentDirections.actionWritingRecapToWritingGame(null, kanjiList.toTypedArray())
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
        // Refresh the list in case mastery has changed
        kanjiList = loadKanjiForLevel(args.level)
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

        val kanjiScores = kanjiList.map { ScoreManager.getScore(requireContext(), it, ScoreManager.ScoreType.WRITING) }

        // --- Update Grid UI for the current page ---
        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(kanjiList.size)

        binding.gridKanji.removeAllViews()
        for (i in startIndex until endIndex) {
            val kanjiCharacter = kanjiList[i]
            val score = kanjiScores[i]

            val textView = TextView(context).apply {
                text = kanjiCharacter
                textSize = 24f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setBackgroundColor(calculateColor(score))
                val params = android.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }
                layoutParams = params
            }
            binding.gridKanji.addView(textView)
        }

        // --- Update Pagination UI ---
        binding.textPagination.text = "${startIndex + 1}..${endIndex} / ${kanjiList.size}"
        binding.buttonPrevPage.isEnabled = currentPage > 0
        binding.buttonPrevPage.alpha = if (currentPage > 0) 1.0f else 0.5f
        binding.buttonNextPage.isEnabled = endIndex < kanjiList.size
        binding.buttonNextPage.alpha = if (endIndex < kanjiList.size) 1.0f else 0.5f
    }

    private fun loadKanjiForLevel(levelName: String): List<String> {
        if (levelName == "user_custom_list") {
            return loadUserListKanji()
        }

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

    private fun loadUserListKanji(): List<String> {
        val scores = ScoreManager.getAllScores(requireContext(), ScoreManager.ScoreType.WRITING)
        return scores.filter { (_, score) -> (score.successes - score.failures) < 10 }.keys.toList()
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

    private fun calculateColor(score: KanjiScore): Int {
        val balance = score.successes - score.failures
        val percentage = (balance.toFloat() / 10.0f).coerceIn(-1.0f, 1.0f)

        return when {
            percentage > 0 -> lerpColor(Color.WHITE, Color.GREEN, percentage)
            percentage < 0 -> lerpColor(Color.WHITE, Color.RED, -percentage)
            else -> Color.WHITE
        }
    }

    private fun lerpColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startA = Color.alpha(startColor)
        val startR = Color.red(startColor)
        val startG = Color.green(startColor)
        val startB = Color.blue(startColor)

        val endA = Color.alpha(endColor)
        val endR = Color.red(endColor)
        val endG = Color.green(endColor)
        val endB = Color.blue(endColor)

        val a = (startA + fraction * (endA - startA)).toInt()
        val r = (startR + fraction * (endR - startR)).toInt()
        val g = (startG + fraction * (endG - startG)).toInt()
        val b = (startB + fraction * (endB - startB)).toInt()

        return Color.argb(a, r, g, b)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}