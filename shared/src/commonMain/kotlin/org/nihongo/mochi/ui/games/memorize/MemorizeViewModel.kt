package org.nihongo.mochi.ui.games.memorize

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaType
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.ViewModel

class MemorizeViewModel(
    private val kanjiRepository: KanjiRepository,
    private val kanaRepository: KanaRepository,
    private val settingsRepository: SettingsRepository,
    private val levelContentProvider: LevelContentProvider,
    private val scoreRepository: ScoreRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val REVISION_LEVEL_ID = "user_custom_list"

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

    private val _isKanaLevel = MutableStateFlow(false)
    val isKanaLevel: StateFlow<Boolean> = _isKanaLevel.asStateFlow()

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
    private var matchingJob: Job? = null

    init {
        viewModelScope.launch {
            val allKanji = kanjiRepository.getAllKanji()
            val maxS = allKanji.maxOfOrNull { it.strokes?.toIntOrNull() ?: 0 } ?: 20
            _maxStrokes.value = maxS
            _selectedMaxStrokes.value = maxS
            updateLevelInfo()
            loadScoresHistory()
        }
    }

    private fun loadScoresHistory() {
        try {
            _scoresHistory.value = scoreRepository.getMemorizeHistory()
        } catch (e: Exception) {
            _scoresHistory.value = emptyList()
        }
    }

    private fun updateLevelInfo() {
        val levelId = settingsRepository.getSelectedLevel().ifEmpty { "hiragana" }
        val lowerLevel = levelId.lowercase()
        val isKana = lowerLevel.equals("hiragana", ignoreCase = true) || lowerLevel.equals("katakana", ignoreCase = true)
        _isKanaLevel.value = isKana

        val count = if (isKana) {
            val type = if (lowerLevel.equals("hiragana", ignoreCase = true)) KanaType.HIRAGANA else KanaType.KATAKANA
            kanaRepository.getKanaEntries(type).size
        } else {
            val kanjis = getKanjisForLevel(levelId)
            kanjis.count { (it.strokes?.toIntOrNull() ?: 0) <= _selectedMaxStrokes.value }
        }
        
        val filtered = allPossibleGridSizes.filter { it.pairsCount <= count }
        _availableGridSizes.value = filtered.ifEmpty { listOf(allPossibleGridSizes.first()) }
        
        if (_selectedGridSize.value !in _availableGridSizes.value) {
            _selectedGridSize.value = _availableGridSizes.value.lastOrNull() ?: allPossibleGridSizes.first()
        }
    }

    private fun getKanjisForLevel(levelId: String): List<KanjiEntry> {
        val lowerLevel = levelId.lowercase()
        return when {
            lowerLevel == "native_challenge" || lowerLevel == "native challenge" -> kanjiRepository.getNativeKanji()
            lowerLevel == "no_reading" || lowerLevel == "no reading" -> kanjiRepository.getNoReadingKanji()
            lowerLevel == "no_meaning" || lowerLevel == "no meaning" -> kanjiRepository.getNoMeaningKanji()
            lowerLevel == REVISION_LEVEL_ID -> {
                val chars = levelContentProvider.getCharactersForLevel(levelId)
                chars.mapNotNull { kanjiRepository.getKanjiByCharacter(it) }
            }
            else -> kanjiRepository.getKanjiByLevel(levelId)
        }
    }

    fun onGridSizeSelected(size: MemorizeGridSize) {
        if (size in _availableGridSizes.value) {
            _selectedGridSize.value = size
        }
    }

    fun onMaxStrokesChanged(strokes: Int) {
        _selectedMaxStrokes.value = strokes
        updateLevelInfo()
    }

    fun startGame() {
        abandonGame() // Ensure everything is cleaned up first
        viewModelScope.launch {
            val levelId = settingsRepository.getSelectedLevel().ifEmpty { "n5" }
            val lowerLevel = levelId.lowercase()
            val isKana = lowerLevel.equals("hiragana", ignoreCase = true) || lowerLevel.equals("katakana", ignoreCase = true)

            var allPlayables = if (isKana) {
                val type = if (lowerLevel.equals("hiragana", ignoreCase = true)) KanaType.HIRAGANA else KanaType.KATAKANA
                kanaRepository.getKanaEntries(type).map { MemorizePlayable(it.character, it.character) }
            } else {
                getKanjisForLevel(levelId)
                    .filter { (it.strokes?.toIntOrNull() ?: 0) <= _selectedMaxStrokes.value }
                    .map { MemorizePlayable(it.id, it.character) }
            }
            
            if (allPlayables.isEmpty() && !isKana) {
                allPlayables = getKanjisForLevel("n5").map { MemorizePlayable(it.id, it.character) }
            }

            if (allPlayables.isEmpty()) return@launch

            val pairsToSelect = _selectedGridSize.value.pairsCount
            val selectedItems = allPlayables.shuffled().take(pairsToSelect)
            
            val gameCards = (selectedItems + selectedItems)
                .shuffled()
                .mapIndexed { index, item ->
                    MemorizeCardState(id = index, item = item)
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
        if (index !in currentCards.indices || 
            _isProcessing.value || 
            currentCards[index].isFaceUp || 
            currentCards[index].isMatched ||
            _isGameFinished.value) return

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
            
            // Fix race condition: set processing to true IMMEDIATELY
            _isProcessing.value = true
            
            matchingJob?.cancel()
            matchingJob = viewModelScope.launch {
                delay(400) 
                
                val updatedCards = _cards.value
                // Safety check to ensure we didn't reset the game during delay
                if (firstIndex in updatedCards.indices && index in updatedCards.indices) {
                    if (updatedCards[firstIndex].item.id == updatedCards[index].item.id) {
                        audioPlayer.playSound("sounds/correct.mp3")
                        _cards.update { list ->
                            list.mapIndexed { i, card ->
                                if (i == firstIndex || i == index) card.copy(isMatched = true, isFaceUp = true) else card
                            }
                        }
                        _isProcessing.value = false
                        checkGameFinished()
                    } else {
                        audioPlayer.playSound("sounds/incorrect.mp3")
                        delay(600) 
                        _cards.update { list ->
                            list.mapIndexed { i, card ->
                                if (i == firstIndex || i == index) card.copy(isFaceUp = false) else card
                            }
                        }
                        _isProcessing.value = false
                    }
                } else {
                    _isProcessing.value = false
                }
            }
        }
    }

    private fun checkGameFinished() {
        if (_cards.value.isNotEmpty() && _cards.value.all { it.isMatched }) {
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
        
        val newHistory = (listOf(result) + _scoresHistory.value).take(10)
        _scoresHistory.value = newHistory
        
        viewModelScope.launch {
            try {
                scoreRepository.saveMemorizeResult(result)
            } catch (e: Exception) {}
        }
    }

    fun abandonGame() {
        timerJob?.cancel()
        matchingJob?.cancel() // Cancel any pending card processing
        if (!_isGameFinished.value && _cards.value.isNotEmpty()) {
            audioPlayer.playSound("sounds/game_over.mp3")
        }
        _cards.value = emptyList()
        _isGameFinished.value = false
        _isProcessing.value = false
        firstSelectedCardIndex = null
        updateLevelInfo()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        matchingJob?.cancel()
        audioPlayer.stopAll()
    }
}
