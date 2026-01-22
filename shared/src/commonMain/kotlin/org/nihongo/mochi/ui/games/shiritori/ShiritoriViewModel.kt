package org.nihongo.mochi.ui.games.shiritori

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.RomajiToKana
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.words.WordEntry
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.presentation.ViewModel
import kotlin.random.Random

data class ShiritoriWord(
    val word: String,
    val phonetics: String,
    val isPlayer: Boolean,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

data class ShiritoriGameResult(
    val levelId: String,
    val score: Int,
    val timeSeconds: Int,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

enum class ShiritoriGameState {
    IDLE,
    LOADING,
    PLAYER_TURN,
    AI_TURN,
    GAME_OVER
}

sealed class ShiritoriError {
    object None : ShiritoriError()
    object InvalidStart : ShiritoriError()
    object EndsInN : ShiritoriError()
    object AlreadyUsed : ShiritoriError()
    object WordNotFound : ShiritoriError()
}

class ShiritoriViewModel(
    private val wordRepository: WordRepository,
    private val scoreRepository: ScoreRepository,
    private val settingsRepository: SettingsRepository,
    private val kanaRepository: KanaRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _gameState = MutableStateFlow(ShiritoriGameState.IDLE)
    val gameState: StateFlow<ShiritoriGameState> = _gameState.asStateFlow()

    private val _playedWords = MutableStateFlow<List<ShiritoriWord>>(emptyList())
    val playedWords: StateFlow<List<ShiritoriWord>> = _playedWords.asStateFlow()

    private val _lastKana = MutableStateFlow("")
    val lastKana: StateFlow<String> = _lastKana.asStateFlow()

    private val _error = MutableStateFlow<ShiritoriError>(ShiritoriError.None)
    val error: StateFlow<ShiritoriError> = _error.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _bestScore = MutableStateFlow(0)
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    private val _scoresHistory = MutableStateFlow<List<ShiritoriGameResult>>(emptyList())
    val scoresHistory: StateFlow<List<ShiritoriGameResult>> = _scoresHistory.asStateFlow()

    private val _gameTimeSeconds = MutableStateFlow(0)
    val gameTimeSeconds: StateFlow<Int> = _gameTimeSeconds.asStateFlow()

    private val _isVictory = MutableStateFlow(false)
    val isVictory: StateFlow<Boolean> = _isVictory.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private var allWords: List<WordEntry> = emptyList()
    private var aiAvailableWords: List<WordEntry> = emptyList()
    private var usedPhonetics = mutableSetOf<String>()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            RomajiToKana.init(kanaRepository)
            loadHistory()
            // On pré-charge les mots pour qu'ils soient prêts au clic sur Play
            allWords = wordRepository.getAllWordEntriesSuspend()
        }
    }

    private fun loadHistory() {
        _scoresHistory.value = emptyList()
        _bestScore.value = 0
    }

    fun startGame() {
        viewModelScope.launch {
            if (allWords.isEmpty()) {
                _gameState.value = ShiritoriGameState.LOADING
                allWords = wordRepository.getAllWordEntriesSuspend()
            }
            
            val selectedLevel = settingsRepository.getSelectedLevel().lowercase()
            val maxRank = when (selectedLevel) {
                "n5" -> 2000 
                "n4" -> 5000
                "n3" -> 10000
                "n2" -> 25000
                "n1" -> 50000
                else -> 100000
            }
            
            aiAvailableWords = allWords.filter { 
                val rank = it.rank?.toIntOrNull() ?: 999999
                rank <= maxRank
            }

            usedPhonetics.clear()
            _playedWords.value = emptyList()
            _score.value = 0
            _inputText.value = ""
            _isVictory.value = false
            _error.value = ShiritoriError.None
            _gameTimeSeconds.value = 0
            
            val firstWordCandidates = aiAvailableWords.filter { !it.phonetics.endsWith("ん") }
            if (firstWordCandidates.isNotEmpty()) {
                val firstWord = firstWordCandidates.random()
                addWord(firstWord.text, firstWord.phonetics, false)
                _lastKana.value = getNextTargetKana(firstWord.phonetics)
                _gameState.value = ShiritoriGameState.PLAYER_TURN
                startTimer()
            } else {
                _gameState.value = ShiritoriGameState.PLAYER_TURN
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _gameTimeSeconds.value++
            }
        }
    }

    fun onInputChanged(newText: String) {
        var finalText = newText
        val currentText = _inputText.value
        if (newText.length > currentText.length) {
            val replacement = RomajiToKana.checkReplacement(newText)
            if (replacement != null) {
                finalText = newText.substring(0, newText.length - replacement.first) + replacement.second
            }
        }
        _inputText.value = finalText
    }

    fun onPlayerSubmit() {
        if (_gameState.value != ShiritoriGameState.PLAYER_TURN) return
        _error.value = ShiritoriError.None

        val input = _inputText.value.trim()
        if (input.isEmpty()) return

        val wordEntry = allWords.find { it.text == input || it.phonetics == input }
        
        if (wordEntry == null) {
            _error.value = ShiritoriError.WordNotFound
            return
        }

        val phonetics = wordEntry.phonetics

        if (phonetics.endsWith("ん")) {
            _error.value = ShiritoriError.EndsInN
            gameOver(false)
            return
        }

        if (_lastKana.value.isNotEmpty() && !isValidStart(phonetics, _lastKana.value)) {
            _error.value = ShiritoriError.InvalidStart
            return
        }

        if (usedPhonetics.contains(phonetics)) {
            _error.value = ShiritoriError.AlreadyUsed
            return
        }

        audioPlayer.playSound("sounds/correct.mp3")
        addWord(wordEntry.text, phonetics, true)
        _score.value++
        _inputText.value = ""
        
        aiTurn(phonetics)
    }

    private fun aiTurn(lastPlayerWordPhonetics: String) {
        if (_gameState.value == ShiritoriGameState.GAME_OVER) return
        
        _gameState.value = ShiritoriGameState.AI_TURN
        val targetKana = getNextTargetKana(lastPlayerWordPhonetics)

        viewModelScope.launch {
            delay(1200)
            if (_gameState.value != ShiritoriGameState.AI_TURN) return@launch

            val possibleWords = aiAvailableWords.filter { 
                isValidStart(it.phonetics, targetKana) && 
                !it.phonetics.endsWith("ん") &&
                !usedPhonetics.contains(it.phonetics)
            }

            if (possibleWords.isEmpty()) {
                gameOver(true)
            } else {
                val aiWord = possibleWords.random()
                addWord(aiWord.text, aiWord.phonetics, false)
                _lastKana.value = getNextTargetKana(aiWord.phonetics)
                _gameState.value = ShiritoriGameState.PLAYER_TURN
            }
        }
    }

    private fun addWord(text: String, phonetics: String, isPlayer: Boolean) {
        val newWord = ShiritoriWord(text, phonetics, isPlayer)
        _playedWords.value = _playedWords.value + newWord
        usedPhonetics.add(phonetics)
    }

    private fun isValidStart(phonetics: String, targetKana: String): Boolean {
        if (phonetics.isEmpty()) return false
        val firstChar = phonetics.take(1)
        return firstChar == targetKana
    }

    private fun getNextTargetKana(phonetics: String): String {
        if (phonetics.isEmpty()) return ""
        var lastChar = phonetics.takeLast(1)
        if (lastChar == "ー" && phonetics.length > 1) {
            lastChar = phonetics.substring(phonetics.length - 2, phonetics.length - 1)
        }
        val normalizationMap = mapOf(
            "ゃ" to "や", "ゅ" to "ゆ", "ょ" to "よ",
            "ぁ" to "あ", "ぃ" to "い", "ぅ" to "う", "ぇ" to "え", "ぉ" to "お",
            "っ" to "つ"
        )
        return normalizationMap[lastChar] ?: lastChar
    }

    private fun gameOver(victory: Boolean) {
        if (_gameState.value == ShiritoriGameState.GAME_OVER) return

        timerJob?.cancel()
        _isVictory.value = victory
        _gameState.value = ShiritoriGameState.GAME_OVER
        if (!victory) audioPlayer.playSound("sounds/game_over.mp3")
        else audioPlayer.playSound("sounds/success.mp3")
        
        saveResult()
    }

    private fun saveResult() {
        val result = ShiritoriGameResult(
            levelId = settingsRepository.getSelectedLevel(),
            score = _score.value,
            timeSeconds = _gameTimeSeconds.value
        )
        val currentHistory = _scoresHistory.value.toMutableList()
        currentHistory.add(0, result)
        _scoresHistory.value = currentHistory.take(5)
        
        if (_score.value > _bestScore.value) {
            _bestScore.value = _score.value
        }
    }

    fun abandonGame() {
        gameOver(false)
    }

    fun resetToIdle() {
        timerJob?.cancel()
        _gameState.value = ShiritoriGameState.IDLE
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
