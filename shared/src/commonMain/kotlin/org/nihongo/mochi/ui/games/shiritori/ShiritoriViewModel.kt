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
import org.nihongo.mochi.domain.kana.KanaUtils
import org.nihongo.mochi.domain.kana.RomajiToKana
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.words.WordEntry
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.domain.meaning.WordMeaningRepository
import org.nihongo.mochi.presentation.ViewModel
import kotlin.random.Random

data class ShiritoriWord(
    val word: String,
    val phonetics: String,
    val meaning: String,
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

    private var allWords: List<WordEntry> = emptyList()
    private var aiAvailableWords: List<WordEntry> = emptyList()
    private var usedPhonetics = mutableSetOf<String>()
    private var timerJob: Job? = null
    private var currentMeanings: Map<String, String> = emptyMap()

    init {
        viewModelScope.launch {
            RomajiToKana.init(kanaRepository)
            loadHistory()
            allWords = wordRepository.getAllWordEntriesSuspend()
        }
    }

    private fun loadHistory() {
        val history = scoreRepository.getShiritoriHistory()
        _scoresHistory.value = history
        _bestScore.value = history.maxByOrNull { it.score }?.score ?: 0
    }

    fun startGame() {
        viewModelScope.launch {
            _gameState.value = ShiritoriGameState.LOADING
            
            val locale = settingsRepository.getAppLocale()
            currentMeanings = wordMeaningRepository.getWordMeanings(locale)

            // On récupère le niveau et on construit la banque de mots comme dans KanaDrop
            val rawLevel = settingsRepository.getSelectedLevel().lowercase().ifEmpty { "n5" }
            val allJlptLevels = listOf("n5", "n4", "n3", "n2", "n1")
            
            // Normalisation pour trouver l'index JLPT (ex: "jlpt_n5" -> "n5")
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
            
            // Fallback si vide
            if (entries.isEmpty()) {
                entries.addAll(wordRepository.getWordEntriesForLevelSuspend("n5"))
            }

            aiAvailableWords = entries.distinctBy { it.text + it.phonetics }
            
            // On s'assure que tout le dictionnaire est prêt pour le joueur
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
            
            // L'IA commence
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
                // Si vraiment rien, on laisse le joueur commencer
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

        val normalizedInput = KanaUtils.katakanaToHiragana(input)
        
        // Recherche souple : texte exact ou phonétique normalisée
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
        
        val normalizationMap = mapOf(
            "ゃ" to "ya", "ゅ" to "yu", "ょ" to "yo",
            "ぁ" to "a", "ぃ" to "i", "ぅ" to "u", "ぇ" to "e", "ぉ" to "o",
            "っ" to "tsu"
        ).mapValues { RomajiToKana.checkReplacement(it.value)?.second ?: it.value } // Double check normalization
        
        // Let's use simpler normalization as the system expects hiragana
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
