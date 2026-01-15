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
import org.nihongo.mochi.domain.meaning.WordMeaningRepository
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.Reading
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.statistics.StatisticsType
import org.nihongo.mochi.domain.words.WordEntry
import org.nihongo.mochi.domain.words.WordListEngine
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.presentation.ViewModel

class WordListViewModel(
    private val wordRepository: WordRepository,
    private val wordMeaningRepository: WordMeaningRepository,
    private val kanjiRepository: KanjiRepository,
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val engine = WordListEngine(wordRepository, scoreRepository)

    private val _displayedWords = MutableStateFlow<List<Triple<WordEntry, LearningScore, String?>>>(emptyList())
    val displayedWords: StateFlow<List<Triple<WordEntry, LearningScore, String?>>> = _displayedWords.asStateFlow()

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
    private var wordMeanings = mapOf<String, String>()

    fun loadList(levelId: String) {
        viewModelScope.launch {
            val locale = settingsRepository.getAppLocale()
            wordMeanings = wordMeaningRepository.getWordMeaningsSuspend(locale)

            val defs = levelsRepository.loadLevelDefinitions()
            val levelDef = defs.sections.values.flatMap { it.levels }.find { it.id == levelId }
            
            if (levelId == "user_custom_list") {
                _screenTitleKey.value = "reading_user_list"
                engine.loadList(levelId)
            } else {
                _screenTitleKey.value = levelDef?.name
                val config = levelDef?.activities?.get(StatisticsType.READING)
                
                if (config != null) {
                    val words = wordRepository.getWordsByConfig(config)
                    engine.setWords(words)
                } else {
                    engine.loadList(levelId)
                }
            }

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
                val meaning = wordMeanings[word.id]
                Triple(word, kanjiScore, meaning)
            }
        } else {
             _displayedWords.value = emptyList()
        }
    }
}
