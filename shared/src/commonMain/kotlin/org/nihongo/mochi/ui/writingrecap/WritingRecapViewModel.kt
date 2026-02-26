package org.nihongo.mochi.ui.writingrecap

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.game.KanjiSortOrder
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.ScorePresentationUtils
import org.nihongo.mochi.presentation.ViewModel

class WritingRecapViewModel(
    private val levelContentProvider: LevelContentProvider,
    private val kanjiRepository: KanjiRepository,
    private val scoreRepository: ScoreRepository,
    private val settingsRepository: SettingsRepository,
    private val baseColorInt: Int
) : ViewModel() {

    private val _kanjiList = MutableStateFlow<List<Pair<KanjiEntry, Color>>>(emptyList())
    val kanjiList: StateFlow<List<Pair<KanjiEntry, Color>>> = _kanjiList.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _isReviewEnabled = MutableStateFlow(false)
    val isReviewEnabled: StateFlow<Boolean> = _isReviewEnabled.asStateFlow()

    private val _sortOrder = MutableStateFlow(KanjiSortOrder.DEFAULT)
    val sortOrder: StateFlow<KanjiSortOrder> = _sortOrder.asStateFlow()

    private val _quizSize = MutableStateFlow(settingsRepository.getQuizSize().toString())
    val quizSize: StateFlow<String> = _quizSize.asStateFlow()

    private val pageSize = 80
    private var allKanjiEntries: List<KanjiEntry> = emptyList()
    private var originalKanjiEntries: List<KanjiEntry> = emptyList()

    fun loadLevel(levelKey: String) {
        viewModelScope.launch {
            val scoreType = if (levelKey == "user_custom_list") ScoreManager.ScoreType.WRITING else ScoreManager.ScoreType.RECOGNITION
            val characters = levelContentProvider.getCharactersForLevel(levelKey, scoreType)
            originalKanjiEntries = characters.mapNotNull { kanjiRepository.getKanjiByCharacter(it) }
            applySortAndRefresh()
        }
    }

    fun setSortOrder(order: KanjiSortOrder) {
        _sortOrder.value = order
        applySortAndRefresh()
    }

    fun setQuizSize(size: String) {
        _quizSize.value = size
        size.toIntOrNull()?.let {
            settingsRepository.setQuizSize(it)
        }
    }

    private fun applySortAndRefresh() {
        allKanjiEntries = when (_sortOrder.value) {
            KanjiSortOrder.FREQUENCY -> originalKanjiEntries.sortedBy { it.frequency?.toIntOrNull() ?: Int.MAX_VALUE }
            KanjiSortOrder.STROKES -> originalKanjiEntries.sortedBy { it.strokes?.toIntOrNull() ?: 0 }
            KanjiSortOrder.DEFAULT -> originalKanjiEntries
        }

        _totalPages.value = if (allKanjiEntries.isEmpty()) 0 else (allKanjiEntries.size + pageSize - 1) / pageSize
        _currentPage.value = 0
        updateCurrentPageItems()
        checkReviewAvailability()
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

    fun updateCurrentPageItems() {
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

    private fun checkReviewAvailability() {
        val revisionList = scoreRepository.getListItems(ScoreManager.WRITING_LIST)
        _isReviewEnabled.value = allKanjiEntries.any { revisionList.contains(it.character) }
    }

    fun getRevisionKanjiForLevel(): List<String> {
        val revisionList = scoreRepository.getListItems(ScoreManager.WRITING_LIST)
        return allKanjiEntries.map { it.character }.filter { revisionList.contains(it) }
    }
}
