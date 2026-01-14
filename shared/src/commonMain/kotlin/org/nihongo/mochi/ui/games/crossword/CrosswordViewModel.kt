package org.nihongo.mochi.ui.games.crossword

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.statistics.StatisticsType
import org.nihongo.mochi.presentation.ViewModel
import org.nihongo.mochi.domain.services.AudioPlayer

class CrosswordViewModel(
    private val wordRepository: WordRepository,
    private val meaningRepository: MeaningRepository,
    private val scoreRepository: ScoreRepository,
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    // --- Setup State ---
    private val _selectedMode = MutableStateFlow(CrosswordMode.KANAS)
    val selectedMode: StateFlow<CrosswordMode> = _selectedMode.asStateFlow()

    private val _selectedHintType = MutableStateFlow(CrosswordHintType.KANJI)
    val selectedHintType: StateFlow<CrosswordHintType> = _selectedHintType.asStateFlow()

    private val _wordCount = MutableStateFlow(10)
    val wordCount: StateFlow<Int> = _wordCount.asStateFlow()

    private val _scoresHistory = MutableStateFlow<List<CrosswordGameResult>>(emptyList())
    val scoresHistory: StateFlow<List<CrosswordGameResult>> = _scoresHistory.asStateFlow()

    // --- Game State ---
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

    private var timerJob: Job? = null

    fun onModeSelected(mode: CrosswordMode) { _selectedMode.value = mode }
    fun onHintTypeSelected(hintType: CrosswordHintType) { _selectedHintType.value = hintType }
    fun onWordCountSelected(count: Int) { _wordCount.value = count }

    fun onCellSelected(r: Int, c: Int) {
        if (_selectedCell.value == r to c) {
            val words = getWordsAt(r, c)
            if (words.size > 1) {
                _isVerticalInput.value = !_isVerticalInput.value
            }
        } else {
            _selectedCell.value = r to c
            val words = getWordsAt(r, c)
            if (words.any { !it.isHorizontal } && !words.any { it.isHorizontal }) {
                _isVerticalInput.value = true
            } else if (words.any { it.isHorizontal }) {
                _isVerticalInput.value = false
            }
        }
        updateKeyboardKeys(r, c)
    }

    private fun updateKeyboardKeys(r: Int, c: Int) {
        val word = getActiveWordAt(r, c) ?: return
        val random = kotlin.random.Random(word.word.hashCode())
        val allKanas = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわをん"
        val disturbers = (1..5).map { allKanas[random.nextInt(allKanas.length)].toString() }
        _keyboardKeys.value = (word.word.map { it.toString() }.toSet() + disturbers).shuffled(random)
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
        val currentPos = _selectedCell.value ?: return
        val currentCells = _cells.value.toMutableList()
        val index = currentCells.indexOfFirst { it.r == currentPos.first && it.c == currentPos.second }
        
        if (index != -1 && !currentCells[index].isCorrect) {
            currentCells[index] = currentCells[index].copy(userInput = char)
            _cells.value = currentCells
            
            checkWordCompletion(currentPos.first, currentPos.second)
            moveToNextCell(currentPos.first, currentPos.second)
        }
    }

    private fun moveToNextCell(r: Int, c: Int) {
        val word = getActiveWordAt(r, c) ?: return
        if (word.isHorizontal) {
            if (c + 1 < word.col + word.word.length) onCellSelected(r, c + 1)
        } else {
            if (r + 1 < word.row + word.word.length) onCellSelected(r + 1, c)
        }
    }

    fun onDelete() {
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
                audioPlayer.playSound("sounds/correct.mp3")
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

    fun startGame() {
        viewModelScope.launch {
            _isGenerating.value = true
            
            // 1. Get currently selected level and find its READING dataFile
            val currentLevelId = settingsRepository.getSelectedLevel().ifEmpty { "n5" }
            val definitions = levelsRepository.loadLevelDefinitions()
            
            var dataFile = "jlpt_wordlist_n5" // Default fallback
            
            // Find the level in any section
            for (section in definitions.sections.values) {
                val level = section.levels.find { it.id.equals(currentLevelId, ignoreCase = true) }
                if (level != null) {
                    val config = level.activities[StatisticsType.READING]
                    if (config != null) {
                        dataFile = config.dataFile
                    }
                    break
                }
            }
            
            val availableWords = wordRepository.getWordEntriesForLevelSuspend(dataFile)
            
            // 2. Generate grid
            val generator = CrosswordGenerator(availableWords, _wordCount.value)
            val (generatedCells, words) = generator.generate()
            
            _cells.value = generatedCells
            _placedWords.value = words
            _isGenerating.value = false
            _gameTimeSeconds.value = 0
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _gameTimeSeconds.value += 1
            }
        }
    }

    fun abandonGame() { timerJob?.cancel() }
}
