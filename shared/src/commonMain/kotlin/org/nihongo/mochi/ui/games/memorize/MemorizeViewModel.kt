package org.nihongo.mochi.ui.games.memorize

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
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
    private val json = Json { ignoreUnknownKeys = true }

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
            val historyJson = scoreRepository.getMemorizeHistory()
            if (historyJson.isNotEmpty() && historyJson != "[]") {
                val history = json.decodeFromString<List<MemorizeGameResult>>(historyJson)
                _scoresHistory.value = history
            }
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
            
            // Ultimate Fallback
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
            
            viewModelScope.launch {
                _isProcessing.value = true
                
                if (currentCards[firstIndex].item.id == currentCards[index].item.id) {
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
        
        // Update local state history for immediate UI feedback if needed
        val newHistory = (_scoresHistory.value.toMutableList().apply {
            add(0, result)
        }).take(10)
        _scoresHistory.value = newHistory
        
        viewModelScope.launch {
            try {
                // Save single result instead of the whole list JSON string
                scoreRepository.saveMemorizeResult(result)
            } catch (e: Exception) {
            }
        }
    }

    fun abandonGame() {
        if (!_isGameFinished.value && _cards.value.isNotEmpty()) {
            audioPlayer.playSound("sounds/game_over.mp3")
        }
        timerJob?.cancel()
        _cards.value = emptyList()
        _isGameFinished.value = false
        updateLevelInfo()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAll()
    }
}
