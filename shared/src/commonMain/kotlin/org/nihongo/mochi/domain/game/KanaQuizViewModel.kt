package org.nihongo.mochi.domain.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaType
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanaCharacter
import org.nihongo.mochi.domain.models.KanaQuestionDirection

class KanaQuizViewModel(
    private val kanaRepository: KanaRepository
) : ViewModel() {

    private val engine = KanaQuizEngine()

    // Delegate properties to Engine
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    var quizMode: QuizMode
        get() = engine.quizMode
        set(value) { engine.quizMode = value }
    
    // Data
    var allKana: List<KanaCharacter>
        get() = engine.allKana
        set(value) { engine.allKana = value }
    
    val currentKanaSet: MutableList<KanaCharacter>
        get() = engine.currentKanaSet

    val kanaStatus: MutableMap<KanaCharacter, GameStatus>
        get() = engine.kanaStatus

    val currentQuestion: KanaCharacter
        get() = engine.currentQuestion

    val currentDirection: KanaQuestionDirection
        get() = engine.currentDirection
    
    val currentAnswers: List<String>
        get() = engine.currentAnswers

    val state: StateFlow<GameState> = engine.state
    val buttonStates: StateFlow<List<AnswerButtonState>> = engine.buttonStates

    // UI Specific State
    var areButtonsEnabled = true
    
    fun setAnimationSpeed(speed: Float) {
        engine.animationSpeed = speed
    }
    
    fun resetState() {
        engine.resetState()
        areButtonsEnabled = true
    }
    
    fun startGame() {
        engine.startGame()
    }
    
    fun submitAnswer(selectedAnswer: String, selectedIndex: Int) {
        viewModelScope.launch {
            engine.submitAnswer(selectedAnswer, selectedIndex)
        }
    }

    fun initializeGame(kanaType: KanaType, quizModeStr: String, levelStr: String): Boolean {
        resetState()
        quizMode = if (quizModeStr == "Kana -> Romaji") QuizMode.KANA_TO_ROMAJI else QuizMode.ROMAJI_TO_KANA

        val allCharacters = loadKana(kanaType)
        allKana = filterCharactersForLevel(allCharacters, levelStr).shuffled()

        return if (allKana.isNotEmpty()) {
            startGame()
            isGameInitialized = true
            true
        } else {
            false
        }
    }

    private fun loadKana(type: KanaType): List<KanaCharacter> {
        return kanaRepository.getKanaEntries(type).map { entry ->
            KanaCharacter(entry.character, entry.romaji, entry.category)
        }
    }

    private fun filterCharactersForLevel(allCharacters: List<KanaCharacter>, level: String): List<KanaCharacter> {
        return when (level) {
            "Gojūon" -> allCharacters.filter { it.category == "gojuon" }
            "Dakuon" -> allCharacters.filter { it.category == "dakuon" || it.category == "handakuon" }
            "Yōon" -> allCharacters.filter { it.category == "yoon" }
            else -> allCharacters
        }
    }
}
