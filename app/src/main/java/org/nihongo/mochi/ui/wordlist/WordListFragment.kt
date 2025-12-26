package org.nihongo.mochi.ui.wordlist

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.databinding.FragmentWordListBinding
import org.nihongo.mochi.ui.ScoreUiUtils
import org.nihongo.mochi.ui.game.KanjiDetail
import org.nihongo.mochi.ui.game.Reading
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

data class WordItem(val text: String, val type: String)

class WordListFragment : Fragment() {

    private var _binding: FragmentWordListBinding? = null
    private val binding get() = _binding!!
    private val args: WordListFragmentArgs by navArgs()

    private var allWords: List<WordItem> = emptyList()
    private var displayedWords: List<WordItem> = emptyList()
    private var currentPage = 0
    private val pageSize = 80
    private val allKanjiDetails = mutableListOf<KanjiDetail>()
    private val wordTypes = mutableListOf<Pair<String, String>>()

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
        if (wordListName == "user_custom_list") {
            binding.textListTitle.text = getString(R.string.reading_user_list)
        } else {
            binding.textListTitle.text = wordListName.replace("bccwj_wordlist_", "Common Words ").replace(".xml", "")
        }

        allWords = loadWordsForList(wordListName)
        loadAllKanjiDetails()

        setupFilters()
        applyFilters()

