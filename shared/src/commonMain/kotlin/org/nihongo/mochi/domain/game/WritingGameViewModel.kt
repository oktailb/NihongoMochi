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
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.util.LevelContentProvider

class WritingGameViewModel(
    private val kanjiRepository: KanjiRepository,
    private val meaningRepository: MeaningRepository,
    private val levelContentProvider: LevelContentProvider,
    private val settingsRepository: SettingsRepository,
    private val scoreRepository: ScoreRepository,
    textNormalizer: TextNormalizer? = null
) : ViewModel() {
    
    private val engine = WritingGameEngine(scoreRepository, textNormalizer)
    
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    // Properties accessed by UI (driven by state changes)
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
    
    fun setAnimationSpeed(speed: Float) {
        engine.animationSpeed = speed
    }

    fun initializeGame(level: String) {
        if (isGameInitialized) return
        
        viewModelScope.launch {
            loadAndStartGame(level)
        }
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

        // Use WRITING_LIST for custom review lists in this ViewModel
        val kanjiCharsForLevel = levelContentProvider.getCharactersForLevel(level, ScoreManager.ScoreType.WRITING)
        
        engine.allKanjiDetails.clear()
        val filtered = allKanjiDetailsRaw.filter { 
            kanjiCharsForLevel.contains(it.character) && 
            it.meanings.isNotEmpty() && 
            it.readings.isNotEmpty() 
        }
        
        engine.allKanjiDetails.addAll(filtered)
        
        // Don't shuffle if it's a user custom list (often used for specific order or small set)
        if (level != "user_custom_list") {
            engine.allKanjiDetails.shuffle()
        }
        
        engine.kanjiListPosition = 0

        isGameInitialized = true
        engine.startGame()
    }

    fun submitAnswer(userAnswer: String) {
        viewModelScope.launch {
            engine.submitAnswer(userAnswer)
        }
    }

    fun resetState() {
        engine.resetState()
    }
}
