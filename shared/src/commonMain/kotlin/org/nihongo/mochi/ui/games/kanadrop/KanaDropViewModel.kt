package org.nihongo.mochi.ui.games.kanadrop

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.presentation.ViewModel
import kotlin.random.Random

class KanaDropViewModel(
    private val wordRepository: WordRepository,
    private val scoreRepository: ScoreRepository,
    private val levelContentProvider: LevelContentProvider,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(KanaDropGameState())
    val state: StateFlow<KanaDropGameState> = _state.asStateFlow()

    private val _history = MutableStateFlow<List<KanaLinkResult>>(emptyList())
    val history: StateFlow<List<KanaLinkResult>> = _history.asStateFlow()

    private var config = KanaDropConfig()
    private var allAvailableWordEntries: List<org.nihongo.mochi.domain.words.WordEntry> = emptyList()
    private var kanaPool: List<String> = emptyList()
    private var timerJob: Job? = null

    // Normalisation map for small kanas to large kanas
    private val kanaNormalizationMap = mapOf(
        'ぁ' to 'あ', 'ぃ' to 'い', 'ぅ' to 'う', 'ぇ' to 'え', 'ぉ' to 'お',
        'っ' to 'つ', 'ゃ' to 'や', 'ゅ' to 'ゆ', 'ょ' to 'よ', 'ゎ' to 'わ',
        'ァ' to 'ア', 'ィ' to 'イ', 'ゥ' to 'ウ', 'ェ' to 'エ', 'ォ' to 'オ',
        'ッ' to 'ツ', 'ャ' to 'ヤ', 'ュ' to 'ユ', 'ョ' to 'ヨ', 'ヮ' to 'ワ'
    )

    init {
        loadHistory()
    }

    private fun normalizeKana(text: String): String {
        return text.map { kanaNormalizationMap[it] ?: it }.joinToString("")
    }

    private fun loadHistory() {
        try {
            val historyJson = scoreRepository.getKanaLinkHistory()
            if (historyJson.isNotEmpty() && historyJson != "[]") {
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
            
            val allLevels = listOf("n5", "n4", "n3", "n2", "n1")
            val selectedLevelIndex = allLevels.indexOf(levelFileName.lowercase())
            val levelsToInclude = if (selectedLevelIndex != -1) {
                allLevels.take(selectedLevelIndex + 1)
            } else {
                listOf(levelFileName) 
            }

            val entries = mutableListOf<org.nihongo.mochi.domain.words.WordEntry>()
            levelsToInclude.forEach { lvl ->
                try {
                    entries.addAll(wordRepository.getWordEntriesForLevelSuspend(lvl))
                } catch (_: Exception) {}
            }
            allAvailableWordEntries = entries.distinctBy { it.text + it.phonetics }
            
            kanaPool = levelContentProvider.getKanaPoolForLevel(levelFileName)
                .filter { it.isNotBlank() && it.first() !in " ./()[]+-*=_\"'!?#%&" }

            generateInitialGrid()
            _state.value = _state.value.copy(
                isLoading = false,
                timeRemaining = config.initialTime,
                timeElapsed = 0,
                score = 0 
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
                        audioPlayer.playSound("sounds/game_over.mp3")
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
        
        viewModelScope.launch {
            try {
                scoreRepository.saveKanaLinkResult(result)
            } catch (e: Exception) {}
        }
    }

    private fun generateInitialGrid() {
        val tempGrid = MutableList(config.rows) { r ->
            MutableList(config.cols) { c ->
                KanaDropCell(id = "${r}_${c}_init", char = "", row = r, col = c)
            }
        }

        val wordsToInject = allAvailableWordEntries.shuffled().take(5).map { it.phonetics }
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

        val finalGrid = List(config.rows) { r ->
            List(config.cols) { c ->
                val cell = tempGrid[r][c]
                if (cell.char.isEmpty()) {
                    cell.copy(char = kanaPool.random(), id = "${r}_${c}_${Random.nextInt(999999)}")
                } else {
                    cell.copy(id = "${r}_${c}_${Random.nextInt(999999)}")
                }
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
        val rawWord = currentState.currentWord
        if (rawWord.isEmpty() || currentState.isGameOver) return
        
        viewModelScope.launch {
            val normalizedSelection = normalizeKana(rawWord)
            val validEntry = allAvailableWordEntries.find { 
                normalizeKana(it.phonetics) == normalizedSelection 
            }
            
            if (validEntry != null) {
                audioPlayer.playSound("sounds/correct.mp3")
                handleValidWord(validEntry)
            } else {
                audioPlayer.playSound("sounds/incorrect.mp3")
                val penalty = rawWord.length * 5 
                val newScore = (currentState.score - penalty).coerceAtLeast(0)
                
                _state.value = currentState.copy(
                    selectedCells = emptyList(),
                    currentWord = "",
                    score = newScore,
                    errorFlash = true
                )
                
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

        // DELAY before applying gravity to see the match
        viewModelScope.launch {
            delay(300)
            applyGravity()
        }
    }

    private fun applyGravity() {
        val currentState = _state.value
        val rows = config.rows
        val cols = config.cols
        val oldGrid = currentState.grid
        
        val colRemaining = Array(cols) { c ->
            val remaining = ArrayList<KanaDropCell>(rows)
            for (r in rows - 1 downTo 0) {
                val cell = oldGrid[r][c]
                if (!cell.isMatched) remaining.add(cell)
            }
            remaining 
        }

        val finalGrid = List(rows) { r ->
            List(cols) { c ->
                val remainingInCol = colRemaining[c]
                val numRemaining = remainingInCol.size
                val firstRemainingRow = rows - numRemaining
                
                if (r >= firstRemainingRow) {
                    val originalCell = remainingInCol[rows - 1 - r]
                    if (originalCell.row == r) originalCell else originalCell.copy(row = r)
                } else {
                    KanaDropCell(
                        id = "n_${r}_${c}_${Random.nextInt(999999)}",
                        char = kanaPool.random(),
                        row = r,
                        col = c
                    )
                }
            }
        }

        _state.value = currentState.copy(
            grid = finalGrid,
            selectedCells = emptyList(),
            currentWord = ""
        )
    }

    fun abandonGame() {
        timerJob?.cancel()
        if (_state.value.score > 0 && !_state.value.isGameOver) {
             audioPlayer.playSound("sounds/game_over.mp3")
        }
        _state.value = _state.value.copy(isGameOver = true)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAll()
    }
}
