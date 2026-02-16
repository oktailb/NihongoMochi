package org.nihongo.mochi.domain.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.LearningScore
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.Reading
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.statistics.StatisticsType

enum class KanjiSortOrder {
    DEFAULT, FREQUENCY, STROKES
}

class RecognitionGameViewModel(
    private val kanjiRepository: KanjiRepository,
    private val meaningRepository: MeaningRepository,
    private val levelContentProvider: LevelContentProvider,
    private val settingsRepository: SettingsRepository,
    private val scoreRepository: ScoreRepository,
    private val statisticsEngine: StatisticsEngine,
    private val audioPlayer: AudioPlayer
) : ViewModel() {
    
    private val engine = RecognitionGameEngine(scoreRepository)
    
    // Delegate properties to Engine
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    private val allKanjiDetailsXml = mutableListOf<KanjiDetail>()
    
    val allKanjiDetails: MutableList<KanjiDetail>
        get() = engine.allKanjiDetails

    val currentKanjiSet: MutableList<KanjiDetail>
        get() = engine.currentKanjiSet

    val kanjiStatus: MutableMap<KanjiDetail, GameStatus>
        get() = engine.kanjiStatus

    var kanjiListPosition: Int
        get() = engine.kanjiListPosition
        set(value) { engine.kanjiListPosition = value }

    val currentKanji: KanjiDetail?
        get() = engine.currentKanji

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
    val errorCount: StateFlow<Int> = engine.errorCount

    var areButtonsEnabled = true

    private var currentLevelId: String = ""
    private var lastSortOrder: KanjiSortOrder = KanjiSortOrder.DEFAULT
    private var lastQuizSize: Int = 80

    fun getCurrentKanjiScore(): LearningScore? {
        if (!isGameInitialized || engine.state.value == GameState.Finished) return null
        return try {
            val kanji = engine.currentKanji ?: return null
            val type = ScoreManager.ScoreType.RECOGNITION
            scoreRepository.getScore(kanji.character, type)
        } catch(e: Exception) {
            null
        }
    }

    /**
     * Calcule la maîtrise globale du niveau pour SagaMap
     */
    fun calculateGlobalMasteryPercent(): Float {
        if (currentLevelId.isEmpty()) return 0f
        val type = StatisticsType.RECOGNITION
        return statisticsEngine.getPercentageForLevel(currentLevelId, type).toFloat() / 100f
    }

    /**
     * Calcule la maîtrise du lot actuel (session courante)
     */
    fun calculateSessionMasteryPercent(): Float {
        if (allKanjiDetails.isEmpty()) return 0f
        val type = ScoreManager.ScoreType.RECOGNITION
        
        // On calcule le score sur l'ensemble des kanjis sélectionnés pour ce quiz (ex: les 80 items)
        return statisticsEngine.calculateMasteryPercentage(
            allKanjiDetails.map { it.character },
            type
        ).toFloat() / 100f
    }

    fun updatePronunciationMode(mode: String) {
        engine.pronunciationMode = mode
    }
    
    fun setAnimationSpeed(speed: Float) {
        engine.animationSpeed = speed
    }

    fun startGame() {
        engine.startGame()
    }

    fun getFormattedReadings(kanji: KanjiDetail): String {
        return engine.getFormattedReadings(kanji)
    }

    fun submitAnswer(selectedAnswer: String, selectedIndex: Int) {
        viewModelScope.launch {
            val isCorrect = if (currentDirection == QuestionDirection.NORMAL) {
                selectedAnswer.lines().any { it.trim().equals(engine.correctAnswer.trim(), ignoreCase = true) }
            } else {
                selectedAnswer == engine.correctAnswer
            }

            if (isCorrect) {
                audioPlayer.playSound("sounds/correct.mp3")
            } else {
                audioPlayer.playSound("sounds/incorrect.mp3")
            }

            engine.submitAnswer(selectedAnswer, selectedIndex)
        }
    }

    fun resetState() {
        engine.resetState()
        areButtonsEnabled = true
    }

    fun initializeGame(
        gameMode: String, 
        readingMode: String, 
        level: String, 
        customWordList: List<String>?,
        sortOrder: KanjiSortOrder = KanjiSortOrder.DEFAULT,
        quizSize: Int = 80
    ): Boolean {
        resetState()
        this.gameMode = gameMode
        this.readingMode = readingMode
        this.currentLevelId = level
        this.lastSortOrder = sortOrder
        this.lastQuizSize = quizSize

        loadAllKanjiDetails()

        val type = if (gameMode == "meaning") ScoreManager.ScoreType.RECOGNITION else ScoreManager.ScoreType.READING

        val kanjiCharsForLevel: List<String> = if (!customWordList.isNullOrEmpty()) {
            customWordList
        } else {
            levelContentProvider.getCharactersForLevel(level, type)
        }

        allKanjiDetails.clear()
        
        val normalizedLevel = level.lowercase()
        val filteredList = allKanjiDetailsXml.filter {
            var include = kanjiCharsForLevel.contains(it.character)

            if (normalizedLevel == "no meaning") {
            } else if (gameMode == "meaning") {
                include = include && it.meanings.isNotEmpty()
            }

            if (normalizedLevel == "no reading") {
            } else if (gameMode == "reading") {
                include = include && it.readings.isNotEmpty()
            }
            
            include
        }

        val sortedList = when (sortOrder) {
            KanjiSortOrder.FREQUENCY -> filteredList.sortedBy { it.frequency }
            KanjiSortOrder.STROKES -> filteredList.sortedBy { it.strokes }
            else -> filteredList // Keep order from database/list provider
        }

        // limite le quizz aux X premier kanjis de la liste (triee selon le critere choisi) non parfaitement maitrises
        val filteredByMastery = if (customWordList.isNullOrEmpty()) {
            sortedList.filter {
                val score = scoreRepository.getScore(it.character, type)
                (score.successes - score.failures) < 10
            }.take(quizSize)
        } else {
            sortedList
        }

        allKanjiDetails.addAll(filteredByMastery)
        
        if (customWordList.isNullOrEmpty()) {
            allKanjiDetails.shuffle()
        }
        
        kanjiListPosition = 0

        isGameInitialized = true
        if (allKanjiDetails.isNotEmpty()) {
            startGame()
            return true
        } else {
            engine.startGame() 
            return false
        }
    }

    fun replay() {
        initializeGame(gameMode, readingMode, currentLevelId, null, lastSortOrder, lastQuizSize)
    }

    private fun loadAllKanjiDetails() {
        if (allKanjiDetailsXml.isNotEmpty()) return

        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        val allKanjiEntries = kanjiRepository.getAllKanji()
        
        allKanjiDetailsXml.clear()
        
        for (entry in allKanjiEntries) {
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            
            val kanjiDetail = KanjiDetail(
                id = id, 
                character = character, 
                meanings = kanjiMeanings, 
                readings = readingsList,
                frequency = entry.frequency?.toIntOrNull() ?: Int.MAX_VALUE,
                strokes = entry.strokes?.toIntOrNull() ?: 0
            )
            allKanjiDetailsXml.add(kanjiDetail)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAll()
    }
}
