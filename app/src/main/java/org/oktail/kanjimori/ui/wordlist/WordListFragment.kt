package org.oktail.kanjimori.ui.wordlist

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.KanjiScore
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.databinding.FragmentWordListBinding
import org.oktail.kanjimori.ui.recognitiongame.KanjiDetail
import org.oktail.kanjimori.ui.recognitiongame.Reading
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class WordListFragment : Fragment() {

    private var _binding: FragmentWordListBinding? = null
    private val binding get() = _binding!!
    private val args: WordListFragmentArgs by navArgs()

    private var wordList: List<String> = emptyList()
    private var currentPage = 0
    private val pageSize = 80
    private val allKanjiDetailsXml = mutableListOf<KanjiDetail>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wordListName = args.wordList
        binding.textListTitle.text = wordListName.replace("bccwj_wordlist_", "Common Words ").replace(".xml", "")

        wordList = loadWordsForList(wordListName)
        loadAllKanjiDetails()

        binding.buttonNextPage.setOnClickListener {
            if ((currentPage + 1) * pageSize < wordList.size) {
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
        if (wordList.isEmpty()) {
            binding.textStats.text = ""
            binding.wordsContainer.removeAllViews()
            binding.textPagination.text = "0..0 / 0"
            return
        }

        // --- Process all Words in one go ---
        val scoreBuckets = (0..10).associateWith { 0 }.toMutableMap()
        var newWords = 0
        val wordScores = wordList.map { ScoreManager.getScore(requireContext(), it) }

        for (score in wordScores) {
            if (score.successes == 0 && score.failures == 0) {
                newWords++
            } else {
                val balance = score.successes - score.failures
                val bucket = balance.coerceIn(0, 10)
                scoreBuckets[bucket] = (scoreBuckets[bucket] ?: 0) + 1
            }
        }

        // --- Update Stats UI ---
        val statsText = buildString {
            append("Nouveau: $newWords")
            scoreBuckets.toSortedMap().forEach { (score, count) ->
                if (count > 0) {
                    append(", $score:$count")
                }
            }
        }
        binding.textStats.text = statsText

        // --- Update Grid UI for the current page ---
        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(wordList.size)

        binding.wordsContainer.removeAllViews()
        for (i in startIndex until endIndex) {
            val word = wordList[i]
            val score = wordScores[i]
            val kanjiDetail = allKanjiDetailsXml.firstOrNull { it.character == word }

            val chip = Chip(context).apply {
                text = word
                textSize = 18f
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(calculateColor(score))
                setTextColor(Color.BLACK)

                if (kanjiDetail != null && kanjiDetail.meanings.isEmpty()) {
                    chipStrokeWidth = 4f
                    chipStrokeColor = android.content.res.ColorStateList.valueOf(Color.RED)
                }
            }
            binding.wordsContainer.addView(chip)
        }

        // --- Update Pagination UI ---
        binding.textPagination.text = "${startIndex + 1}..${endIndex} / ${wordList.size}"
        binding.buttonPrevPage.isEnabled = currentPage > 0
        binding.buttonPrevPage.alpha = if (currentPage > 0) 1.0f else 0.5f
        binding.buttonNextPage.isEnabled = endIndex < wordList.size
        binding.buttonNextPage.alpha = if (endIndex < wordList.size) 1.0f else 0.5f
    }

    private fun loadWordsForList(listName: String): List<String> {
        val words = mutableListOf<String>()
        val resourceId = resources.getIdentifier(listName, "xml", requireContext().packageName)
        if (resourceId == 0) return emptyList()

        val parser = resources.getXml(resourceId)

        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "word") {
                    words.add(parser.nextText())
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return words
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
            Log.e("WordListFragment", "meanings.xml not found for current locale. Fallback should occur.", e)
        } catch (e: XmlPullParserException) {
            Log.e("WordListFragment", "Error parsing meanings.xml", e)
        } catch (e: IOException) {
            Log.e("WordListFragment", "IO error reading meanings.xml", e)
        }
        return meaningsMap
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