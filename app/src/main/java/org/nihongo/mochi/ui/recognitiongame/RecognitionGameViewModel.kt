package org.nihongo.mochi.ui.recognitiongame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.game.GameState
import org.nihongo.mochi.domain.game.RecognitionGameEngine
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress

// Re-export type aliases to maintain compatibility with Fragment imports if they use these types from ViewModel package
typealias QuestionDirection = org.nihongo.mochi.domain.game.QuestionDirection

class RecognitionGameViewModel : ViewModel() {
    
    private val engine = RecognitionGameEngine()
    
    // Delegate properties to Engine
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    val allKanjiDetailsXml = mutableListOf<KanjiDetail>()
    
    val allKanjiDetails: MutableList<KanjiDetail>
        get() = engine.allKanjiDetails

    // Read-only access to lists for UI binding (if needed) but mutations should go through Engine methods ideally
    val currentKanjiSet: MutableList<KanjiDetail>
        get() = engine.currentKanjiSet

    val revisionList: MutableList<KanjiDetail>
        get() = engine.revisionList

    val kanjiStatus: MutableMap<KanjiDetail, GameStatus>
        get() = engine.kanjiStatus

    val kanjiProgress: MutableMap<KanjiDetail, KanjiProgress>
        get() = engine.kanjiProgress

    var kanjiListPosition: Int
        get() = engine.kanjiListPosition
        set(value) { engine.kanjiListPosition = value }

    val currentKanji: KanjiDetail
        get() = engine.currentKanji

    val correctAnswer: String
        get() = engine.correctAnswer

    var gameMode: String
        get() = engine.gameMode
        set(value) { engine.gameMode = value }

    var readingMode: String
        get() = engine.readingMode
        set(value) { engine.readingMode = value }

    val currentDirection: QuestionDirection
        get() = engine.currentDirection
    
    val currentAnswers: List<String>
        get() = engine.currentAnswers

    val state: StateFlow<GameState> = engine.state
    val buttonStates: StateFlow<List<AnswerButtonState>> = engine.buttonStates

    // UI Specific State (Platform dependent)
    var areButtonsEnabled = true
    // Removed buttonColors as it's now handled via state flow from engine

    fun updatePronunciationMode(mode: String) {
        engine.pronunciationMode = mode
    }
    
    fun setAnimationSpeed(speed: Float) {
        engine.animationSpeed = speed
    }

    fun startGame() {
        engine.startGame()
    }

    // Next question is handled by engine in flow
    
    fun getFormattedReadings(kanji: KanjiDetail): String {
        return engine.getFormattedReadings(kanji)
    }

    fun submitAnswer(selectedAnswer: String, selectedIndex: Int) {
        viewModelScope.launch {
            engine.submitAnswer(selectedAnswer, selectedIndex)
        }
    }

    fun resetState() {
        engine.resetState()
        allKanjiDetailsXml.clear()
        areButtonsEnabled = true
        // buttonColors cleared implicitly by engine reset
    }
}
