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
    
    private var _currentKanji: KanjiDetail? = null
    val currentKanji: KanjiDetail? get() = _currentKanji

    var currentQuestionType: QuestionType = QuestionType.MEANING
    
    private val _state = MutableStateFlow<GameState>(GameState.Loading)
    val state: StateFlow<GameState> = _state.asStateFlow()
    
    private val _isAnswerProcessing = MutableStateFlow(false)
    val isAnswerProcessing: StateFlow<Boolean> = _isAnswerProcessing.asStateFlow()

    private val _lastAnswerStatus = MutableStateFlow<Boolean?>(null)
    val lastAnswerStatus: StateFlow<Boolean?> = _lastAnswerStatus.asStateFlow()

    private val _showCorrectionFeedback = MutableStateFlow(false)
    val showCorrectionFeedback: StateFlow<Boolean> = _showCorrectionFeedback.asStateFlow()

    var animationSpeed: Float = 1.0f

    fun resetState() {
        isGameInitialized = false
        allKanjiDetails.clear()
        kanjiListPosition = 0
        currentKanjiSet.clear()
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()
        _currentKanji = null
        _isAnswerProcessing.value = false
        _lastAnswerStatus.value = null
        _showCorrectionFeedback.value = false
        _state.value = GameState.Loading
    }

    fun startGame() {
        if (allKanjiDetails.isEmpty()) {
            _state.value = GameState.Finished
            return
        }

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
        if (revisionList.isEmpty()) {
            _state.value = GameState.Finished
            return
        }

        _isAnswerProcessing.value = false
        _lastAnswerStatus.value = null
        _showCorrectionFeedback.value = false

        val nextKanji = revisionList.randomOrNull()
        if (nextKanji == null) {
            _state.value = GameState.Finished
            return
        }

        _currentKanji = nextKanji
        val progress = kanjiProgress[nextKanji]!!
        
        currentQuestionType = when {
            !progress.meaningSolved && !progress.readingSolved -> if (Random.nextDouble() < 0.5) QuestionType.MEANING else QuestionType.READING
            !progress.meaningSolved -> QuestionType.MEANING
            else -> QuestionType.READING
        }
    }

    suspend fun submitAnswer(userAnswer: String): Boolean {
        val kanji = _currentKanji ?: return false
        if (_isAnswerProcessing.value) return false
        _isAnswerProcessing.value = true

        val isCorrect = if (currentQuestionType == QuestionType.MEANING) {
            checkMeaning(userAnswer, kanji)
        } else {
            checkReading(userAnswer, kanji)
        }

        scoreRepository.saveScore(kanji.character, isCorrect, ScoreManager.ScoreType.WRITING)

        _lastAnswerStatus.value = isCorrect
        
        if (isCorrect) {
            val progress = kanjiProgress[kanji]!!
            if (currentQuestionType == QuestionType.MEANING) progress.meaningSolved = true
            else progress.readingSolved = true

            if (progress.meaningSolved && progress.readingSolved) {
                kanjiStatus[kanji] = GameStatus.CORRECT
                revisionList.remove(kanji)
            } else {
                kanjiStatus[kanji] = GameStatus.PARTIAL
            }
            
            _state.value = GameState.ShowingResult(isCorrect)
            delay((1000 * animationSpeed).toLong())
            displayQuestion()
            
        } else {
            kanjiStatus[kanji] = GameStatus.INCORRECT
            _showCorrectionFeedback.value = true
            _state.value = GameState.ShowingResult(isCorrect)
            
            var delayMs: Long
            if (currentQuestionType == QuestionType.MEANING) {
                 val len = kanji.meanings.joinToString(", ").length
                 delayMs = kotlin.math.max(2000L, len * 100L)
            } else {
                 val len = kanji.readings.sumOf { it.value.length }
                 delayMs = kotlin.math.max(2000L, len * 150L)
            }
            
            delayMs = delayMs.coerceAtMost(6000L)
            delay((delayMs * animationSpeed).toLong())
            displayQuestion()
        }

        return isCorrect
    }

    private fun checkMeaning(answer: String, kanji: KanjiDetail): Boolean {
        val normalizedAnswer = normalizeForComparison(answer, isReading = false)
        return kanji.meanings.any { normalizeForComparison(it, isReading = false) == normalizedAnswer }
    }

    private fun checkReading(answer: String, kanji: KanjiDetail): Boolean {
        val normalizedAnswer = normalizeForComparison(answer, isReading = true)
        return kanji.readings.any { normalizeForComparison(it.value, isReading = true) == normalizedAnswer }
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
