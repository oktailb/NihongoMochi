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
    val state: StateFlow<GameState> = engine.state
    
    private var currentLevelId: String = ""

    fun setAnimationSpeed(speed: Float) {
        engine.animationSpeed = speed
    }

    fun initializeGame(level: String) {
        if (isGameInitialized) return
        this.currentLevelId = level
        
        viewModelScope.launch {
            loadAndStartGame(level)
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
        if (currentKanjiSet.isEmpty()) return 0f
        return statisticsEngine.calculateMasteryPercentage(
            currentKanjiSet.map { it.character },
            ScoreManager.ScoreType.WRITING
        ).toFloat() / 100f
    }

    private suspend fun loadAndStartGame(level: String) {
        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        val allKanjiEntries = kanjiRepository.getAllKanjiSuspend()
        
        val allKanjiDetailsRaw = mutableListOf<KanjiDetail>()
        for (entry in allKanjiEntries) {
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            
            allKanjiDetailsRaw.add(KanjiDetail(id, character, kanjiMeanings, readingsList))
        }

        val kanjiCharsForLevel = levelContentProvider.getCharactersForLevel(level, ScoreManager.ScoreType.WRITING)
        
        engine.allKanjiDetails.clear()
        val filtered = allKanjiDetailsRaw.filter { 
            kanjiCharsForLevel.contains(it.character) && 
            it.meanings.isNotEmpty() && 
            it.readings.isNotEmpty() 
        }
        
        engine.allKanjiDetails.addAll(filtered)
        
        if (level != "user_custom_list") {
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
        initializeGame(currentLevelId)
    }

    fun resetState() {
        engine.resetState()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAll()
    }
}
