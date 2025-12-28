package org.nihongo.mochi.domain.game

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kana.KanaToRomaji
import org.nihongo.mochi.domain.kana.KanaUtils
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress
import kotlin.random.Random

enum class QuestionDirection { NORMAL, REVERSE }

sealed class GameState {
    object Loading : GameState()
    object WaitingForAnswer : GameState()
    data class ShowingResult(val isCorrect: Boolean, val selectedAnswerIndex: Int) : GameState()
    object Finished : GameState()
}

class RecognitionGameEngine {
    var isGameInitialized = false
    
    val allKanjiDetails = mutableListOf<KanjiDetail>()
    var currentKanjiSet = mutableListOf<KanjiDetail>()
    var revisionList = mutableListOf<KanjiDetail>()
    val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    val kanjiProgress = mutableMapOf<KanjiDetail, KanjiProgress>()
    var kanjiListPosition = 0
    lateinit var currentKanji: KanjiDetail
    lateinit var correctAnswer: String
    lateinit var gameMode: String
    lateinit var readingMode: String
    var currentDirection: QuestionDirection = QuestionDirection.NORMAL
    
    // UI State
    var currentAnswers = listOf<String>()
    
    private val _state = MutableStateFlow<GameState>(GameState.Loading)
    val state: StateFlow<GameState> = _state.asStateFlow()
    
    // Buttons state
    private val _buttonStates = MutableStateFlow<List<AnswerButtonState>>(List(4) { AnswerButtonState.DEFAULT })
    val buttonStates: StateFlow<List<AnswerButtonState>> = _buttonStates.asStateFlow()

    // Configuration
    var pronunciationMode: String = "Hiragana" // "Hiragana" or "Roman"
    var animationSpeed: Float = 1.0f

    fun resetState() {
        isGameInitialized = false
        allKanjiDetails.clear()
        currentKanjiSet.clear()
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()
        kanjiListPosition = 0
        currentAnswers = emptyList()
        _state.value = GameState.Loading
        _buttonStates.value = List(4) { AnswerButtonState.DEFAULT }
    }
    
    fun startGame() {
        if (startNewSet()) {
            nextQuestion()
            _state.value = GameState.WaitingForAnswer
        } else {
            _state.value = GameState.Finished
        }
    }

    private fun startNewSet(): Boolean {
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()

        if (kanjiListPosition >= allKanjiDetails.size) {
            return false // No more sets
        }

        val nextSet = allKanjiDetails.drop(kanjiListPosition).take(10)
        kanjiListPosition += nextSet.size

        currentKanjiSet.clear()
        currentKanjiSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentKanjiSet.forEach {
            kanjiStatus[it] = GameStatus.NOT_ANSWERED
            kanjiProgress[it] = KanjiProgress()
        }
        return true
    }

    fun nextQuestion() {
        if (revisionList.isEmpty()) return

        currentKanji = revisionList.random()
        val progress = kanjiProgress[currentKanji]!!

        // Determine direction
        currentDirection = when {
            !progress.normalSolved && !progress.reverseSolved -> if (Random.nextDouble() < 0.5) QuestionDirection.NORMAL else QuestionDirection.REVERSE
            !progress.normalSolved -> QuestionDirection.NORMAL
            else -> QuestionDirection.REVERSE
        }

        currentAnswers = generateAnswers(currentKanji)
        _buttonStates.value = List(4) { AnswerButtonState.DEFAULT }
    }

    private fun generateAnswers(correctKanji: KanjiDetail): List<String> {
        if (currentDirection == QuestionDirection.NORMAL) {
            val correctButtonText: String
            if (gameMode == "meaning") {
                correctAnswer = correctKanji.meanings.firstOrNull() ?: ""
                correctButtonText = correctKanji.meanings.take(3).joinToString("\n")
            } else { // reading mode
                correctButtonText = getFormattedReadings(correctKanji)
                correctAnswer = correctButtonText.lines().firstOrNull() ?: ""
            }

            if (correctAnswer.isEmpty()) return listOf("", "", "", "")

            val incorrectPool = allKanjiDetails
                .asSequence()
                .filter { it.id != correctKanji.id }
                .map { detail ->
                    if (gameMode == "meaning") {
                        detail.meanings.take(3).joinToString("\n")
                    } else {
                        getFormattedReadings(detail)
                    }
                }
                .filter { it.isNotEmpty() }
                .distinct()
                .shuffled()
                .take(3)
                .toList()

            return (incorrectPool + correctButtonText).shuffled()
        } else {
            correctAnswer = correctKanji.character
            val correctButtonText = correctKanji.character

            val incorrectPool = allKanjiDetails
                .asSequence()
                .filter { it.id != correctKanji.id }
                .map { it.character }
                .distinct()
                .shuffled()
                .take(3)
                .toList()

            return (incorrectPool + correctButtonText).shuffled()
        }
    }
    
    fun getFormattedReadings(kanji: KanjiDetail): String {
        val onReadings = kanji.readings.filter { it.type == "on" }
        val kunReadings = kanji.readings.filter { it.type == "kun" }

        val selectedOn = if (readingMode == "common") {
            onReadings.sortedByDescending { it.frequency }
        } else {
            onReadings.shuffled()
        }.take(2)

        val selectedKun = if (readingMode == "common") {
            kunReadings.sortedByDescending { it.frequency }
        } else {
            kunReadings.shuffled()
        }.take(2)

        val onStrings = selectedOn.map {
            if (pronunciationMode == "Roman") KanaToRomaji.convert(it.value).uppercase() else KanaUtils.hiraganaToKatakana(it.value)
        }
        val kunStrings = selectedKun.map {
            if (pronunciationMode == "Roman") KanaToRomaji.convert(it.value) else it.value
        }

        return (onStrings + kunStrings).joinToString("\n")
    }

    suspend fun submitAnswer(selectedAnswer: String, selectedIndex: Int) {
        val isCorrect = if (currentDirection == QuestionDirection.NORMAL) {
            // For NORMAL, button text might be multi-line, check if any line matches correctAnswer
            selectedAnswer.lines().any { it.equals(correctAnswer, ignoreCase = true) }
        } else {
            selectedAnswer == correctAnswer
        }
        
        ScoreManager.saveScore(currentKanji.character, isCorrect, ScoreManager.ScoreType.RECOGNITION)
        
        // Update Game State
        val progress = kanjiProgress[currentKanji]!!
        val newButtonStates = _buttonStates.value.toMutableList()

        if (isCorrect) {
            if (currentDirection == QuestionDirection.NORMAL) progress.normalSolved = true
            else progress.reverseSolved = true

            if (progress.normalSolved && progress.reverseSolved) {
                kanjiStatus[currentKanji] = GameStatus.CORRECT
                revisionList.remove(currentKanji)
                newButtonStates[selectedIndex] = AnswerButtonState.CORRECT
            } else {
                kanjiStatus[currentKanji] = GameStatus.PARTIAL
                newButtonStates[selectedIndex] = AnswerButtonState.NEUTRAL
            }
        } else {
            kanjiStatus[currentKanji] = GameStatus.INCORRECT
            newButtonStates[selectedIndex] = AnswerButtonState.INCORRECT
        }
        
        _buttonStates.value = newButtonStates
        _state.value = GameState.ShowingResult(isCorrect, selectedIndex)
        delay((1000 * animationSpeed).toLong())

        if (revisionList.isEmpty()) {
            if (!startNewSet()) {
                _state.value = GameState.Finished
                return
            }
        }
        
        nextQuestion()
        _state.value = GameState.WaitingForAnswer
    }
}
