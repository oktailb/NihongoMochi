package org.nihongo.mochi.ui.games.memorize

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.presentation.ViewModel

class MemorizeViewModel(
    private val kanjiRepository: KanjiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val allPossibleGridSizes = listOf(
        MemorizeGridSize(4, 3), 
        MemorizeGridSize(4, 4), 
        MemorizeGridSize(5, 4), 
        MemorizeGridSize(6, 4), 
        MemorizeGridSize(6, 5)  
    )

    private val _availableGridSizes = MutableStateFlow<List<MemorizeGridSize>>(allPossibleGridSizes)
    val availableGridSizes: StateFlow<List<MemorizeGridSize>> = _availableGridSizes.asStateFlow()

    private val _selectedGridSize = MutableStateFlow(allPossibleGridSizes[1])
    val selectedGridSize: StateFlow<MemorizeGridSize> = _selectedGridSize.asStateFlow()

    private val _maxStrokes = MutableStateFlow(20)
    val maxStrokes: StateFlow<Int> = _maxStrokes.asStateFlow()
    
    private val _selectedMaxStrokes = MutableStateFlow(20)
    val selectedMaxStrokes: StateFlow<Int> = _selectedMaxStrokes.asStateFlow()

    private val _scoresHistory = MutableStateFlow<List<MemorizeGameResult>>(emptyList())
    val scoresHistory: StateFlow<List<MemorizeGameResult>> = _scoresHistory.asStateFlow()

    private val _cards = MutableStateFlow<List<MemorizeCardState>>(emptyList())
    val cards: StateFlow<List<MemorizeCardState>> = _cards.asStateFlow()

    private val _moves = MutableStateFlow(0)
    val moves: StateFlow<Int> = _moves.asStateFlow()

    private val _gameTimeSeconds = MutableStateFlow(0)
    val gameTimeSeconds: StateFlow<Int> = _gameTimeSeconds.asStateFlow()

    private val _isGameFinished = MutableStateFlow(false)
    val isGameFinished: StateFlow<Boolean> = _isGameFinished.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private var firstSelectedCardIndex: Int? = null
    private var timerJob: Job? = null
    private val json = Json { ignoreUnknownKeys = true }

    init {
        viewModelScope.launch {
            val allKanji = kanjiRepository.getAllKanji()
            val maxS = allKanji.maxOfOrNull { it.strokes?.toIntOrNull() ?: 0 } ?: 20
            _maxStrokes.value = maxS
            _selectedMaxStrokes.value = maxS
            updateAvailableGridSizes()
            loadScoresHistory()
        }
    }

    private fun loadScoresHistory() {
        try {
            val historyJson = ScoreManager.getMemorizeHistory()
            if (historyJson.isNotEmpty() && historyJson != "[]") {
                val history = json.decodeFromString<List<MemorizeGameResult>>(historyJson)
                _scoresHistory.value = history
            }
        } catch (e: Exception) {
            _scoresHistory.value = emptyList()
        }
    }

    private fun updateAvailableGridSizes() {
        val levelId = settingsRepository.getSelectedLevel()
        val count = kanjiRepository.getKanjiByLevel(levelId)
            .count { (it.strokes?.toIntOrNull() ?: 0) <= _selectedMaxStrokes.value }
        
        val filtered = allPossibleGridSizes.filter { it.pairsCount <= count }
        _availableGridSizes.value = filtered.ifEmpty { listOf(allPossibleGridSizes.first()) }
        
        if (_selectedGridSize.value !in _availableGridSizes.value) {
            _selectedGridSize.value = _availableGridSizes.value.last()
        }
    }

    fun onGridSizeSelected(size: MemorizeGridSize) {
        if (size in _availableGridSizes.value) {
            _selectedGridSize.value = size
        }
    }

    fun onMaxStrokesChanged(strokes: Int) {
        _selectedMaxStrokes.value = strokes
        updateAvailableGridSizes()
    }

    fun startGame() {
        viewModelScope.launch {
            val levelId = settingsRepository.getSelectedLevel()
            val allAvailableKanji = kanjiRepository.getKanjiByLevel(levelId)
                .filter { (it.strokes?.toIntOrNull() ?: 0) <= _selectedMaxStrokes.value }
            
            if (allAvailableKanji.isEmpty()) return@launch

            val pairsToSelect = _selectedGridSize.value.pairsCount
            val selectedKanji = allAvailableKanji.shuffled().take(pairsToSelect)
            
            val gameCards = (selectedKanji + selectedKanji)
                .shuffled()
                .mapIndexed { index, kanji ->
                    MemorizeCardState(id = index, kanji = kanji)
                }
                
            _cards.value = gameCards
            _moves.value = 0
            _gameTimeSeconds.value = 0
            _isGameFinished.value = false
            _isProcessing.value = false
            firstSelectedCardIndex = null
            
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _gameTimeSeconds.update { it + 1 }
            }
        }
    }

    fun onCardClicked(index: Int) {
        val currentCards = _cards.value
        if (index !in currentCards.indices || _isProcessing.value || currentCards[index].isFaceUp || currentCards[index].isMatched) return

        _cards.update { list ->
            list.mapIndexed { i, card ->
                if (i == index) card.copy(isFaceUp = true) else card
            }
        }

        if (firstSelectedCardIndex == null) {
            firstSelectedCardIndex = index
        } else {
            val firstIndex = firstSelectedCardIndex!!
            firstSelectedCardIndex = null 
            _moves.update { it + 1 }
            
            if (currentCards[firstIndex].kanji.id == currentCards[index].kanji.id) {
                _cards.update { list ->
                    list.mapIndexed { i, card ->
                        if (i == firstIndex || i == index) card.copy(isMatched = true, isFaceUp = true) else card
                    }
                }
                checkGameFinished()
            } else {
                viewModelScope.launch {
                    _isProcessing.value = true
                    delay(800)
                    _cards.update { list ->
                        list.mapIndexed { i, card ->
                            if (i == firstIndex || i == index) card.copy(isFaceUp = false) else card
                        }
                    }
                    _isProcessing.value = false
                }
            }
        }
    }

    private fun checkGameFinished() {
        if (_cards.value.all { it.isMatched }) {
            _isGameFinished.value = true
            timerJob?.cancel()
            saveFinalScore()
        }
    }

    private fun saveFinalScore() {
        val result = MemorizeGameResult(
            moves = _moves.value,
            totalPairs = _selectedGridSize.value.pairsCount,
            gridSizeLabel = _selectedGridSize.value.toString(),
            timeSeconds = _gameTimeSeconds.value,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        
        val newHistory = (_scoresHistory.value.toMutableList().apply {
            add(0, result)
        }).take(10)
        
        _scoresHistory.value = newHistory
        
        viewModelScope.launch {
            try {
                val historyJson = json.encodeToString(newHistory)
                ScoreManager.saveMemorizeHistory(historyJson)
            } catch (e: Exception) {
            }
        }
    }
}
