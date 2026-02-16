package org.nihongo.mochi.domain.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kana.KanaToRomaji
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.Word
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.domain.words.WordEntry
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.statistics.StatisticsType

class WordQuizViewModel(
    private val wordRepository: WordRepository,
    private val scoreRepository: ScoreRepository,
    private val statisticsEngine: StatisticsEngine,
    private val audioPlayer: AudioPlayer
) : ViewModel() {
    
    private val engine = WordQuizEngine()
    
    // Reactive State
    private val _state = MutableStateFlow<GameState>(GameState.Loading)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _buttonStates = MutableStateFlow<List<AnswerButtonState>>(List(4) { AnswerButtonState.DEFAULT })
    val buttonStates: StateFlow<List<AnswerButtonState>> = _buttonStates.asStateFlow()
    
    private val _areButtonsEnabled = MutableStateFlow(true)
    val areButtonsEnabled: StateFlow<Boolean> = _areButtonsEnabled.asStateFlow()

    private val _currentAnswers = MutableStateFlow<List<String>>(emptyList())
    val currentAnswers: StateFlow<List<String>> = _currentAnswers.asStateFlow()

    private val _currentWord = MutableStateFlow<Word?>(null)
    val currentWord: StateFlow<Word?> = _currentWord.asStateFlow()
    
    // Progress
    private val _wordStatuses = MutableStateFlow<List<GameStatus>>(emptyList())
    val wordStatuses: StateFlow<List<GameStatus>> = _wordStatuses.asStateFlow()

    // Settings
    private var pronunciationMode: String = "Hiragana"
    private var animationSpeed: Float = 1.0f
    private var currentLevelId: String = ""

    fun updateSettings(mode: String, speed: Float) {
        pronunciationMode = mode
        animationSpeed = speed
    }

    fun initializeGame(entries: List<WordEntry>, levelId: String = "") {
        if (engine.isGameInitialized) return
        this.currentLevelId = levelId

        if (entries.isEmpty()) {
            engine.isGameInitialized = true
            _state.value = GameState.Finished
            return
        }

        val manualRevisionList = scoreRepository.getListItems(ScoreManager.READING_LIST)
        
        // Filter out mastered words, UNLESS they are in the manual revision list
        val filteredEntries = if (levelId != "user_custom_list" && levelId.isNotEmpty()) {
            entries.filter {
                val score = scoreRepository.getScore(it.text, ScoreManager.ScoreType.READING)
                val mastery = score.successes - score.failures
                val isManualRevision = manualRevisionList.contains(it.id) || manualRevisionList.contains(it.text)
                mastery < 10 || isManualRevision
            }
        } else {
            entries
        }

        if (filteredEntries.isEmpty()) {
             engine.isGameInitialized = true
             _state.value = GameState.Finished
             return
        }

        val words = filteredEntries.map { Word(it.text, it.phonetics) }

        engine.allWords = words.shuffled().toMutableList()
        engine.wordListPosition = 0
        engine.isGameInitialized = true

        startNewSet()
    }

    /**
     * Calcule la maîtrise globale (cohérent avec SagaMap)
     */
    fun calculateGlobalMasteryPercent(): Float {
        if (currentLevelId.isEmpty()) return 0f
        return statisticsEngine.getPercentageForLevel(currentLevelId, StatisticsType.READING).toFloat() / 100f
    }

    /**
     * Calcule la maîtrise du lot actuel
     */
    fun calculateSessionMasteryPercent(): Float {
        if (engine.currentWordSet.isEmpty()) return 0f
        return statisticsEngine.calculateMasteryPercentage(
            engine.currentWordSet.map { it.text },
            ScoreManager.ScoreType.READING
        ).toFloat() / 100f
    }

    private fun startNewSet() {
        engine.revisionList.clear()
        engine.wordStatus.clear()
        
        if (engine.wordListPosition >= engine.allWords.size) {
            _state.value = GameState.Finished
            return
        }

        val nextSet = engine.allWords.drop(engine.wordListPosition).take(10)
        engine.wordListPosition += nextSet.size

        engine.currentWordSet.clear()
        engine.currentWordSet.addAll(nextSet)
        engine.revisionList.addAll(nextSet)
        engine.currentWordSet.forEach {
            engine.wordStatus[it] = GameStatus.NOT_ANSWERED
        }
        
        updateWordStatuses()
        displayQuestion()
    }
    
    private fun updateWordStatuses() {
        // We only want to show as many slots as there are words in the current set
        val statuses = engine.currentWordSet.map { engine.wordStatus[it] ?: GameStatus.NOT_ANSWERED }
        _wordStatuses.value = statuses
    }

    private fun displayQuestion() {
        if (engine.revisionList.isEmpty()) {
            startNewSet()
            return
        }

        val nextWord = engine.revisionList.randomOrNull()
        if (nextWord == null) {
            _state.value = GameState.Finished
            return
        }

        engine.currentWord = nextWord
        _currentWord.value = nextWord

        val answers = generateAnswers(nextWord)
        engine.currentAnswers = answers
        _currentAnswers.value = answers
        
        _buttonStates.value = List(4) { AnswerButtonState.DEFAULT }
        _areButtonsEnabled.value = true
        _state.value = GameState.WaitingForAnswer
    }

    private fun generateAnswers(correctWord: Word): List<String> {
        val getReading: (Word) -> String = { word ->
            if (word.phonetics.isNotEmpty()) {
                if (pronunciationMode == "Roman") KanaToRomaji.convert(word.phonetics) 
                else word.phonetics
            } else {
                "?"
            }
        }
        
        val correctAnswer = getReading(correctWord)
        
        val incorrectAnswers = engine.allWords
            .asSequence()
            .filter { it.text != correctWord.text }
            .map { getReading(it) }
            .distinct()
            .filter { it != correctAnswer && it != "?" }
            .shuffled()
            .take(3)
            .toList()

        return (incorrectAnswers + correctAnswer).shuffled()
    }

    fun submitAnswer(selectedAnswer: String, selectedIndex: Int) {
        if (!_areButtonsEnabled.value) return
        val currentWord = engine.currentWord ?: return
        _areButtonsEnabled.value = false

        val correctReading = if (currentWord.phonetics.isNotEmpty()) {
             if (pronunciationMode == "Roman") KanaToRomaji.convert(currentWord.phonetics)
             else currentWord.phonetics
        } else "?"
        
        val isCorrect = selectedAnswer == correctReading

        if (isCorrect) {
            audioPlayer.playSound("sounds/correct.mp3")
        } else {
            audioPlayer.playSound("sounds/incorrect.mp3")
        }

        scoreRepository.saveScore(currentWord.text, isCorrect, ScoreManager.ScoreType.READING)

        val newButtonStates = _buttonStates.value.toMutableList()
        
        if (isCorrect) {
            engine.wordStatus[currentWord] = GameStatus.CORRECT
            engine.revisionList.remove(currentWord)
            if (selectedIndex in newButtonStates.indices) {
                newButtonStates[selectedIndex] = AnswerButtonState.CORRECT
            }
        } else {
            engine.wordStatus[currentWord] = GameStatus.INCORRECT
            if (selectedIndex in newButtonStates.indices) {
                newButtonStates[selectedIndex] = AnswerButtonState.INCORRECT
            }
            
            val correctIndex = _currentAnswers.value.indexOfFirst { it == correctReading }
            if (correctIndex != -1) {
                newButtonStates[correctIndex] = AnswerButtonState.CORRECT
            }
        }
        
        _buttonStates.value = newButtonStates
        updateWordStatuses()
        _state.value = GameState.ShowingResult(isCorrect, selectedIndex)

        viewModelScope.launch {
            val delayMs = (1000 * animationSpeed).toLong()
            delay(delayMs)
            displayQuestion()
        }
    }

    fun replay() {
        engine.isGameInitialized = false
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAll()
    }
}