        binding.buttonNextPage.setOnClickListener {
            if ((currentPage + 1) * pageSize < displayedWords.size) {
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

        binding.buttonPlay.setOnClickListener {
            val customWordList = displayedWords.map { it.text }.toTypedArray()
            val action = WordListFragmentDirections.actionNavWordListToNavWordQuiz(customWordList)
            findNavController().navigate(action)
        }
    }

    private fun setupFilters() {
        binding.checkboxKanjiOnly.setOnCheckedChangeListener { _, _ -> applyFilters() }
        binding.checkboxSimpleWords.setOnCheckedChangeListener { _, _ -> applyFilters() }
        binding.checkboxCompoundWords.setOnCheckedChangeListener { _, _ -> applyFilters() }
        binding.checkboxIgnoreKnown.setOnCheckedChangeListener { _, _ -> applyFilters() }

        wordTypes.clear()
        wordTypes.add("Tous" to getString(R.string.word_type_all))
        wordTypes.add("和" to getString(R.string.word_type_wa))
        wordTypes.add("固" to getString(R.string.word_type_ko))
        wordTypes.add("外" to getString(R.string.word_type_gai))
        wordTypes.add("混" to getString(R.string.word_type_kon))
        wordTypes.add("漢" to getString(R.string.word_type_kan))
        wordTypes.add("記号" to getString(R.string.word_type_kigo))

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wordTypes.map { it.second })
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerWordType.adapter = adapter
        binding.spinnerWordType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyFilters() {
        val kanjiOnly = binding.checkboxKanjiOnly.isChecked
        val simpleWords = binding.checkboxSimpleWords.isChecked
        val compoundWords = binding.checkboxCompoundWords.isChecked
        val ignoreKnown = binding.checkboxIgnoreKnown.isChecked
        val selectedPosition = binding.spinnerWordType.selectedItemPosition
        val selectedType = if (selectedPosition >= 0 && selectedPosition < wordTypes.size) wordTypes[selectedPosition].first else "Tous"

        displayedWords = allWords.filter { word ->
            val kanjiCount = word.text.count { it.isKanji() }
            val hasKana = word.text.any { it.isKana() }

            val typeFilter = when (selectedType) {
                "Tous" -> true
                else -> word.type == selectedType
            }

            val structureFilter = when {
                kanjiOnly && kanjiCount == 1 && !hasKana -> true
                simpleWords && kanjiCount == 1 && hasKana -> true
                compoundWords && kanjiCount > 1 -> true
                !kanjiOnly && !simpleWords && !compoundWords -> true
                else -> false
            }

            val knownFilter = if (ignoreKnown) {
                val score = ScoreManager.getScore(word.text, ScoreManager.ScoreType.READING)
                (score.successes - score.failures) < 10
            } else {
                true
            }

            typeFilter && structureFilter && knownFilter
        }
        currentPage = 0
        updateUi()
    }

    override fun onResume() {
        super.onResume()
        if (args.wordList == "user_custom_list") {
            allWords = loadWordsForList(args.wordList)
        }
        applyFilters()
    }

    private fun updateUi() {
        if (displayedWords.isEmpty()) {
            binding.wordsContainer.removeAllViews()
            binding.textPagination.text = "0..0 / 0"
            binding.buttonPrevPage.isEnabled = false
            binding.buttonNextPage.isEnabled = false
            return
        }

        val wordScores = displayedWords.map { ScoreManager.getScore(it.text, ScoreManager.ScoreType.READING) }

        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(displayedWords.size)

        binding.wordsContainer.removeAllViews()
        for (i in startIndex until endIndex) {
            val word = displayedWords[i]
            val score = wordScores[i]
            val kanjiDetail = allKanjiDetails.firstOrNull { it.character == word.text }

            val chip = Chip(context).apply {
                text = word.text
                textSize = 18f
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(ScoreUiUtils.getScoreColor(requireContext(), score))
                setTextColor(Color.BLACK)

                if (kanjiDetail != null && kanjiDetail.meanings.isEmpty()) {
                    chipStrokeWidth = 4f
                    chipStrokeColor = android.content.res.ColorStateList.valueOf(Color.RED)
                }
            }
            binding.wordsContainer.addView(chip)
        }

        binding.textPagination.text = "${startIndex + 1}..${endIndex} / ${displayedWords.size}"
        binding.buttonPrevPage.isEnabled = currentPage > 0
        binding.buttonPrevPage.alpha = if (currentPage > 0) 1.0f else 0.5f
        binding.buttonNextPage.isEnabled = endIndex < displayedWords.size
        binding.buttonNextPage.alpha = if (endIndex < displayedWords.size) 1.0f else 0.5f
    }

    private fun loadWordsForList(listName: String): List<WordItem> {
        if (listName == "user_custom_list") {
            val scores = ScoreManager.getAllScores(ScoreManager.ScoreType.READING)
            return scores.mapNotNull { (word, score) ->
                WordItem(word, "")
            }
        }

        val words = mutableListOf<WordItem>()
        val resourceId = resources.getIdentifier(listName, "xml", requireContext().packageName)
        if (resourceId == 0) return emptyList()

        val parser = resources.getXml(resourceId)

        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "word") {
                    val type = parser.getAttributeValue(null, "type") ?: ""
                    val text = parser.nextText()
                    words.add(WordItem(text, type))
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return words
    }

    private fun loadAllKanjiDetails() {
        // Load meanings from XML (Legacy)
        val meanings = loadMeanings()
        
        // Load details from Shared Repository (JSON)
        val allKanjiEntries = MochiApplication.kanjiRepository.getAllKanji()
        
        allKanjiDetails.clear()
        
        for (entry in allKanjiEntries) {
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            // Map JSON readings to UI Readings
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            
            val kanjiDetail = KanjiDetail(id, character, kanjiMeanings, readingsList)
            allKanjiDetails.add(kanjiDetail)
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
            Log.e("WordListFragment", "meanings.xml not found.", e)
        } catch (e: XmlPullParserException) {
            Log.e("WordListFragment", "Error parsing meanings.xml", e)
        } catch (e: IOException) {
            Log.e("WordListFragment", "IO error reading meanings.xml", e)
        }
        return meaningsMap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun Char.isKanji(): Boolean {
        return this in '\u4e00'..'\u9faf' || this in '\u3400'..'\u4dbf'
    }

    fun Char.isKana(): Boolean {
        return this in '\u3040'..'\u309F' || this in '\u30A0'..'\u30FF'
    }
}
