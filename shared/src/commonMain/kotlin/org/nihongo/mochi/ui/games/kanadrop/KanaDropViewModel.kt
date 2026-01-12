package org.nihongo.mochi.ui.games.kanadrop

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.presentation.ViewModel
import kotlin.random.Random

class KanaDropViewModel(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(KanaDropGameState())
    val state: StateFlow<KanaDropGameState> = _state.asStateFlow()

    private val _history = MutableStateFlow<List<KanaLinkResult>>(emptyList())
    val history: StateFlow<List<KanaLinkResult>> = _history.asStateFlow()

    private var config = KanaDropConfig()
    private var availableWords: List<String> = emptyList()
    private var kanaPool: List<String> = emptyList()
    private var timerJob: Job? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        try {
            val historyJson = ScoreManager.getKanaLinkHistory()
            if (historyJson.isNotEmpty()) {
                _history.value = json.decodeFromString(historyJson)
            }
        } catch (e: Exception) {
            _history.value = emptyList()
        }
    }

    fun initGame(levelFileName: String, mode: KanaLinkMode = KanaLinkMode.TIME_ATTACK) {
        viewModelScope.launch {
            _state.value = KanaDropGameState(isLoading = true)
            config = KanaDropConfig(
                levelFileName = levelFileName,
                mode = mode,
                initialTime = if (mode == KanaLinkMode.TIME_ATTACK) 60 else 0
            )
            
            val entries = wordRepository.getWordEntriesForLevelSuspend(levelFileName)
            availableWords = entries.map { it.phonetics }
            
            kanaPool = availableWords.flatMap { word -> 
                word.map { it.toString() } 
            }.distinct().filter { it.isNotBlank() && it != "/" }

            if (kanaPool.isEmpty()) {
                kanaPool = listOf("あ", "い", "う", "え", "お")
            }

            generateInitialGrid()
            _state.value = _state.value.copy(
                isLoading = false,
                timeRemaining = config.initialTime,
                timeElapsed = 0,
                score = 100 
            )
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_state.value.isGameOver) {
                delay(1000)
                val currentState = _state.value
                val newElapsed = currentState.timeElapsed + 1
                
                if (config.mode == KanaLinkMode.TIME_ATTACK) {
                    val newTime = currentState.timeRemaining - 1
                    if (newTime <= 0) {
                        _state.value = currentState.copy(timeRemaining = 0, timeElapsed = newElapsed)
                        endGame()
                    } else {
                        _state.value = currentState.copy(timeRemaining = newTime, timeElapsed = newElapsed)
                    }
                } else {
                    _state.value = currentState.copy(timeRemaining = 0, timeElapsed = newElapsed)
                }
            }
        }
    }

    private fun endGame() {
        timerJob?.cancel()
        val currentState = _state.value
        _state.value = currentState.copy(isGameOver = true)
        saveResult()
    }

    private fun saveResult() {
        val s = _state.value
        val result = KanaLinkResult(
            score = s.score,
            wordsFound = s.wordsFound,
            timeSeconds = s.timeElapsed,
            levelId = config.levelFileName,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        
        val newHistory = (listOf(result) + _history.value).take(10)
        _history.value = newHistory
        try {
            ScoreManager.saveKanaLinkHistory(json.encodeToString(newHistory))
        } catch (e: Exception) {}
    }

    private fun generateInitialGrid() {
        val tempGrid = MutableList(config.rows) { r ->
            MutableList(config.cols) { c ->
                KanaDropCell(id = "${r}_${c}_init", char = "", row = r, col = c)
            }
        }

        val wordsToInject = availableWords.shuffled().take(3)
        wordsToInject.forEach { word ->
            var placed = false
            var attempts = 0
            while (!placed && attempts < 10) {
                val startRow = Random.nextInt(config.rows)
                val startCol = Random.nextInt(config.cols)
                val direction = Random.nextInt(2) 
                
                if (direction == 0 && startCol + word.length <= config.cols) {
                    val canPlace = (0 until word.length).all { tempGrid[startRow][startCol + it].char.isEmpty() }
                    if (canPlace) {
                        word.forEachIndexed { i, char ->
                            tempGrid[startRow][startCol + i] = tempGrid[startRow][startCol + i].copy(char = char.toString())
                        }
                        placed = true
                    }
                } else if (direction == 1 && startRow + word.length <= config.rows) {
                    val canPlace = (0 until word.length).all { tempGrid[startRow + it][startCol].char.isEmpty() }
                    if (canPlace) {
                        word.forEachIndexed { i, char ->
                            tempGrid[startRow + i][startCol] = tempGrid[startRow + i][startCol].copy(char = char.toString())
                        }
                        placed = true
                    }
                }
                attempts++
            }
        }

        val finalGrid = tempGrid.map { row ->
            row.map { cell ->
                if (cell.char.isEmpty()) cell.copy(char = kanaPool.random(), id = "${cell.row}_${cell.col}_${Random.nextInt()}")
                else cell.copy(id = "${cell.row}_${cell.col}_${Random.nextInt()}")
            }
        }
        
        _state.value = _state.value.copy(grid = finalGrid)
    }

    fun onCellTouched(row: Int, col: Int) {
        val currentState = _state.value
        if (currentState.isGameOver || currentState.isLoading) return
        
        val cell = currentState.grid.getOrNull(row)?.getOrNull(col) ?: return
        val selected = currentState.selectedCells
        
        if (selected.isEmpty()) {
            _state.value = currentState.copy(selectedCells = listOf(cell), currentWord = cell.char)
            return
        }

        if (selected.last().id == cell.id) return

        if (selected.size >= 2 && selected[selected.size - 2].id == cell.id) {
            val newSelection = selected.dropLast(1)
            _state.value = currentState.copy(
                selectedCells = newSelection,
                currentWord = newSelection.joinToString("") { it.char }
            )
            return
        }

        val last = selected.last()
        val isAdjacent = kotlin.math.abs(last.row - cell.row) <= 1 && 
                         kotlin.math.abs(last.col - cell.col) <= 1
        
        if (isAdjacent && !selected.any { it.id == cell.id }) {
            val newSelection = selected + cell
            _state.value = currentState.copy(
                selectedCells = newSelection,
                currentWord = currentState.currentWord + cell.char
            )
        }
    }

    fun onReleaseSelection() {
        val currentState = _state.value
        val word = currentState.currentWord
        if (word.isEmpty() || currentState.isGameOver) return
        
        viewModelScope.launch {
            val validEntry = wordRepository.getAllWordEntriesSuspend().find { it.phonetics == word }
            
            if (validEntry != null) {
                handleValidWord(validEntry)
            } else {
                val penalty = word.length * 10 
                val newScore = (currentState.score - penalty).coerceAtLeast(0)
                
                _state.value = currentState.copy(
                    selectedCells = emptyList(),
                    currentWord = "",
                    score = newScore,
                    errorFlash = true
                )
                
                if (newScore <= 0) {
                    endGame()
                }

                delay(300)
                _state.value = _state.value.copy(errorFlash = false)
            }
        }
    }

    private fun handleValidWord(entry: org.nihongo.mochi.domain.words.WordEntry) {
        val currentState = _state.value
        val selectedIds = currentState.selectedCells.map { it.id }.toSet()
        
        val newGrid = currentState.grid.map { row ->
            row.map { cell ->
                if (selectedIds.contains(cell.id)) cell.copy(isMatched = true) else cell
            }
        }

        val wordPoints = entry.phonetics.length * 10
        val timeBonus = if (config.mode == KanaLinkMode.TIME_ATTACK) entry.phonetics.length else 0
        
        _state.value = currentState.copy(
            grid = newGrid,
            score = currentState.score + wordPoints,
            wordsFound = currentState.wordsFound + 1,
            timeRemaining = currentState.timeRemaining + timeBonus,
            lastValidWord = entry
        )

        applyGravity()
    }

    private fun applyGravity() {
        val currentState = _state.value
        val rows = config.rows
        val cols = config.cols
        
        val newGridData = MutableList(rows) { MutableList<KanaDropCell?>(cols) { null } }
        
        for (c in 0 until cols) {
            var targetRow = rows - 1
            for (r in rows - 1 downTo 0) {
                val cell = currentState.grid[r][c]
                if (!cell.isMatched) {
                    newGridData[targetRow][c] = cell.copy(row = targetRow, col = c)
                    targetRow--
                }
            }
            while (targetRow >= 0) {
                newGridData[targetRow][c] = KanaDropCell(
                    id = "new_${targetRow}_${c}_${Random.nextInt()}",
                    char = kanaPool.random(),
                    row = targetRow,
                    col = c
                )
                targetRow--
            }
        }

        _state.value = _state.value.copy(
            grid = newGridData.map { it.filterNotNull() },
            selectedCells = emptyList(),
            currentWord = ""
        )
    }

    fun abandonGame() {
        timerJob?.cancel()
        _state.value = _state.value.copy(isGameOver = true)
    }
}
