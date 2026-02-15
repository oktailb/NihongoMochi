package org.nihongo.mochi.ui.gamerecap

import androidx.compose.ui.graphics.Color
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.ScorePresentationUtils
import org.nihongo.mochi.presentation.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameRecapViewModel(
    private val levelContentProvider: LevelContentProvider,
    private val kanjiRepository: KanjiRepository,
    private val scoreRepository: ScoreRepository,
    private val baseColorInt: Int
) : ViewModel() {

    private val _kanjiListWithColors = MutableStateFlow<List<Pair<KanjiEntry, Color>>>(emptyList())
    val kanjiListWithColors: StateFlow<List<Pair<KanjiEntry, Color>>> = _kanjiListWithColors.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()
    
    private val _isReviewEnabled = MutableStateFlow(false)
    val isReviewEnabled: StateFlow<Boolean> = _isReviewEnabled.asStateFlow()

    private var allKanjiEntries: List<KanjiEntry> = emptyList()
    private val pageSize = 80

    fun loadLevel(level: String, gameMode: String) {
        viewModelScope.launch {
            val lowerLevel = level.lowercase()
            
            allKanjiEntries = when {
                lowerLevel == "native_challenge" || lowerLevel == "native challenge" -> 
                    kanjiRepository.getNativeKanji()
                lowerLevel == "no_reading" || lowerLevel == "no reading" -> 
                    kanjiRepository.getNoReadingKanji()
                lowerLevel == "no_meaning" || lowerLevel == "no meaning" -> 
                    kanjiRepository.getNoMeaningKanji()
                else -> {
                    val characters = levelContentProvider.getCharactersForLevel(level)
                    characters.mapNotNull { kanjiRepository.getKanjiByCharacter(it) }
                }
            }
            
            _currentPage.value = 0
            _totalPages.value = (allKanjiEntries.size + pageSize - 1) / pageSize
            updateCurrentPageItems(gameMode)
            checkReviewAvailability(gameMode)
        }
    }

    fun nextPage(gameMode: String) {
        if (_currentPage.value < _totalPages.value - 1) {
            _currentPage.value++
            updateCurrentPageItems(gameMode)
        }
    }

    fun prevPage(gameMode: String) {
        if (_currentPage.value > 0) {
            _currentPage.value--
            updateCurrentPageItems(gameMode)
        }
    }

    fun updateCurrentPageItems(gameMode: String) {
        val scoreType = if (gameMode == "meaning") ScoreManager.ScoreType.RECOGNITION else ScoreManager.ScoreType.READING
        val startIndex = _currentPage.value * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(allKanjiEntries.size)
        
        if (startIndex < allKanjiEntries.size) {
            _kanjiListWithColors.value = allKanjiEntries.subList(startIndex, endIndex).map { kanji ->
                val score = scoreRepository.getScore(kanji.character, scoreType)
                val colorInt = ScorePresentationUtils.getScoreColor(score, baseColorInt)
                kanji to Color(colorInt)
            }
        } else {
             _kanjiListWithColors.value = emptyList()
        }
        checkReviewAvailability(gameMode)
    }
    
    private fun checkReviewAvailability(gameMode: String) {
        val listName = if (gameMode == "meaning") ScoreManager.RECOGNITION_LIST else ScoreManager.READING_LIST
        val revisionList = scoreRepository.getListItems(listName)
        _isReviewEnabled.value = allKanjiEntries.any { revisionList.contains(it.character) }
    }

    fun getRevisionKanjiForLevel(gameMode: String): List<String> {
        val listName = if (gameMode == "meaning") ScoreManager.RECOGNITION_LIST else ScoreManager.READING_LIST
        val revisionList = scoreRepository.getListItems(listName)
        return allKanjiEntries.map { it.character }.filter { revisionList.contains(it) }
    }
}
