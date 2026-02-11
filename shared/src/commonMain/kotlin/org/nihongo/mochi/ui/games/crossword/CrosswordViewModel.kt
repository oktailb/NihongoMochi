package org.nihongo.mochi.ui.games.crossword

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.domain.meaning.WordMeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.presentation.ViewModel
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.settings.GAME_STATE_CROSSWORD

class CrosswordViewModel(
    private val wordRepository: WordRepository,
    private val wordMeaningRepository: WordMeaningRepository,
    private val scoreRepository: ScoreRepository,
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _selectedMode = MutableStateFlow(CrosswordMode.KANAS)
    val selectedMode: StateFlow<CrosswordMode> = _selectedMode.asStateFlow()

    private val _selectedHintType = MutableStateFlow(CrosswordHintType.KANJI)
    val selectedHintType: StateFlow<CrosswordHintType> = _selectedHintType.asStateFlow()

    private val _wordCount = MutableStateFlow(10)
    val wordCount: StateFlow<Int> = _wordCount.asStateFlow()

    private val _scoresHistory = MutableStateFlow<List<CrosswordGameResult>>(emptyList())
    val scoresHistory: StateFlow<List<CrosswordGameResult>> = _scoresHistory.asStateFlow()

    private val _cells = MutableStateFlow<List<CrosswordCell>>(emptyList())
    val cells: StateFlow<List<CrosswordCell>> = _cells.asStateFlow()

    private val _placedWords = MutableStateFlow<List<CrosswordWord>>(emptyList())
    val placedWords: StateFlow<List<CrosswordWord>> = _placedWords.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _gameTimeSeconds = MutableStateFlow(0)
    val gameTimeSeconds: StateFlow<Int> = _gameTimeSeconds.asStateFlow()

    private val _selectedCell = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedCell: StateFlow<Pair<Int, Int>?> = _selectedCell.asStateFlow()

    private val _isVerticalInput = MutableStateFlow(false)
    val isVerticalInput: StateFlow<Boolean> = _isVerticalInput.asStateFlow()

    private val _keyboardKeys = MutableStateFlow<List<String>>(emptyList())
    val keyboardKeys: StateFlow<List<String>> = _keyboardKeys.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _hasSavedGame = MutableStateFlow(false)
    val hasSavedGame: StateFlow<Boolean> = _hasSavedGame.asStateFlow()

    private var timerJob: Job? = null
    private var lastActiveWordId: Int? = null

    init {
        loadHistory()
        checkSavedGame()
    }

    private fun checkSavedGame() {
        _hasSavedGame.value = settingsRepository.getGameState(GAME_STATE_CROSSWORD) != null
    }

    fun restoreGame(onRestored: () -> Unit) {
        val savedJson = settingsRepository.getGameState(GAME_STATE_CROSSWORD) ?: return
        try {
            val restored = Json.decodeFromString<CrosswordGameState>(savedJson)
            if (!restored.isFinished) {
                _cells.value = restored.cells
                _placedWords.value = restored.placedWords
                _gameTimeSeconds.value = restored.gameTimeSeconds
                _selectedMode.value = restored.selectedMode
                _selectedHintType.value = restored.selectedHintType
                _isFinished.value = restored.isFinished
                _isPaused.value = true
                startTimer()
                onRestored()
            }
        } catch (e: Exception) {
            settingsRepository.clearGameState(GAME_STATE_CROSSWORD)
            _hasSavedGame.value = false
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                _scoresHistory.value = scoreRepository.getCrosswordHistory()
            } catch (e: Exception) {
                _scoresHistory.value = emptyList()
            }
        }
    }

    fun onModeSelected(mode: CrosswordMode) { _selectedMode.value = mode }
    fun onHintTypeSelected(hintType: CrosswordHintType) { _selectedHintType.value = hintType }
    fun onWordCountSelected(count: Int) { _wordCount.value = count }

    private fun cleanPhonetics(p: String): String {
        return p.split("/").firstOrNull { it.isNotBlank() }?.replace(".", "")?.replace(" ", "") ?: ""
    }

    fun onCellSelected(r: Int, c: Int, preserveOrientation: Boolean = false) {
        if (_isFinished.value || _isPaused.value) return
        
        val words = getWordsAt(r, c)
        if (words.isEmpty()) return

        if (_selectedCell.value == r to c && !preserveOrientation) {
            if (words.size > 1) {
                _isVerticalInput.value = !_isVerticalInput.value
            }
        } else if (!preserveOrientation) {
            val currentVertical = _isVerticalInput.value
            val hasHorizontal = words.any { it.isHorizontal }
            val hasVertical = words.any { !it.isHorizontal }

            if (currentVertical && hasVertical) {
                _isVerticalInput.value = true
            } else if (!currentVertical && hasHorizontal) {
                _isVerticalInput.value = false
            } else {
                _isVerticalInput.value = hasVertical && !hasHorizontal
            }
        }
        
        _selectedCell.value = r to c
        updateKeyboardKeys(r, c)
    }

    private fun updateKeyboardKeys(r: Int, c: Int) {
        val activeWord = getActiveWordAt(r, c) ?: return
        
        if (activeWord.number == lastActiveWordId && _keyboardKeys.value.isNotEmpty()) {
            return
        }
        
        lastActiveWordId = activeWord.number
        val wordChars = activeWord.word.map { it.toString() }.toSet()
        val random = kotlin.random.Random(activeWord.number + activeWord.word.hashCode())
        
        val disturbersCount = (15 - wordChars.size).coerceAtLeast(0)
        
        val disturbers = if (_selectedMode.value == CrosswordMode.KANJIS) {
            val otherKanjis = _placedWords.value.flatMap { it.word.map { c -> c.toString() } }.toSet()
            if (otherKanjis.size > disturbersCount + 2) {
                otherKanjis.shuffled(random).take(disturbersCount).toSet()
            } else {
                "日一国会人年大十二本中長出三同時政自前者".map { it.toString() }.shuffled(random).take(disturbersCount).toSet()
            }
        } else {
            val allKanas = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわをん"
            (1..disturbersCount).map { allKanas[random.nextInt(allKanas.length)].toString() }.toSet()
        }
        
        val combined = (wordChars + disturbers).toList().shuffled(random)
        _keyboardKeys.value = if (combined.size > 15) combined.take(15) else if (combined.size < 15) {
            val allKanas = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわをん"
            val needed = 15 - combined.size
            combined + (1..needed).map { allKanas[random.nextInt(allKanas.length)].toString() }
        } else combined
    }

    private fun getWordsAt(r: Int, c: Int): List<CrosswordWord> {
        return _placedWords.value.filter { word ->
            if (word.isHorizontal) {
                word.row == r && c >= word.col && c < word.col + word.word.length
            } else {
                word.col == c && r >= word.row && r < word.row + word.word.length
            }
        }
    }

    private fun getActiveWordAt(r: Int, c: Int): CrosswordWord? {
        val words = getWordsAt(r, c)
        return if (_isVerticalInput.value) {
            words.find { !it.isHorizontal } ?: words.firstOrNull()
        } else {
            words.find { it.isHorizontal } ?: words.firstOrNull()
        }
    }

    fun onKeyTyped(char: String) {
        if (_isFinished.value || _isPaused.value) return
        val currentPos = _selectedCell.value ?: return
        val currentCells = _cells.value.toMutableList()
        val index = currentCells.indexOfFirst { it.r == currentPos.first && it.c == currentPos.second }
        
        if (index != -1 && !currentCells[index].isCorrect) {
            val isLetterCorrect = char == currentCells[index].solution
            if (isLetterCorrect) {
                audioPlayer.playSound("sounds/correct.mp3")
            } else {
                audioPlayer.playSound("sounds/incorrect.mp3")
            }

            currentCells[index] = currentCells[index].copy(userInput = char)
            _cells.value = currentCells
            
            checkWordCompletion(currentPos.first, currentPos.second)
            moveToNextCell(currentPos.first, currentPos.second)
        }
    }

    private fun moveToNextCell(r: Int, c: Int) {
        val word = getActiveWordAt(r, c) ?: return
        if (word.isHorizontal) {
            if (c + 1 < word.col + word.word.length) {
                onCellSelected(r, c + 1, preserveOrientation = true)
            }
        } else {
            if (r + 1 < word.row + word.word.length) {
                onCellSelected(r + 1, c, preserveOrientation = true)
            }
        }
    }

    fun onDelete() {
        if (_isFinished.value || _isPaused.value) return
        val currentPos = _selectedCell.value ?: return
        val currentCells = _cells.value.toMutableList()
        val index = currentCells.indexOfFirst { it.r == currentPos.first && it.c == currentPos.second }
        if (index != -1 && !currentCells[index].isCorrect) {
            currentCells[index] = currentCells[index].copy(userInput = "")
            _cells.value = currentCells
        }
    }

    private fun checkWordCompletion(r: Int, c: Int) {
        val words = getWordsAt(r, c)
        words.forEach { word ->
            val wordCells = _cells.value.filter { cell ->
                if (word.isHorizontal) {
                    cell.r == word.row && cell.c >= word.col && cell.c < word.col + word.word.length
                } else {
                    cell.c == word.col && cell.r >= word.row && cell.r < word.row + word.word.length
                }
            }.sortedBy { if (word.isHorizontal) it.c else it.r }

            if (wordCells.joinToString("") { it.userInput } == word.word) {
                markWordAsCorrect(word, wordCells)
                checkGameFinished()
            }
        }
    }

    private fun markWordAsCorrect(word: CrosswordWord, wordCells: List<CrosswordCell>) {
        _cells.value = _cells.value.map { cell ->
            if (wordCells.any { it.r == cell.r && it.c == cell.c }) cell.copy(isCorrect = true) else cell
        }
        _placedWords.value = _placedWords.value.map {
            if (it.number == word.number) it.copy(isSolved = true) else it
        }
    }

    private fun checkGameFinished() {
        if (_placedWords.value.isNotEmpty() && _placedWords.value.all { it.isSolved }) {
            _isFinished.value = true
            timerJob?.cancel()
            settingsRepository.clearGameState(GAME_STATE_CROSSWORD)
            _hasSavedGame.value = false
            saveGameResult()
        }
    }

    private fun saveGameResult() {
        val result = CrosswordGameResult(
            wordCount = _placedWords.value.size,
            mode = _selectedMode.value,
            timeSeconds = _gameTimeSeconds.value,
            completionPercentage = 100,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        viewModelScope.launch {
            try {
                scoreRepository.saveCrosswordResult(result)
                loadHistory()
            } catch (e: Exception) {}
        }
    }

    fun startGame(onGenerated: () -> Unit) {
        viewModelScope.launch {
            _isGenerating.value = true
            _isFinished.value = false
            _isPaused.value = false
            _hasSavedGame.value = false
            settingsRepository.clearGameState(GAME_STATE_CROSSWORD)
            lastActiveWordId = null
            
            val result = withContext(Dispatchers.Default) {
                val currentLevelId = settingsRepository.getSelectedLevel().ifEmpty { "n5" }
                val locale = settingsRepository.getAppLocale()
                val meanings = wordMeaningRepository.getWordMeanings(locale)
                
                var availableWords = wordRepository.getWordEntriesForLevelSuspend(currentLevelId)
                if (availableWords.isEmpty()) availableWords = wordRepository.getWordEntriesForLevelSuspend("n5")
                
                val generator = CrosswordGenerator(availableWords, _wordCount.value, _selectedMode.value)
                val (genCells, wordsWithoutMeanings) = generator.generate()
                
                val finalWords = wordsWithoutMeanings.map { cw ->
                    val originalEntry = availableWords.find { entry ->
                        if (_selectedMode.value == CrosswordMode.KANJIS) {
                            entry.text == cw.word
                        } else {
                            cleanPhonetics(entry.phonetics) == cw.word
                        }
                    }
                    val realMeaning = originalEntry?.let { meanings[it.id] }
                    cw.copy(meaning = realMeaning ?: "") 
                }
                Pair(genCells, finalWords)
            }
            
            _cells.value = result.first
            _placedWords.value = result.second
            _isGenerating.value = false
            _gameTimeSeconds.value = 0
            startTimer()
            onGenerated()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_isFinished.value) {
                delay(1000)
                if (!_isPaused.value) {
                    _gameTimeSeconds.value += 1
                }
            }
        }
    }

    fun pauseGame() {
        _isPaused.value = true
    }

    fun resumeGame() {
        _isPaused.value = false
    }

    fun saveAndExit() {
        val state = CrosswordGameState(
            cells = _cells.value,
            placedWords = _placedWords.value,
            gameTimeSeconds = _gameTimeSeconds.value,
            selectedMode = _selectedMode.value,
            selectedHintType = _selectedHintType.value,
            isFinished = _isFinished.value
        )
        val json = Json.encodeToString(CrosswordGameState.serializer(), state)
        settingsRepository.saveGameState(GAME_STATE_CROSSWORD, json)
        _hasSavedGame.value = true
    }

    fun abandonGame() { 
        timerJob?.cancel()
        settingsRepository.clearGameState(GAME_STATE_CROSSWORD)
        _hasSavedGame.value = false
        if (!_isFinished.value) {
            audioPlayer.playSound("sounds/game_over.mp3")
        }
    }
}
