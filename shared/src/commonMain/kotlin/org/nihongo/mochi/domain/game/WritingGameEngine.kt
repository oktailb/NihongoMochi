package org.nihongo.mochi.domain.game

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kana.KanaUtils
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress
import kotlin.random.Random

enum class QuestionType { MEANING, READING }

class WritingGameEngine(
    private val scoreRepository: ScoreRepository,
    private val textNormalizer: TextNormalizer? = null
) {
    var isGameInitialized = false
    
    val allKanjiDetails = mutableListOf<KanjiDetail>()
    
    var kanjiListPosition = 0
    val currentKanjiSet = mutableListOf<KanjiDetail>()
    val revisionList = mutableListOf<KanjiDetail>()
    val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    val kanjiProgress = mutableMapOf<KanjiDetail, KanjiProgress>()
    
    lateinit var currentKanji: KanjiDetail
    var currentQuestionType: QuestionType = QuestionType.MEANING
    
    // UI State - Reactive
    private val _state = MutableStateFlow<GameState>(GameState.Loading)
    val state: StateFlow<GameState> = _state.asStateFlow()
    
    private val _isAnswerProcessing = MutableStateFlow(false)
    val isAnswerProcessing: StateFlow<Boolean> = _isAnswerProcessing.asStateFlow()

    private val _lastAnswerStatus = MutableStateFlow<Boolean?>(null)
    val lastAnswerStatus: StateFlow<Boolean?> = _lastAnswerStatus.asStateFlow()

    private val _showCorrectionFeedback = MutableStateFlow(false)
    val showCorrectionFeedback: StateFlow<Boolean> = _showCorrectionFeedback.asStateFlow()

    // Configuration
    var animationSpeed: Float = 1.0f

    fun resetState() {
        isGameInitialized = false
        allKanjiDetails.clear()
        kanjiListPosition = 0
        currentKanjiSet.clear()
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()
        _isAnswerProcessing.value = false
        _lastAnswerStatus.value = null
        _showCorrectionFeedback.value = false
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

        _isAnswerProcessing.value = false
        _lastAnswerStatus.value = null
        _showCorrectionFeedback.value = false

        currentKanji = revisionList.random()
        val progress = kanjiProgress[currentKanji]!!
        
        currentQuestionType = when {
            !progress.meaningSolved && !progress.readingSolved -> if (Random.nextDouble() < 0.5) QuestionType.MEANING else QuestionType.READING
            !progress.meaningSolved -> QuestionType.MEANING
            else -> QuestionType.READING
        }
    }

    suspend fun submitAnswer(userAnswer: String): Boolean {
        if (_isAnswerProcessing.value) return false
        _isAnswerProcessing.value = true

        val isCorrect = if (currentQuestionType == QuestionType.MEANING) {
            checkMeaning(userAnswer)
        } else {
            checkReading(userAnswer)
        }

        scoreRepository.saveScore(currentKanji.character, isCorrect, ScoreManager.ScoreType.WRITING)

        _lastAnswerStatus.value = isCorrect
        
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
            _showCorrectionFeedback.value = true
            _state.value = GameState.ShowingResult(isCorrect)
            
            var delayMs: Long
            if (currentQuestionType == QuestionType.MEANING) {
                 val len = currentKanji.meanings.joinToString(", ").length
                 delayMs = kotlin.math.max(2000L, len * 100L)
            } else {
                 val len = currentKanji.readings.sumOf { it.value.length }
                 delayMs = kotlin.math.max(2000L, len * 150L)
            }
            
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
