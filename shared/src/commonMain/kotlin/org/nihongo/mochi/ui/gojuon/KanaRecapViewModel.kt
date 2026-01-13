package org.nihongo.mochi.ui.gojuon

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kana.KanaEntry
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.presentation.ScorePresentationUtils
import org.nihongo.mochi.presentation.ViewModel

class KanaRecapViewModel(
    private val kanaRepository: KanaRepository,
    private val scoreRepository: ScoreRepository,
    private val baseColorInt: Int
) : ViewModel() {

    private val _charactersByLine = MutableStateFlow<Map<Int, List<KanaEntry>>>(emptyMap())
    val charactersByLine: StateFlow<Map<Int, List<KanaEntry>>> = _charactersByLine.asStateFlow()

    private val _linesToShow = MutableStateFlow<List<Int>>(emptyList())
    val linesToShow: StateFlow<List<Int>> = _linesToShow.asStateFlow()

    private val _kanaColors = MutableStateFlow<Map<String, Color>>(emptyMap())
    val kanaColors: StateFlow<Map<String, Color>> = _kanaColors.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private var allLineKeys: List<Int> = emptyList()

    fun loadKana(kanaType: org.nihongo.mochi.domain.kana.KanaType, pageSize: Int) {
        viewModelScope.launch {
            val allCharacters = kanaRepository.getKanaEntries(kanaType)
            val grouped = allCharacters.groupBy { it.line }.toSortedMap()
            _charactersByLine.value = grouped
            allLineKeys = grouped.keys.toList()

            _totalPages.value = if (allLineKeys.isEmpty()) 0 else (allLineKeys.size + pageSize - 1) / pageSize

            if (_currentPage.value >= _totalPages.value) {
                _currentPage.value = (_totalPages.value - 1).coerceAtLeast(0)
            }

            updateCurrentPageItems(pageSize)
        }
    }

    fun refreshScores() {
        val currentChars = _charactersByLine.value.values.flatten()
        val colorMap = currentChars.associate { kana ->
            val score = scoreRepository.getScore(kana.character, ScoreManager.ScoreType.RECOGNITION)
            val colorInt = ScorePresentationUtils.getScoreColor(score, baseColorInt)
            kana.character to Color(colorInt)
        }
        _kanaColors.value = colorMap
    }

    fun nextPage(pageSize: Int) {
        if (_currentPage.value < _totalPages.value - 1) {
            _currentPage.value++
            updateCurrentPageItems(pageSize)
        }
    }

    fun prevPage(pageSize: Int) {
        if (_currentPage.value > 0) {
            _currentPage.value--
            updateCurrentPageItems(pageSize)
        }
    }

    private fun updateCurrentPageItems(pageSize: Int) {
        val startLineIndex = _currentPage.value * pageSize
        val endLineIndex = (startLineIndex + pageSize).coerceAtMost(allLineKeys.size)

        if (startLineIndex < allLineKeys.size) {
            _linesToShow.value = allLineKeys.subList(startLineIndex, endLineIndex)
        } else {
            _linesToShow.value = emptyList()
        }
        refreshScores()
    }
}
