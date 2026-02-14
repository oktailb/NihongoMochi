package org.nihongo.mochi.domain.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaType
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanaCharacter
import org.nihongo.mochi.domain.models.KanaQuestionDirection
import org.nihongo.mochi.domain.services.AudioPlayer

class KanaQuizViewModel(
    private val kanaRepository: KanaRepository,
    private val scoreRepository: ScoreRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val engine = KanaQuizEngine(scoreRepository)

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
            val isNormal = currentDirection == KanaQuestionDirection.NORMAL
            val correctAnswerText = if (isNormal) currentQuestion.romaji else currentQuestion.kana
            val isCorrect = selectedAnswer == correctAnswerText
            
            if (isCorrect) {
                audioPlayer.playSound("sounds/correct.mp3")
            } else {
                audioPlayer.playSound("sounds/incorrect.mp3")
            }
            
            engine.submitAnswer(selectedAnswer, selectedIndex)
        }
    }

    fun initializeGame(kanaType: KanaType, quizModeStr: String, levelStr: String): Boolean {
        resetState()
        quizMode = if (quizModeStr == "Kana -> Romaji") QuizMode.KANA_TO_ROMAJI else QuizMode.ROMAJI_TO_KANA

        val allAvailable = loadKana(kanaType)
        
        // Progression pédagogique
        val gojuon = allAvailable.filter { it.category == "gojuon" }
        val dakuon = allAvailable.filter { it.category == "dakuon" || it.category == "handakuon" }
        val yoon = allAvailable.filter { it.category == "yoon" }

        // 2. On calcule la maîtrise du Gojuon
        val gojuonMastery = calculateMastery(gojuon)
        
        val finalPool = mutableListOf<KanaCharacter>()
        finalPool.addAll(gojuon)

        // 3. Si Gojuon > 20% de maîtrise (en moyenne 1 point sur 10 par item)
        if (gojuonMastery >= 0.20f) {
            finalPool.addAll(dakuon)
            
            // 4. On calcule la maîtrise globale (Gojuon + Dakuon)
            val dakuonMastery = calculateMastery(finalPool)
            
            // 5. Si global > 45%, on ajoute les Yoons
            if (dakuonMastery >= 0.45f) {
                finalPool.addAll(yoon)
            }
        }

        allKana = finalPool.shuffled()

        return if (allKana.isNotEmpty()) {
            startGame()
            isGameInitialized = true
            true
        } else {
            false
        }
    }

    fun calculateMasteryPercent(): Float {
        return calculateMastery(allKana)
    }

    private fun calculateMastery(characters: List<KanaCharacter>): Float {
        if (characters.isEmpty()) return 0f
        
        var totalPoints = 0f
        characters.forEach { char ->
            val score = scoreRepository.getScore(char.kana, ScoreManager.ScoreType.RECOGNITION)
            val mastery = (score.successes - score.failures).coerceIn(0, 10)
            totalPoints += mastery
        }
        
        return totalPoints / (characters.size * 10f)
    }

    private fun loadKana(type: KanaType): List<KanaCharacter> {
        return kanaRepository.getKanaEntries(type).map { entry ->
            KanaCharacter(entry.character, entry.romaji, entry.category)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAll()
    }
}
