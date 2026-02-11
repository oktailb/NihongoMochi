package org.nihongo.mochi.ui.games.shiritori

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaUtils
import org.nihongo.mochi.domain.kana.RomajiToKana
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.words.WordEntry
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.domain.meaning.WordMeaningRepository
import org.nihongo.mochi.presentation.ViewModel
import org.nihongo.mochi.settings.GAME_STATE_SHIRITORI
import kotlin.random.Random

sealed class ShiritoriError {
    object None : ShiritoriError()
    object InvalidStart : ShiritoriError()
    object EndsInN : ShiritoriError()
    object AlreadyUsed : ShiritoriError()
    object WordNotFound : ShiritoriError()
}

class ShiritoriViewModel(
    private val wordRepository: WordRepository,
    private val wordMeaningRepository: WordMeaningRepository,
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

    private val _hasSavedGame = MutableStateFlow(false)
    val hasSavedGame: StateFlow<Boolean> = _hasSavedGame.asStateFlow()

    private var autoRestoreDone = false

    private var allWords: List<WordEntry> = emptyList()
    private var aiAvailableWords: List<WordEntry> = emptyList()
    private var usedPhonetics = mutableSetOf<String>()
    private var timerJob: Job? = null
    private var currentMeanings: Map<String, String> = emptyMap()
    private var previousGameState: ShiritoriGameState? = null

    init {
        viewModelScope.launch {
            RomajiToKana.init(kanaRepository)
            loadHistory()
            allWords = wordRepository.getAllWordEntriesSuspend()
            checkSavedGame()
        }
    }

    private fun loadHistory() {
        val history = scoreRepository.getShiritoriHistory()
        _scoresHistory.value = history
        _bestScore.value = history.maxByOrNull { it.score }?.score ?: 0
    }

    private fun checkSavedGame() {
        _hasSavedGame.value = settingsRepository.getGameState(GAME_STATE_SHIRITORI) != null
    }

    fun tryAutoRestore(onRestored: () -> Unit) {
        if (!autoRestoreDone && _hasSavedGame.value) {
            autoRestoreDone = true
            restoreGame(onRestored)
        }
    }

    fun restoreGame(onRestored: () -> Unit) {
        val savedJson = settingsRepository.getGameState(GAME_STATE_SHIRITORI) ?: return
        try {
            val restored = Json.decodeFromString<ShiritoriGameStateData>(savedJson)
            if (restored.gameState != ShiritoriGameState.GAME_OVER) {
                _playedWords.value = restored.playedWords
                _lastKana.value = restored.lastKana
                _score.value = restored.score
                _gameTimeSeconds.value = restored.gameTimeSeconds
                usedPhonetics = restored.usedPhonetics.toMutableSet()
                _gameState.value = ShiritoriGameState.PAUSED
                
                viewModelScope.launch {
                    val locale = settingsRepository.getAppLocale()
                    currentMeanings = wordMeaningRepository.getWordMeanings(locale)
                    prepareWordBank()
                    startTimer()
                    onRestored()
                }
            }
        } catch (e: Exception) {
            settingsRepository.clearGameState(GAME_STATE_SHIRITORI)
            _hasSavedGame.value = false
        }
    }

    private suspend fun prepareWordBank() {
        val rawLevel = settingsRepository.getSelectedLevel().lowercase().ifEmpty { "n5" }
        val allJlptLevels = listOf("n5", "n4", "n3", "n2", "n1")
        val normalizedLevel = allJlptLevels.find { rawLevel.contains(it) } ?: rawLevel
        val selectedLevelIndex = allJlptLevels.indexOf(normalizedLevel)
        
        val levelsToInclude = if (selectedLevelIndex != -1) {
            allJlptLevels.take(selectedLevelIndex + 1)
        } else {
            listOf(rawLevel) 
        }

        val entries = mutableListOf<WordEntry>()
        levelsToInclude.forEach { lvl ->
            try {
                val words = wordRepository.getWordEntriesForLevelSuspend(lvl)
                entries.addAll(words)
            } catch (_: Exception) {}
        }
        
        if (entries.isEmpty()) {
            entries.addAll(wordRepository.getWordEntriesForLevelSuspend("n5"))
        }

        aiAvailableWords = entries.distinctBy { it.text + it.phonetics }
    }

    fun startGame() {
        viewModelScope.launch {
            _gameState.value = ShiritoriGameState.LOADING
            settingsRepository.clearGameState(GAME_STATE_SHIRITORI)
            _hasSavedGame.value = false
            autoRestoreDone = true
            
            val locale = settingsRepository.getAppLocale()
            currentMeanings = wordMeaningRepository.getWordMeanings(locale)

            prepareWordBank()
            
            if (allWords.isEmpty()) {
                allWords = wordRepository.getAllWordEntriesSuspend()
            }

            usedPhonetics.clear()
            _playedWords.value = emptyList()
            _score.value = 0
            _inputText.value = ""
            _isVictory.value = false
            _error.value = ShiritoriError.None
            _gameTimeSeconds.value = 0
            
            val firstWordCandidates = aiAvailableWords.filter { 
                val hira = KanaUtils.katakanaToHiragana(it.phonetics)
                !hira.endsWith("ん") && !hira.endsWith("ン") 
            }
            
            if (firstWordCandidates.isNotEmpty()) {
                val firstWord = firstWordCandidates.random()
                addWord(firstWord, false)
                _lastKana.value = getNextTargetKana(firstWord.phonetics)
                _gameState.value = ShiritoriGameState.PLAYER_TURN
                startTimer()
            } else {
                _lastKana.value = ""
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
                if (_gameState.value != ShiritoriGameState.PAUSED) {
                    _gameTimeSeconds.value++
                }
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

        val normalizedInput = KanaUtils.katakanaToHiragana(input)
        
        val wordEntry = allWords.find { 
            it.text == input || 
            KanaUtils.katakanaToHiragana(it.phonetics) == normalizedInput ||
            it.phonetics == input 
        }
        
        if (wordEntry == null) {
            _error.value = ShiritoriError.WordNotFound
            return
        }

        val phonetics = wordEntry.phonetics
        val normalizedPhonetics = KanaUtils.katakanaToHiragana(phonetics)

        if (normalizedPhonetics.endsWith("ん")) {
            _error.value = ShiritoriError.EndsInN
            gameOver(false)
            return
        }

        if (_lastKana.value.isNotEmpty() && !isValidStart(phonetics, _lastKana.value)) {
            _error.value = ShiritoriError.InvalidStart
            return
        }

        if (usedPhonetics.contains(normalizedPhonetics)) {
            _error.value = ShiritoriError.AlreadyUsed
            return
        }

        audioPlayer.playSound("sounds/correct.mp3")
        addWord(wordEntry, true)
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
                !it.phonetics.endsWith("ン") &&
                !usedPhonetics.contains(KanaUtils.katakanaToHiragana(it.phonetics))
            }

            if (possibleWords.isEmpty()) {
                gameOver(true)
            } else {
                val aiWord = possibleWords.random()
                addWord(aiWord, false)
                _lastKana.value = getNextTargetKana(aiWord.phonetics)
                _gameState.value = ShiritoriGameState.PLAYER_TURN
            }
        }
    }

    private fun addWord(entry: WordEntry, isPlayer: Boolean) {
        val meaning = currentMeanings[entry.id] ?: ""
        val newWord = ShiritoriWord(entry.text, entry.phonetics, meaning, isPlayer)
        _playedWords.value = _playedWords.value + newWord
        usedPhonetics.add(KanaUtils.katakanaToHiragana(entry.phonetics))
    }

    private fun isValidStart(phonetics: String, targetKana: String): Boolean {
        if (phonetics.isEmpty()) return false
        val firstChar = KanaUtils.katakanaToHiragana(phonetics.take(1))
        val normalizedTarget = KanaUtils.katakanaToHiragana(targetKana)
        return firstChar == normalizedTarget
    }

    private fun getNextTargetKana(phonetics: String): String {
        if (phonetics.isEmpty()) return ""
        val hiraPhonetics = KanaUtils.katakanaToHiragana(phonetics)
        var lastChar = hiraPhonetics.takeLast(1)
        
        if (lastChar == "ー" && hiraPhonetics.length > 1) {
            lastChar = hiraPhonetics.substring(hiraPhonetics.length - 2, hiraPhonetics.length - 1)
        }
        
        val simpleNormalization = mapOf(
            "ゃ" to "や", "ゅ" to "ゆ", "ょ" to "よ",
            "ぁ" to "あ", "ぃ" to "い", "ぅ" to "う", "ぇ" to "え", "ぉ" to "お",
            "っ" to "つ"
        )
        
        return simpleNormalization[lastChar] ?: lastChar
    }

    private fun gameOver(victory: Boolean) {
        if (_gameState.value == ShiritoriGameState.GAME_OVER) return

        timerJob?.cancel()
        settingsRepository.clearGameState(GAME_STATE_SHIRITORI)
        _hasSavedGame.value = false
        autoRestoreDone = true
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
        scoreRepository.saveShiritoriResult(result)
        loadHistory()
    }

    fun pauseGame() {
        if (_gameState.value != ShiritoriGameState.GAME_OVER && _gameState.value != ShiritoriGameState.IDLE) {
            previousGameState = _gameState.value
            _gameState.value = ShiritoriGameState.PAUSED
        }
    }

    fun resumeGame() {
        if (_gameState.value == ShiritoriGameState.PAUSED) {
            _gameState.value = previousGameState ?: ShiritoriGameState.PLAYER_TURN
        }
    }

    fun saveAndExit() {
        val data = ShiritoriGameStateData(
            playedWords = _playedWords.value,
            lastKana = _lastKana.value,
            score = _score.value,
            gameTimeSeconds = _gameTimeSeconds.value,
            usedPhonetics = usedPhonetics,
            gameState = previousGameState ?: _gameState.value
        )
        val json = Json.encodeToString(ShiritoriGameStateData.serializer(), data)
        settingsRepository.saveGameState(GAME_STATE_SHIRITORI, json)
        _hasSavedGame.value = true
        autoRestoreDone = true
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
