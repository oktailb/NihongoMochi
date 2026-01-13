package org.nihongo.mochi.ui.wordquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.game.WordQuizEngine
import org.nihongo.mochi.domain.kana.KanaToRomaji
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.Word
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.domain.words.WordEntry

class WordQuizViewModel(
    private val wordRepository: WordRepository,
    private val scoreRepository: ScoreRepository
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

    fun updateSettings(mode: String, speed: Float) {
        pronunciationMode = mode
        animationSpeed = speed
    }

    fun initializeGame(entries: List<WordEntry>) {
        if (engine.isGameInitialized) return

        val words = entries.map { Word(it.text, it.phonetics) }

        engine.allWords = words.shuffled().toMutableList()
        engine.wordListPosition = 0
        engine.isGameInitialized = true

        if (engine.allWords.isNotEmpty()) {
            startNewSet()
        } else {
            _state.value = GameState.Finished
        }
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
        // Map current set statuses to list
        val statuses = engine.currentWordSet.map { engine.wordStatus[it] ?: GameStatus.NOT_ANSWERED }
        // Pad to 10
        val paddedStatuses = statuses + List(10 - statuses.size) { GameStatus.NOT_ANSWERED }
        _wordStatuses.value = paddedStatuses
    }

    private fun displayQuestion() {
        if (engine.revisionList.isEmpty()) {
            startNewSet()
            return
        }

        engine.currentWord = engine.revisionList.random()
        _currentWord.value = engine.currentWord

        val answers = generateAnswers(engine.currentWord)
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
        _areButtonsEnabled.value = false

        val correctReading = if (engine.currentWord.phonetics.isNotEmpty()) {
             if (pronunciationMode == "Roman") KanaToRomaji.convert(engine.currentWord.phonetics)
             else engine.currentWord.phonetics
        } else "?"
        
        val isCorrect = selectedAnswer == correctReading

        // Side effect: save score
        scoreRepository.saveScore(engine.currentWord.text, isCorrect, ScoreManager.ScoreType.READING)

        val newButtonStates = _buttonStates.value.toMutableList()
        
        if (isCorrect) {
            engine.wordStatus[engine.currentWord] = GameStatus.CORRECT
            engine.revisionList.remove(engine.currentWord)
            if (selectedIndex in newButtonStates.indices) {
                newButtonStates[selectedIndex] = AnswerButtonState.CORRECT
            }
        } else {
            engine.wordStatus[engine.currentWord] = GameStatus.INCORRECT
            if (selectedIndex in newButtonStates.indices) {
                newButtonStates[selectedIndex] = AnswerButtonState.INCORRECT
            }
            
            // Highlight correct answer
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
}
