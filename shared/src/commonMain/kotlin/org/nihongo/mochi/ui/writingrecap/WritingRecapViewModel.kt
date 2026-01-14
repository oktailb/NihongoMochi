package org.nihongo.mochi.ui.writingrecap

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.ScorePresentationUtils
import org.nihongo.mochi.presentation.ViewModel

class WritingRecapViewModel(
    private val levelContentProvider: LevelContentProvider,
    private val kanjiRepository: KanjiRepository,
    private val scoreRepository: ScoreRepository,
    private val baseColorInt: Int
) : ViewModel() {

    private val _kanjiList = MutableStateFlow<List<Pair<KanjiEntry, Color>>>(emptyList())
    val kanjiList: StateFlow<List<Pair<KanjiEntry, Color>>> = _kanjiList.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val pageSize = 80
    private var allKanjiEntries: List<KanjiEntry> = emptyList()

    fun loadLevel(levelKey: String) {
        viewModelScope.launch {
            val scoreType = if (levelKey == "user_custom_list") ScoreManager.ScoreType.WRITING else ScoreManager.ScoreType.RECOGNITION
            val characters = levelContentProvider.getCharactersForLevel(levelKey, scoreType)
            allKanjiEntries = characters.mapNotNull { kanjiRepository.getKanjiByCharacter(it) }

            _totalPages.value = if (allKanjiEntries.isEmpty()) 0 else (allKanjiEntries.size + pageSize - 1) / pageSize
            updateCurrentPageItems()
        }
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

    private fun updateCurrentPageItems() {
        val startIndex = _currentPage.value * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(allKanjiEntries.size)

        if (startIndex < allKanjiEntries.size) {
            val pageItems = allKanjiEntries.subList(startIndex, endIndex)
            _kanjiList.value = pageItems.map { kanji ->
                val score = scoreRepository.getScore(kanji.character, ScoreManager.ScoreType.WRITING)
                val colorInt = ScorePresentationUtils.getScoreColor(score, baseColorInt)
                kanji to Color(colorInt)
            }
        } else {
            _kanjiList.value = emptyList()
        }
    }
}
