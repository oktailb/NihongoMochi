package org.nihongo.mochi.ui.wordlist

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.databinding.FragmentWordListBinding
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.Reading
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.words.WordListEngine
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.presentation.ScorePresentationUtils

class WordListFragment : Fragment() {

    private var _binding: FragmentWordListBinding? = null
    private val binding get() = _binding!!
    private val args: WordListFragmentArgs by navArgs()

    private val wordRepository: WordRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val meaningRepository: MeaningRepository by inject()
    private val kanjiRepository: KanjiRepository by inject()
    
    // Engine instance
    private lateinit var engine: WordListEngine

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
        
        // Init Engine
        engine = WordListEngine(wordRepository)

        val wordListName = args.wordList
        if (wordListName == "user_custom_list") {
            binding.textListTitle.text = getString(R.string.reading_user_list)
        } else {
            binding.textListTitle.text = wordListName.replace("bccwj_wordlist_", "Common Words ").replace(".json", "")
        }

        // Load data via Engine
        engine.loadList(wordListName)
        
        // Load Kanji Details for tooltips/display logic (kept local for now as it seems purely UI decoration related)
        loadAllKanjiDetails()

        setupFilters()
        // Initial application of filters
        updateDisplayedList()

        binding.buttonNextPage.setOnClickListener {
            val displayedWords = engine.getDisplayedWords()
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
            val displayedWords = engine.getDisplayedWords()
            val customWordList = displayedWords.map { it.text }.toTypedArray()
            val action = WordListFragmentDirections.actionNavWordListToNavWordQuiz(customWordList)
            findNavController().navigate(action)
        }
    }

    private fun setupFilters() {
        binding.checkboxKanjiOnly.setOnCheckedChangeListener { _, isChecked -> 
            engine.filterKanjiOnly = isChecked
            updateDisplayedList() 
        }
        binding.checkboxSimpleWords.setOnCheckedChangeListener { _, isChecked -> 
            engine.filterSimpleWords = isChecked
            updateDisplayedList() 
        }
        binding.checkboxCompoundWords.setOnCheckedChangeListener { _, isChecked -> 
            engine.filterCompoundWords = isChecked
            updateDisplayedList() 
        }
        binding.checkboxIgnoreKnown.setOnCheckedChangeListener { _, isChecked -> 
            engine.filterIgnoreKnown = isChecked
            updateDisplayedList() 
        }

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
                if (position >= 0 && position < wordTypes.size) {
                    engine.filterWordType = wordTypes[position].first
                    updateDisplayedList()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    
    private fun updateDisplayedList() {
        engine.applyFilters()
        currentPage = 0
        updateUi()
    }

    override fun onResume() {
        super.onResume()
        if (args.wordList == "user_custom_list") {
             engine.loadList(args.wordList)
             updateDisplayedList()
        }
    }

    private fun updateUi() {
        val displayedWords = engine.getDisplayedWords()
        
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

        // Pre-fetch base color
        val baseColor = ContextCompat.getColor(requireContext(), R.color.recap_grid_base_color)

        binding.wordsContainer.removeAllViews()
        for (i in startIndex until endIndex) {
            val word = displayedWords[i]
            val score = wordScores[i]
            val kanjiDetail = allKanjiDetails.firstOrNull { it.character == word.text }

            val chip = Chip(context).apply {
                text = word.text
                textSize = 18f
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(ScorePresentationUtils.getScoreColor(score, baseColor))
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

    private fun loadAllKanjiDetails() {
        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        val allKanjiEntries = kanjiRepository.getAllKanji()
        
        allKanjiDetails.clear()
        
        for (entry in allKanjiEntries) {
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            
            val kanjiDetail = KanjiDetail(id, character, kanjiMeanings, readingsList)
            allKanjiDetails.add(kanjiDetail)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
