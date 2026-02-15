package org.nihongo.mochi.domain.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.Reading
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.statistics.StatisticsType

class WritingGameViewModel(
    private val kanjiRepository: KanjiRepository,
    private val meaningRepository: MeaningRepository,
    private val levelContentProvider: LevelContentProvider,
    private val settingsRepository: SettingsRepository,
    private val scoreRepository: ScoreRepository,
    private val statisticsEngine: StatisticsEngine,
    private val audioPlayer: AudioPlayer,
    textNormalizer: TextNormalizer? = null
) : ViewModel() {
    
    private val engine = WritingGameEngine(scoreRepository, textNormalizer)
    
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    val currentKanji: KanjiDetail?
        get() = engine.currentKanji

    val currentQuestionType: QuestionType
        get() = engine.currentQuestionType

    val currentKanjiSet: MutableList<KanjiDetail>
        get() = engine.currentKanjiSet

    val kanjiStatus: MutableMap<KanjiDetail, GameStatus>
        get() = engine.kanjiStatus
    
    val showCorrectionFeedback: StateFlow<Boolean> = engine.showCorrectionFeedback
    val lastAnswerStatus: StateFlow<Boolean?> = engine.lastAnswerStatus
    val isAnswerProcessing: StateFlow<Boolean> = engine.isAnswerProcessing
    val errorCount: StateFlow<Int> = engine.errorCount
    val state: StateFlow<GameState> = engine.state
    
    private var currentLevelId: String = ""
    private var lastSortOrder: KanjiSortOrder = KanjiSortOrder.DEFAULT
    private var lastQuizSize: Int = 80

    fun setAnimationSpeed(speed: Float) {
        engine.animationSpeed = speed
    }

    fun initializeGame(level: String, customWordList: List<String>? = null, sortOrder: KanjiSortOrder = KanjiSortOrder.DEFAULT, quizSize: Int = 80) {
        this.currentLevelId = level
        this.lastSortOrder = sortOrder
        this.lastQuizSize = quizSize
        
        viewModelScope.launch {
            loadAndStartGame(level, customWordList, sortOrder, quizSize)
        }
    }

    /**
     * Calcule la maîtrise du NIVEAU COMPLET (ex: N5) pour être cohérent avec SagaMap
     */
    fun calculateGlobalMasteryPercent(): Float {
        if (currentLevelId.isEmpty()) return 0f
        return statisticsEngine.getPercentageForLevel(currentLevelId, StatisticsType.WRITING).toFloat() / 100f
    }

    /**
     * Calcule la maîtrise du lot actuel (session en cours)
     */
    fun calculateSessionMasteryPercent(): Float {
        if (engine.allKanjiDetails.isEmpty()) return 0f
        return statisticsEngine.calculateMasteryPercentage(
            engine.allKanjiDetails.map { it.character },
            ScoreManager.ScoreType.WRITING
        ).toFloat() / 100f
    }

    private suspend fun loadAndStartGame(level: String, customWordList: List<String>?, sortOrder: KanjiSortOrder, quizSize: Int) {
        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        val allKanjiEntries = kanjiRepository.getAllKanjiSuspend()
        
        val kanjiCharsForLevel = customWordList ?: levelContentProvider.getCharactersForLevel(level, ScoreManager.ScoreType.WRITING)
        
        // 1. Get entries for level and apply sort order
        val entriesForLevel = allKanjiEntries.filter { kanjiCharsForLevel.contains(it.character) }
        val sortedEntries = when (sortOrder) {
            KanjiSortOrder.FREQUENCY -> entriesForLevel.sortedBy { it.frequency?.toIntOrNull() ?: Int.MAX_VALUE }
            KanjiSortOrder.STROKES -> entriesForLevel.sortedBy { it.strokes?.toIntOrNull() ?: 0 }
            KanjiSortOrder.DEFAULT -> entriesForLevel
        }

        // 2. Create details and filter items with missing data
        val allKanjiDetailsRaw = sortedEntries.map { entry ->
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            
            KanjiDetail(id, character, kanjiMeanings, readingsList)
        }.filter { it.meanings.isNotEmpty() && it.readings.isNotEmpty() }

        // 3. Filter by mastery and take the quiz size
        val filteredByMastery = if (customWordList.isNullOrEmpty() && level != "user_custom_list") {
            allKanjiDetailsRaw.filter {
                val score = scoreRepository.getScore(it.character, ScoreManager.ScoreType.WRITING)
                (score.successes - score.failures) < 10
            }.take(quizSize)
        } else {
            allKanjiDetailsRaw
        }

        engine.allKanjiDetails.clear()
        engine.allKanjiDetails.addAll(filteredByMastery)
        
        // 4. Shuffle the final set if it's not a custom list
        if (customWordList.isNullOrEmpty()) {
            engine.allKanjiDetails.shuffle()
        }
        
        engine.kanjiListPosition = 0
        isGameInitialized = true
        engine.startGame()
    }

    fun submitAnswer(userAnswer: String) {
        viewModelScope.launch {
            val isCorrect = engine.submitAnswer(userAnswer)
            if (isCorrect) {
                audioPlayer.playSound("sounds/correct.mp3")
            } else {
                audioPlayer.playSound("sounds/incorrect.mp3")
            }
        }
    }

    fun replay() {
        isGameInitialized = false
        initializeGame(currentLevelId, null, lastSortOrder, lastQuizSize)
    }

    fun resetState() {
        engine.resetState()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAll()
    }
}
