package org.nihongo.mochi.ui.wordlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.LearningScore
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.Reading
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.words.WordEntry
import org.nihongo.mochi.domain.words.WordListEngine
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.presentation.ViewModel

class WordListViewModel(
    private val wordRepository: WordRepository,
    private val meaningRepository: MeaningRepository,
    private val kanjiRepository: KanjiRepository,
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val engine = WordListEngine(wordRepository, scoreRepository)

    private val _displayedWords = MutableStateFlow<List<Triple<WordEntry, LearningScore, Boolean>>>(emptyList())
    val displayedWords: StateFlow<List<Triple<WordEntry, LearningScore, Boolean>>> = _displayedWords.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    // Filters state
    private val _filterKanjiOnly = MutableStateFlow(false)
    val filterKanjiOnly = _filterKanjiOnly.asStateFlow()
    
    private val _filterSimpleWords = MutableStateFlow(false)
    val filterSimpleWords = _filterSimpleWords.asStateFlow()
    
    private val _filterCompoundWords = MutableStateFlow(false)
    val filterCompoundWords = _filterCompoundWords.asStateFlow()
    
    private val _filterIgnoreKnown = MutableStateFlow(false)
    val filterIgnoreKnown = _filterIgnoreKnown.asStateFlow()

    private val _selectedWordType = MutableStateFlow("Tous" to "All") // Internal Key, Display Value
    val selectedWordType = _selectedWordType.asStateFlow()

    private val _screenTitleKey = MutableStateFlow<String?>(null)
    val screenTitleKey = _screenTitleKey.asStateFlow()

    private val pageSize = 80
    private var allKanjiDetails = listOf<KanjiDetail>()

    fun loadList(listName: String) {
        viewModelScope.launch {
            if (listName == "user_custom_list") {
                _screenTitleKey.value = "reading_user_list"
            } else {
                val defs = levelsRepository.loadLevelDefinitions()
                var foundKey: String? = null
                outer@ for (section in defs.sections.values) {
                    for (level in section.levels) {
                        for (activity in level.activities.values) {
                            if (activity.dataFile == listName) {
                                foundKey = level.name
                                break@outer
                            }
                        }
                    }
                }
                _screenTitleKey.value = foundKey
            }

            loadAllKanjiDetails()
            engine.loadList(listName)
            applyFilters()
        }
    }
    
    fun setFilterKanjiOnly(enabled: Boolean) {
        _filterKanjiOnly.value = enabled
        engine.filterKanjiOnly = enabled
        applyFilters()
    }
    
    fun setFilterSimpleWords(enabled: Boolean) {
        _filterSimpleWords.value = enabled
        engine.filterSimpleWords = enabled
        applyFilters()
    }
    
    fun setFilterCompoundWords(enabled: Boolean) {
        _filterCompoundWords.value = enabled
        engine.filterCompoundWords = enabled
        applyFilters()
    }
    
    fun setFilterIgnoreKnown(enabled: Boolean) {
        _filterIgnoreKnown.value = enabled
        engine.filterIgnoreKnown = enabled
        applyFilters()
    }
    
    fun setWordType(type: Pair<String, String>) {
        _selectedWordType.value = type
        engine.filterWordType = type.first
        applyFilters()
    }

    private fun applyFilters() {
        engine.applyFilters()
        _currentPage.value = 0
        updateCurrentPageItems()
    }

    fun nextPage() {
        if (_currentPage.value < _totalPages.value - 1) {
            _currentPage.value++
            updateCurrentPageItems()
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
            updateCurrentPageItems()
        }
    }
    
    fun getGameWordList(): Array<String> {
         return engine.getDisplayedWords().map { it.text }.toTypedArray()
    }

    private fun updateCurrentPageItems() {
        val allDisplayed = engine.getDisplayedWords()
        _totalPages.value = if (allDisplayed.isEmpty()) 0 else (allDisplayed.size + pageSize - 1) / pageSize
        
        val startIndex = _currentPage.value * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(allDisplayed.size)
        
        if (startIndex < allDisplayed.size) {
            val pageItems = allDisplayed.subList(startIndex, endIndex)
            _displayedWords.value = pageItems.map { word ->
                val kanjiScore = scoreRepository.getScore(word.text, ScoreManager.ScoreType.READING)
                val kanjiDetail = allKanjiDetails.firstOrNull { it.character == word.text }
                val isRedBorder = kanjiDetail != null && kanjiDetail.meanings.isEmpty()
                Triple(word, kanjiScore, isRedBorder)
            }
        } else {
             _displayedWords.value = emptyList()
        }
    }
    
    private fun loadAllKanjiDetails() {
        viewModelScope.launch {
            val locale = settingsRepository.getAppLocale()
            val meanings = meaningRepository.getMeanings(locale)
            val allKanjiEntries = kanjiRepository.getAllKanji()
            
            val details = mutableListOf<KanjiDetail>()
            for (entry in allKanjiEntries) {
                val id = entry.id
                val character = entry.character
                val kanjiMeanings = meanings[id] ?: emptyList()
                
                val readingsList = mutableListOf<Reading>()
                entry.readings?.reading?.forEach { readingEntry ->
                     val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                     readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
                }
                details.add(KanjiDetail(id, character, kanjiMeanings, readingsList))
            }
            allKanjiDetails = details
        }
    }
}
