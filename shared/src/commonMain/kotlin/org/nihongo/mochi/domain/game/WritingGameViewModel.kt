package org.nihongo.mochi.domain.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanjiDetail

class WritingGameViewModel : ViewModel() {
    
    // Default normalizer (or inject via constructor later if needed for platform specific behavior)
    // For pure KMP, we should use a shared normalizer or inject it.
    // For now, we will use a basic one if not provided, assuming the engine handles basic normalization.
    // However, the original code used java.text.Normalizer. In KMP, we don't have that directly in commonMain.
    // We can rely on the engine's default behavior if it's sufficient, or we need to inject it.
    // The engine's constructor: class WritingGameEngine(private val textNormalizer: TextNormalizer? = null)
    
    private val engine = WritingGameEngine()
    
    // Delegate properties to Engine
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    val allKanjiDetails: MutableList<KanjiDetail>
        get() = engine.allKanjiDetails

    val allKanjiDetailsXml = mutableListOf<KanjiDetail>()

    var kanjiListPosition: Int
        get() = engine.kanjiListPosition
        set(value) { engine.kanjiListPosition = value }

    val currentKanjiSet: MutableList<KanjiDetail>
        get() = engine.currentKanjiSet

    val kanjiStatus: MutableMap<KanjiDetail, GameStatus>
        get() = engine.kanjiStatus

    val currentKanji: KanjiDetail
        get() = engine.currentKanji

    val currentQuestionType: QuestionType
        get() = engine.currentQuestionType

    // Observables
    val state: StateFlow<GameState> = engine.state
    
    fun setAnimationSpeed(speed: Float) {
        engine.animationSpeed = speed
    }

    fun startGame() {
        engine.startGame()
    }

    fun submitAnswer(userAnswer: String) {
        viewModelScope.launch {
            engine.submitAnswer(userAnswer)
        }
    }
}
