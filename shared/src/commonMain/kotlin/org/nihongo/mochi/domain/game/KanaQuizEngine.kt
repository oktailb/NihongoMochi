package org.nihongo.mochi.domain.game

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanaCharacter
import org.nihongo.mochi.domain.models.KanaProgress
import org.nihongo.mochi.domain.models.KanaQuestionDirection

enum class QuizMode {
    KANA_TO_ROMAJI,
    ROMAJI_TO_KANA
}

class KanaQuizEngine {
    var isGameInitialized = false
    var quizMode: QuizMode = QuizMode.KANA_TO_ROMAJI
    
    // Data
    var allKana = listOf<KanaCharacter>()
    
    // Game State
    var kanaListPosition = 0
    val currentKanaSet = mutableListOf<KanaCharacter>()
    val revisionList = mutableListOf<KanaCharacter>()
    val kanaStatus = mutableMapOf<KanaCharacter, GameStatus>()
    val kanaProgress = mutableMapOf<KanaCharacter, KanaProgress>()
    
    lateinit var currentQuestion: KanaCharacter
    var currentDirection: KanaQuestionDirection = KanaQuestionDirection.NORMAL
    
    // UI State Flows
    private val _state = MutableStateFlow<GameState>(GameState.Loading)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _buttonStates = MutableStateFlow<List<AnswerButtonState>>(List(4) { AnswerButtonState.DEFAULT })
    val buttonStates: StateFlow<List<AnswerButtonState>> = _buttonStates.asStateFlow()

    var currentAnswers = listOf<String>()
    
    // Configuration
    var animationSpeed: Float = 1.0f

    fun resetState() {
        isGameInitialized = false
        allKana = emptyList()
        kanaListPosition = 0
        currentKanaSet.clear()
        revisionList.clear()
        kanaStatus.clear()
        kanaProgress.clear()
        currentAnswers = emptyList()
        _state.value = GameState.Loading
        _buttonStates.value = List(4) { AnswerButtonState.DEFAULT }
    }
    
    fun startGame() {
        if (startNewSet()) {
            displayQuestion()
        } else {
            _state.value = GameState.Finished
        }
    }

    private fun startNewSet(): Boolean {
        revisionList.clear()
        kanaStatus.clear()
        kanaProgress.clear()

        if (kanaListPosition >= allKana.size) {
            return false
        }

        val nextSet = allKana.drop(kanaListPosition).take(10)
        kanaListPosition += nextSet.size

        currentKanaSet.clear()
        currentKanaSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentKanaSet.forEach {
            kanaStatus[it] = GameStatus.NOT_ANSWERED
            kanaProgress[it] = KanaProgress()
        }
        return true
    }

    private fun displayQuestion() {
        if (revisionList.isEmpty()) {
            if (!startNewSet()) {
                _state.value = GameState.Finished
                return
            }
        }

        currentQuestion = revisionList.random()
        val progress = kanaProgress[currentQuestion]!!

        currentDirection = when {
            !progress.normalSolved && !progress.reverseSolved -> if (Math.random() < 0.5) KanaQuestionDirection.NORMAL else KanaQuestionDirection.REVERSE
            !progress.normalSolved -> KanaQuestionDirection.NORMAL
            else -> KanaQuestionDirection.REVERSE
        }

        currentAnswers = generateAnswers(currentQuestion)
        _buttonStates.value = List(4) { AnswerButtonState.DEFAULT }
        _state.value = GameState.WaitingForAnswer
    }

    private fun generateAnswers(correctChar: KanaCharacter): List<String> {
        val isNormal = currentDirection == KanaQuestionDirection.NORMAL
        val correctAnswer = if (isNormal) correctChar.romaji else correctChar.kana
        
        val incorrectAnswers = allKana
            .asSequence()
            .map { if (isNormal) it.romaji else it.kana }
            .distinct()
            .filter { it != correctAnswer }
            .shuffled()
            .take(3)
            .toList()
            
        return (incorrectAnswers + correctAnswer).shuffled()
    }

    suspend fun submitAnswer(selectedAnswer: String, selectedIndex: Int) {
        val isNormal = currentDirection == KanaQuestionDirection.NORMAL
        val correctAnswerText = if (isNormal) currentQuestion.romaji else currentQuestion.kana
        val isCorrect = selectedAnswer == correctAnswerText

        ScoreManager.saveScore(currentQuestion.kana, isCorrect, ScoreManager.ScoreType.RECOGNITION)
        
        val newButtonStates = _buttonStates.value.toMutableList()

        if (isCorrect) {
            val progress = kanaProgress[currentQuestion]!!
            if (isNormal) progress.normalSolved = true else progress.reverseSolved = true

            if (progress.normalSolved && progress.reverseSolved) {
                kanaStatus[currentQuestion] = GameStatus.CORRECT
                revisionList.remove(currentQuestion)
                newButtonStates[selectedIndex] = AnswerButtonState.CORRECT
            } else {
                kanaStatus[currentQuestion] = GameStatus.PARTIAL
                newButtonStates[selectedIndex] = AnswerButtonState.NEUTRAL
            }
        } else {
            kanaStatus[currentQuestion] = GameStatus.INCORRECT
            newButtonStates[selectedIndex] = AnswerButtonState.INCORRECT
            
            // Highlight correct answer if incorrect
            val correctIndex = currentAnswers.indexOf(correctAnswerText)
            if (correctIndex != -1) {
                newButtonStates[correctIndex] = AnswerButtonState.CORRECT
            }
        }
        
        _buttonStates.value = newButtonStates
        _state.value = GameState.ShowingResult(isCorrect, selectedIndex)

        delay((1000 * animationSpeed).toLong())
        displayQuestion()
    }
}
