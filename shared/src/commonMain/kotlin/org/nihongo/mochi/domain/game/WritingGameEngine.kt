package org.nihongo.mochi.domain.game

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kana.KanaUtils
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress
import kotlin.random.Random

enum class QuestionType { MEANING, READING }

// TextNormalizer interface is already defined in TextNormalizer.kt
// interface TextNormalizer { fun normalize(text: String): String }

class WritingGameEngine(private val textNormalizer: TextNormalizer? = null) {
    var isGameInitialized = false
    
    val allKanjiDetails = mutableListOf<KanjiDetail>()
    
    var kanjiListPosition = 0
    val currentKanjiSet = mutableListOf<KanjiDetail>()
    val revisionList = mutableListOf<KanjiDetail>()
    val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    val kanjiProgress = mutableMapOf<KanjiDetail, KanjiProgress>()
    
    lateinit var currentKanji: KanjiDetail
    var currentQuestionType: QuestionType = QuestionType.MEANING
    
    // UI State
    private val _state = MutableStateFlow<GameState>(GameState.Loading)
    val state: StateFlow<GameState> = _state.asStateFlow()
    
    var isAnswerProcessing = false
    var lastAnswerStatus: Boolean? = null
    var showCorrectionFeedback = false
    var correctionDelayPending = false

    // Configuration
    var animationSpeed: Float = 1.0f

    @Suppress("unused") // Kept for potential future use (game restart feature)
    fun resetState() {
        isGameInitialized = false
        allKanjiDetails.clear()
        kanjiListPosition = 0
        currentKanjiSet.clear()
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()
        isAnswerProcessing = false
        lastAnswerStatus = null
        showCorrectionFeedback = false
        correctionDelayPending = false
        _state.value = GameState.Loading
    }

    fun startGame() {
        if (startNewSet()) {
            displayQuestion()
        } else {
            _state.value = GameState.Finished
        }
    }

    fun startNewSet(): Boolean {
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()

        if (kanjiListPosition >= allKanjiDetails.size) {
            return false
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
    
    private fun displayQuestion() {
        if (revisionList.isEmpty()) {
            if (!startNewSet()) {
                _state.value = GameState.Finished
                return
            }
        }
        
        nextQuestion()
        _state.value = GameState.WaitingForAnswer
    }

    fun nextQuestion() {
        if (revisionList.isEmpty()) return

        isAnswerProcessing = false
        lastAnswerStatus = null
        showCorrectionFeedback = false
        correctionDelayPending = false

        currentKanji = revisionList.random()
        val progress = kanjiProgress[currentKanji]!!
        
        currentQuestionType = when {
            !progress.meaningSolved && !progress.readingSolved -> if (Random.nextDouble() < 0.5) QuestionType.MEANING else QuestionType.READING
            !progress.meaningSolved -> QuestionType.MEANING
            else -> QuestionType.READING
        }
    }

    suspend fun submitAnswer(userAnswer: String): Boolean {
        if (isAnswerProcessing) return false
        isAnswerProcessing = true

        val isCorrect = if (currentQuestionType == QuestionType.MEANING) {
            checkMeaning(userAnswer)
        } else {
            checkReading(userAnswer)
        }

        ScoreManager.saveScore(currentKanji.character, isCorrect, ScoreManager.ScoreType.WRITING)

        lastAnswerStatus = isCorrect
        
        if (isCorrect) {
            val progress = kanjiProgress[currentKanji]!!
            if (currentQuestionType == QuestionType.MEANING) progress.meaningSolved = true
            else progress.readingSolved = true

            if (progress.meaningSolved && progress.readingSolved) {
                kanjiStatus[currentKanji] = GameStatus.CORRECT
                revisionList.remove(currentKanji)
            } else {
                kanjiStatus[currentKanji] = GameStatus.PARTIAL
            }
            
            _state.value = GameState.ShowingResult(isCorrect)
            delay((1000 * animationSpeed).toLong())
            displayQuestion()
            
        } else {
            kanjiStatus[currentKanji] = GameStatus.INCORRECT
            showCorrectionFeedback = true
            _state.value = GameState.ShowingResult(isCorrect)
            
            // For incorrect answers, feedback is shown, and we might wait longer or wait for user action
            // The original logic had a complex delay. 
            // We can simplify: showing result -> delay -> next question OR just show result and let UI decide when to call next
            
            // To mimic original behavior where feedback time depended on length:
            // The logic was in Fragment. We can move it here or just use a standard delay + factor
            
            var delayMs: Long
            if (currentQuestionType == QuestionType.MEANING) {
                 val len = currentKanji.meanings.joinToString(", ").length
                 delayMs = kotlin.math.max(2000L, len * 100L)
            } else {
                 val len = currentKanji.readings.sumOf { it.value.length }
                 delayMs = kotlin.math.max(2000L, len * 150L)
            }
            
            // Limit max delay
            delayMs = delayMs.coerceAtMost(6000L)
            
            delay((delayMs * animationSpeed).toLong())
            displayQuestion()
        }

        return isCorrect
    }

    private fun checkMeaning(answer: String): Boolean {
        val normalizedAnswer = normalizeForComparison(answer, isReading = false)
        return currentKanji.meanings.any { normalizeForComparison(it, isReading = false) == normalizedAnswer }
    }

    private fun checkReading(answer: String): Boolean {
        val normalizedAnswer = normalizeForComparison(answer, isReading = true)
        return currentKanji.readings.any { normalizeForComparison(it.value, isReading = true) == normalizedAnswer }
    }

    private fun normalizeForComparison(input: String, isReading: Boolean): String {
        var normalized = input.lowercase()

        if (!isReading && textNormalizer != null) {
            normalized = textNormalizer.normalize(normalized)
        }

        if (isReading) {
            normalized = KanaUtils.katakanaToHiragana(normalized)
        }

        return normalized.replace(Regex("[.\\s-]"), "")
    }
}
