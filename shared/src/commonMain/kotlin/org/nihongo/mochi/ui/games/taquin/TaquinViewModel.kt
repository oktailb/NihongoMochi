package org.nihongo.mochi.ui.games.taquin

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaType
import org.nihongo.mochi.presentation.ViewModel
import org.nihongo.mochi.domain.kana.KanaEntry
import kotlinx.serialization.Serializable

@Serializable
data class NumberEntry(val character: String, val romaji: String, val value: Int)
@Serializable
data class NumberData(val numbers: List<NumberEntry>)

class TaquinViewModel(
    private val kanaRepository: KanaRepository,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    // --- Setup State ---
    private val _selectedMode = MutableStateFlow(TaquinMode.HIRAGANA)
    val selectedMode: StateFlow<TaquinMode> = _selectedMode.asStateFlow()

    private val _selectedRows = MutableStateFlow(3)
    val selectedRows: StateFlow<Int> = _selectedRows.asStateFlow()

    private val _scoresHistory = MutableStateFlow<List<TaquinGameResult>>(emptyList())
    val scoresHistory: StateFlow<List<TaquinGameResult>> = _scoresHistory.asStateFlow()

    // --- Game State ---
    private val _gameState = MutableStateFlow<TaquinGameState?>(null)
    val gameState: StateFlow<TaquinGameState?> = _gameState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadScoresHistory()
    }

    private fun loadScoresHistory() {
        try {
            val historyJson = scoreRepository.getTaquinHistory()
            if (historyJson.isNotEmpty()) {
                _scoresHistory.value = json.decodeFromString(historyJson)
            }
        } catch (e: Exception) {
            _scoresHistory.value = emptyList()
        }
    }

    fun onModeSelected(mode: TaquinMode) {
        _selectedMode.value = mode
        if (mode == TaquinMode.NUMBERS) {
             _selectedRows.value = 4 // Numbers fixed to 4x4 (15 pieces)
        }
    }

    fun onRowsSelected(rows: Int) {
        _selectedRows.value = rows
    }

    fun startGame() {
        viewModelScope.launch {
            val rows = _selectedRows.value
            val cols = if (_selectedMode.value == TaquinMode.NUMBERS) 4 else 5
            
            val solvedPieces = mutableListOf<TaquinPiece>()
            
            if (_selectedMode.value != TaquinMode.NUMBERS) {
                val entries = kanaRepository.getKanaEntriesSuspend(
                    if (_selectedMode.value == TaquinMode.HIRAGANA) KanaType.HIRAGANA else KanaType.KATAKANA
                ).filter { it.line <= rows }

                for (r in 1..rows) {
                    for (c in 1..cols) {
                        val entry = entries.find { it.line == r && it.column == c }
                        if (r == rows && c == cols) {
                            solvedPieces.add(TaquinPiece("", r, c, isBlank = true))
                        } else if (entry != null) {
                            solvedPieces.add(TaquinPiece(entry.character, r, c))
                        } else {
                            solvedPieces.add(TaquinPiece("", 0, 0)) 
                        }
                    }
                }
            } else {
                // Load from numbers.json
                val numberEntries = kanaRepository.getNumberEntries()
                numberEntries.take(15).forEachIndexed { i, entry ->
                    solvedPieces.add(TaquinPiece(entry.character, (i / 4) + 1, (i % 4) + 1))
                }
                // Ensure we have exactly 15 pieces + 1 blank even if JSON is short
                while (solvedPieces.size < 15) {
                    solvedPieces.add(TaquinPiece("", 0, 0))
                }
                solvedPieces.add(TaquinPiece("", 4, 4, isBlank = true))
            }

            var shuffledPieces = solvedPieces.toList()
            val shuffleMoves = rows * cols * 20
            repeat(shuffleMoves) {
                val blankIndex = shuffledPieces.indexOfFirst { it.isBlank }
                val bRow = blankIndex / cols
                val bCol = blankIndex % cols
                
                val neighbors = mutableListOf<Int>()
                if (bRow > 0) neighbors.add(blankIndex - cols)
                if (bRow < rows - 1) neighbors.add(blankIndex + cols)
                if (bCol > 0) neighbors.add(blankIndex - 1)
                if (bCol < cols - 1) neighbors.add(blankIndex + 1)
                
                val moveIndex = neighbors.random()
                shuffledPieces = swapped(shuffledPieces, blankIndex, moveIndex)
            }

            _gameState.value = TaquinGameState(
                pieces = shuffledPieces,
                rows = rows,
                cols = cols
            )
            
            startTimer()
        }
    }

    private fun swapped(list: List<TaquinPiece>, i: Int, j: Int): List<TaquinPiece> {
        val res = list.toMutableList()
        val temp = res[i]
        res[i] = res[j]
        res[j] = temp
        return res
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _gameState.value = _gameState.value?.let { it.copy(timeSeconds = it.timeSeconds + 1) }
            }
        }
    }

    fun onPieceClicked(clickedIndex: Int) {
        val state = _gameState.value ?: return
        if (state.isSolved) return

        val blankIndex = state.pieces.indexOfFirst { it.isBlank }
        if (blankIndex == -1) return

        val clickedRow = clickedIndex / state.cols
        val clickedCol = clickedIndex % state.cols
        val blankRow = blankIndex / state.cols
        val blankCol = blankIndex % state.cols

        if (clickedRow == blankRow || clickedCol == blankCol) {
            val newPieces = state.pieces.toMutableList()
            
            if (clickedRow == blankRow) {
                val step = if (clickedCol < blankCol) 1 else -1
                var curr = blankIndex
                while (curr != clickedIndex) {
                    val next = curr - step
                    newPieces[curr] = newPieces[next]
                    curr = next
                }
                newPieces[clickedIndex] = state.pieces[blankIndex]
            } else {
                val step = if (clickedRow < blankRow) 1 else -1
                var curr = blankIndex
                while (curr != clickedIndex) {
                    val next = curr - (step * state.cols)
                    newPieces[curr] = newPieces[next]
                    curr = next
                }
                newPieces[clickedIndex] = state.pieces[blankIndex]
            }

            val newState = state.copy(
                pieces = newPieces,
                moves = state.moves + 1
            )
            
            _gameState.value = newState
            checkSolved(newState)
        }
    }

    private fun checkSolved(state: TaquinGameState) {
        val isSolved = state.pieces.all { piece ->
            val currentIndex = state.pieces.indexOf(piece)
            val currentRow = (currentIndex / state.cols) + 1
            val currentCol = (currentIndex % state.cols) + 1
            
            if (piece.isBlank || piece.character.isEmpty()) {
                true 
            } else {
                piece.targetLine == currentRow && piece.targetColumn == currentCol
            }
        }

        if (isSolved) {
            timerJob?.cancel()
            _gameState.value = state.copy(isSolved = true)
            saveResult()
        }
    }

    private fun saveResult() {
        val state = _gameState.value ?: return
        val result = TaquinGameResult(
            mode = _selectedMode.value,
            rows = state.rows,
            moves = state.moves,
            timeSeconds = state.timeSeconds,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        
        val newHistory = (listOf(result) + _scoresHistory.value).take(10)
        _scoresHistory.value = newHistory
        try {
            scoreRepository.saveTaquinHistory(json.encodeToString(newHistory))
        } catch (e: Exception) {}
    }

    fun abandonGame() {
        timerJob?.cancel()
        _gameState.value = null
    }
}
