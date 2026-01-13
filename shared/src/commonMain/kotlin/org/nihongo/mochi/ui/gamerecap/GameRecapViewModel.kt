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

    private var allKanjiEntries: List<KanjiEntry> = emptyList()
    private val pageSize = 80

    fun loadLevel(level: String, gameMode: String) {
        viewModelScope.launch {
            val characters = levelContentProvider.getCharactersForLevel(level)
            
            allKanjiEntries = if (level.equals("no_meaning", ignoreCase = true)) {
                kanjiRepository.getNoMeaningKanji()
            } else {
                 characters.mapNotNull { kanjiRepository.getKanjiByCharacter(it) }
            }
            
            _totalPages.value = (allKanjiEntries.size + pageSize - 1) / pageSize
            updateCurrentPageItems(gameMode)
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
    }
}
