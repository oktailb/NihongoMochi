package org.nihongo.mochi.ui.games.snake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.domain.words.WordEntry
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.services.StringProvider
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.settings.GAME_STATE_SNAKE
import org.nihongo.mochi.shared.generated.resources.*
import kotlin.random.Random

class SnakeViewModel(
    private val levelContentProvider: LevelContentProvider,
    private val wordRepository: WordRepository,
    private val scoreRepository: ScoreRepository,
    private val settingsRepository: SettingsRepository,
    private val audioPlayer: AudioPlayer,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val _gameState = MutableStateFlow(SnakeGameState())
    val gameState: StateFlow<SnakeGameState> = _gameState.asStateFlow()

    private val _selectedMode = MutableStateFlow(SnakeMode.HIRAGANA)
    val selectedMode: StateFlow<SnakeMode> = _selectedMode.asStateFlow()

    private val _scoresHistory = MutableStateFlow<List<SnakeGameResult>>(emptyList())
    val scoresHistory: StateFlow<List<SnakeGameResult>> = _scoresHistory.asStateFlow()

    private var gameJob: Job? = null
    private var timerJob: Job? = null
    
    private var itemSequence = emptyList<String>()
    private var currentWord: WordEntry? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        _scoresHistory.value = scoreRepository.getSnakeHistory()
    }

    private fun String.toHiragana(): String {
        return this.map { c ->
            if (c in '\u30A1'..'\u30F6') {
                (c.code - 0x60).toChar()
            } else {
                c
            }
        }.joinToString("")
    }

    fun onModeSelected(mode: SnakeMode) {
        _selectedMode.value = mode
    }

    fun onDirectionChanged(newDirection: Direction) {
        val currentDir = _gameState.value.direction
        val isOpposite = when (newDirection) {
            Direction.UP -> currentDir == Direction.DOWN
            Direction.DOWN -> currentDir == Direction.UP
            Direction.LEFT -> currentDir == Direction.RIGHT
            Direction.RIGHT -> currentDir == Direction.LEFT
        }
        if (!isOpposite && !_gameState.value.isPaused) {
            _gameState.update { it.copy(direction = newDirection) }
        }
    }

    fun startGame() {
        gameJob?.cancel()
        timerJob?.cancel()
        settingsRepository.clearGameState(GAME_STATE_SNAKE)
        
        viewModelScope.launch {
            prepareSequence()
            
            _gameState.update {
                SnakeGameState(
                    gridWidth = 15,
                    gridHeight = 22,
                    snake = listOf(Point(7, 10), Point(7, 11), Point(7, 12)),
                    direction = Direction.UP,
                    currentTargetLabel = it.currentTargetLabel,
                    mode = _selectedMode.value,
                    sequenceIndex = 0,
                    currentNumber = 1,
                    tickDelay = 220L 
                )
            }
            
            spawnItems()
            
            gameJob = viewModelScope.launch {
                gameLoop()
            }
            
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_gameState.value.isGameOver) {
                delay(1000)
                if (!_gameState.value.isPaused) {
                    _gameState.update { it.copy(timeSeconds = it.timeSeconds + 1) }
                }
            }
        }
    }

    private suspend fun prepareSequence() {
        when (_selectedMode.value) {
            SnakeMode.HIRAGANA -> {
                itemSequence = levelContentProvider.getCharactersForLevel("hiragana").map { it.toHiragana() }
                updateLabel(stringProvider.getString("game_snake_next_format", itemSequence.getOrNull(_gameState.value.sequenceIndex) ?: ""))
            }
            SnakeMode.KATAKANA -> {
                itemSequence = levelContentProvider.getCharactersForLevel("katakana").map { it.toHiragana() }
                updateLabel(stringProvider.getString("game_snake_next_format", itemSequence.getOrNull(_gameState.value.sequenceIndex) ?: ""))
            }
            SnakeMode.NUMBERS -> {
                val currentNum = _gameState.value.currentNumber
                itemSequence = listOf(convertToKanji(currentNum))
                updateLabel(stringProvider.getString("game_snake_next_format", itemSequence[0]))
            }
            SnakeMode.WORDS -> {
                pickNextWord()
            }
        }
    }

    private suspend fun pickNextWord() {
        val words = wordRepository.getWordEntriesForLevelSuspend("n5")
        currentWord = words.randomOrNull()
        currentWord?.let { word ->
            itemSequence = word.phonetics.toHiragana().map { it.toString() }.filter { it.isNotBlank() }
            _gameState.update { it.copy(sequenceIndex = 0) }
            updateLabel(stringProvider.getString("game_snake_word_format", word.text))
        }
    }

    private fun updateLabel(label: String) {
        _gameState.update { it.copy(currentTargetLabel = label) }
    }

    private fun spawnItems() {
        val state = _gameState.value
        val occupied = (state.snake).toSet()
        
        fun randomPoint(): Point {
            var p: Point
            do {
                p = Point(Random.nextInt(state.gridWidth), Random.nextInt(state.gridHeight))
            } while (occupied.contains(p))
            return p
        }

        val targetChar = itemSequence.getOrNull(state.sequenceIndex) ?: return
        val targetItem = SnakeItem(targetChar, randomPoint(), true)
        
        val distractions = mutableListOf<SnakeItem>()
        val pool = when(_selectedMode.value) {
            SnakeMode.NUMBERS -> listOf(convertToKanji(state.currentNumber + 1), convertToKanji(state.currentNumber + Random.nextInt(2, 10)))
            SnakeMode.WORDS -> itemSequence.filterIndexed { index, _ -> index != state.sequenceIndex }
            else -> itemSequence.shuffled().take(5)
        }
        
        repeat(2) {
            val distChar = if (pool.isNotEmpty()) pool.random() else "？"
            distractions.add(SnakeItem(distChar, randomPoint(), false))
        }

        _gameState.update { it.copy(targetItem = targetItem, distractions = distractions) }
    }

    private suspend fun gameLoop() {
        while (!_gameState.value.isGameOver) {
            if (!_gameState.value.isPaused) {
                moveSnake()
            }
            delay(_gameState.value.tickDelay)
        }
    }

    private suspend fun moveSnake() {
        val state = _gameState.value
        val head = state.snake.first()
        val nextHead = when (state.direction) {
            Direction.UP -> Point(head.x, head.y - 1)
            Direction.DOWN -> Point(head.x, head.y + 1)
            Direction.LEFT -> Point(head.x - 1, head.y)
            Direction.RIGHT -> Point(head.x + 1, head.y)
        }

        if (nextHead.x < 0 || nextHead.x >= state.gridWidth || nextHead.y < 0 || nextHead.y >= state.gridHeight) {
            gameOver()
            return
        }

        if (state.snake.contains(nextHead)) {
            gameOver()
            return
        }

        var grew = false
        if (nextHead == state.targetItem?.position) {
            audioPlayer.playSound("files/sounds/correct.mp3")
            grew = true
            val nextSequenceIndex = state.sequenceIndex + 1
            _gameState.update { it.copy(score = it.score + 10, sequenceIndex = nextSequenceIndex) }
            
            if (nextSequenceIndex >= itemSequence.size) {
                if (_selectedMode.value == SnakeMode.WORDS) {
                    _gameState.update { it.copy(wordsCompleted = it.wordsCompleted + 1) }
                    pickNextWord()
                } else if (_selectedMode.value == SnakeMode.NUMBERS) {
                    val nextNum = state.currentNumber + 1
                    _gameState.update { it.copy(currentNumber = nextNum, sequenceIndex = 0, tickDelay = (state.tickDelay * 0.98).toLong().coerceAtLeast(100L)) }
                    itemSequence = listOf(convertToKanji(nextNum))
                } else {
                    _gameState.update { it.copy(sequenceIndex = 0, tickDelay = (state.tickDelay * 0.95).toLong().coerceAtLeast(100L)) }
                }
            }
            
            val labelText = when(_selectedMode.value) {
                SnakeMode.WORDS -> stringProvider.getString("game_snake_word_format", currentWord?.text ?: "")
                SnakeMode.NUMBERS -> stringProvider.getString("game_snake_next_format", itemSequence[0])
                else -> stringProvider.getString("game_snake_next_format", itemSequence.getOrNull(_gameState.value.sequenceIndex) ?: "")
            }
            updateLabel(labelText)
            spawnItems()
        } else if (state.distractions.any { it.position == nextHead }) {
            gameOver()
            return
        }

        val newSnake = mutableListOf(nextHead)
        newSnake.addAll(if (grew) state.snake else state.snake.dropLast(1))
        
        _gameState.update { it.copy(snake = newSnake) }
    }

    private fun gameOver() {
        if (_gameState.value.isGameOver) return
        
        timerJob?.cancel()
        settingsRepository.clearGameState(GAME_STATE_SNAKE)
        audioPlayer.playSound("files/sounds/game_over.mp3")
        _gameState.update { it.copy(isGameOver = true) }
        
        val result = SnakeGameResult(
            mode = _selectedMode.value,
            score = _gameState.value.score,
            wordsCompleted = _gameState.value.wordsCompleted,
            timeSeconds = _gameState.value.timeSeconds,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        scoreRepository.saveSnakeResult(result)
        loadHistory()
    }

    private fun convertToKanji(n: Int): String {
        if (n == 0) return "零"
        val units = listOf("", "一", "二", "三", "四", "五", "六", "七", "八", "九")
        val positions = listOf("", "十", "百", "千")
        
        var num = n
        var res = ""
        var pos = 0
        
        while (num > 0) {
            val digit = num % 10
            if (digit > 0) {
                val p = positions[pos]
                val u = if (digit == 1 && pos > 0) "" else units[digit]
                res = u + p + res
            }
            num /= 10
            pos++
        }
        return res
    }

    fun pauseGame() {
        _gameState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        _gameState.update { it.copy(isPaused = false) }
    }

    fun abandonGame() {
        gameOver()
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
        timerJob?.cancel()
        audioPlayer.stopAll()
    }
}
