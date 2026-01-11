package org.nihongo.mochi.domain.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
    textNormalizer: TextNormalizer? = null
) : ViewModel() {
    
    private val engine = WritingGameEngine(textNormalizer)
    
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    // Properties accessed by UI (driven by state changes)
    val currentKanji: KanjiDetail
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
        
        val allKanjiDetails = mutableListOf<KanjiDetail>()
        for (entry in allKanjiEntries) {
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            
            allKanjiDetails.add(KanjiDetail(id, character, kanjiMeanings, readingsList))
        }

        val kanjiCharsForLevel = levelContentProvider.getCharactersForLevel(level)
        
        engine.allKanjiDetails.clear()
        engine.allKanjiDetails.addAll(
            allKanjiDetails.filter { kanjiCharsForLevel.contains(it.character) }
        )
        engine.allKanjiDetails.shuffle()
        engine.kanjiListPosition = 0

        if (engine.allKanjiDetails.isNotEmpty()) {
            engine.startGame()
            isGameInitialized = true
        } else {
            engine.startGame()
        }
    }

    fun submitAnswer(userAnswer: String) {
        viewModelScope.launch {
            engine.submitAnswer(userAnswer)
        }
    }
}
