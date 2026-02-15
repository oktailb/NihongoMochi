package org.nihongo.mochi.ui.grammar

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.grammar.Exercise
import org.nihongo.mochi.domain.grammar.ExercisePayload
import org.nihongo.mochi.domain.grammar.ExerciseRepository
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.statistics.StatisticsType
import org.nihongo.mochi.presentation.ViewModel
import kotlin.random.Random

data class GrammarQuizState(
    val currentExercise: Exercise? = null,
    val score: Int = 0,
    val gameState: GameState = GameState.Loading,
    val currentExercisePayload: ExercisePayload? = null,
    val currentOptions: List<String> = emptyList(),
    val selectedOption: String? = null,
    val isAnswerCorrect: Boolean? = null,
    val progressHistory: List<GameStatus> = List(10) { GameStatus.NOT_ANSWERED },
    val currentStarIndex: Int = 0,
    val globalMasteryPercent: Float = 0f,
    val sessionMasteryPercent: Float = 0f
)

class GrammarQuizViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
    private val scoreRepository: ScoreRepository,
    private val statisticsEngine: StatisticsEngine,
    private val audioPlayer: AudioPlayer,
    private val grammarTags: List<String>
) : ViewModel() {

    private val _state = MutableStateFlow(GrammarQuizState())
    val state: StateFlow<GrammarQuizState> = _state.asStateFlow()

    private var allExercises = listOf<Exercise>()
    private var currentSet = listOf<Exercise>()
    private val revisionList = mutableListOf<Exercise>()
    private val exercisesStatus = mutableMapOf<String, GameStatus>()
    private var exercisesListPosition = 0

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            _state.value = _state.value.copy(gameState = GameState.Loading)
            
            val allPossibleExercises = mutableListOf<Exercise>()
            grammarTags.forEach { tag ->
                allPossibleExercises.addAll(exerciseRepository.getExercisesForTag(tag, limit = 10))
            }
            
            allExercises = allPossibleExercises.shuffled()
            
            if (allExercises.isNotEmpty()) {
                if (startNewSet()) {
                    setupQuestion()
                } else {
                    finishGame()
                }
            } else {
                finishGame()
            }
        }
    }

    private fun startNewSet(): Boolean {
        revisionList.clear()
        exercisesStatus.clear()

        if (exercisesListPosition >= allExercises.size) {
            return false
        }

        currentSet = allExercises.drop(exercisesListPosition).take(10)
        exercisesListPosition += currentSet.size

        revisionList.addAll(currentSet)
        currentSet.forEach {
            exercisesStatus[it.id] = GameStatus.NOT_ANSWERED
        }
        return true
    }

    private fun setupQuestion() {
        val exercise = revisionList.randomOrNull()
        if (exercise == null) {
            if (startNewSet()) {
                setupQuestion()
            } else {
                finishGame()
            }
            return
        }

        val payload = exerciseRepository.parsePayload(exercise)
        val options = generateOptions(payload)
        val starIndex = if (payload is ExercisePayload.SentenceOrder) Random.nextInt(payload.blocks.size) else 0
        
        _state.value = _state.value.copy(
            currentExercise = exercise,
            currentExercisePayload = payload,
            currentOptions = options,
            currentStarIndex = starIndex,
            selectedOption = null,
            isAnswerCorrect = null,
            progressHistory = currentSet.map { exercisesStatus[it.id] ?: GameStatus.NOT_ANSWERED },
            gameState = GameState.WaitingForAnswer
        )
    }

    private fun generateOptions(payload: ExercisePayload?): List<String> {
        return when (payload) {
            is ExercisePayload.FillBlank -> {
                val distractors = payload.distractors.shuffled().take(3)
                (distractors + payload.correct).shuffled()
            }
            is ExercisePayload.Underline -> {
                val distractors = payload.distractors.shuffled().take(3)
                (distractors + payload.correct).shuffled()
            }
            is ExercisePayload.Paraphrase -> {
                val distractors = payload.distractors.shuffled().take(3)
                (distractors + payload.correct).shuffled()
            }
            is ExercisePayload.WordUsage -> {
                val correctOnes = payload.options.filter { it.is_correct }.shuffled()
                val incorrectOnes = payload.options.filter { !it.is_correct }.shuffled()
                val selected = (correctOnes.take(1) + incorrectOnes.take(3)).map { it.text }
                selected.shuffled()
            }
            is ExercisePayload.SentenceOrder -> payload.blocks.shuffled()
            else -> emptyList()
        }
    }

    fun onOptionSelected(option: String) {
        val currentState = _state.value
        val exercise = currentState.currentExercise ?: return
        if (currentState.selectedOption != null) return

        val isCorrect = checkAnswer(option, currentState.currentExercisePayload, currentState.currentStarIndex)
        
        if (isCorrect) {
            audioPlayer.playSound("sounds/correct.mp3")
        } else {
            audioPlayer.playSound("sounds/incorrect.mp3")
        }

        exercise.tags.forEach { tag ->
            scoreRepository.saveScore(tag, isCorrect, ScoreManager.ScoreType.GRAMMAR)
        }

        if (isCorrect) {
            exercisesStatus[exercise.id] = GameStatus.CORRECT
            revisionList.remove(exercise)
        } else {
            exercisesStatus[exercise.id] = GameStatus.INCORRECT
        }

        _state.value = currentState.copy(
            selectedOption = option,
            isAnswerCorrect = isCorrect,
            score = if (isCorrect) currentState.score + 1 else currentState.score,
            progressHistory = currentSet.map { exercisesStatus[it.id] ?: GameStatus.NOT_ANSWERED },
            gameState = GameState.ShowingResult(isCorrect, currentState.currentOptions.indexOf(option))
        )

        viewModelScope.launch {
            val factor = settingsRepository.getAnimationSpeed()
            delay((1000 * factor).toLong())
            setupQuestion()
        }
    }

    private fun checkAnswer(option: String, payload: ExercisePayload?, starIndex: Int): Boolean {
        return when (payload) {
            is ExercisePayload.FillBlank -> option == payload.correct
            is ExercisePayload.Underline -> option == payload.correct
            is ExercisePayload.Paraphrase -> option == payload.correct
            is ExercisePayload.WordUsage -> payload.options.find { it.text == option }?.is_correct ?: false
            is ExercisePayload.SentenceOrder -> {
                if (starIndex < payload.blocks.size) {
                    option == payload.blocks[starIndex]
                } else false
            }
            else -> false
        }
    }

    private fun finishGame() {
        val sessionMastery = calculateSessionMastery()
        val globalMastery = calculateGlobalMastery()
        
        _state.value = _state.value.copy(
            gameState = GameState.Finished,
            sessionMasteryPercent = sessionMastery,
            globalMasteryPercent = globalMastery
        )
    }

    private fun calculateSessionMastery(): Float {
        if (currentSet.isEmpty()) return 0f
        // Current tags are the base for global mastery, but for session we can average the mastery of used tags
        val totalMastery = grammarTags.sumOf { tag ->
            statisticsEngine.calculateMasteryPercentage(listOf(tag), ScoreManager.ScoreType.GRAMMAR)
        }
        return (totalMastery / grammarTags.size.toDouble()).toFloat() / 100f
    }

    private fun calculateGlobalMastery(): Float {
        if (grammarTags.isEmpty()) return 0f
        val totalMastery = grammarTags.sumOf { tag ->
            statisticsEngine.calculateMasteryPercentage(listOf(tag), ScoreManager.ScoreType.GRAMMAR)
        }
        return (totalMastery / grammarTags.size.toDouble()).toFloat() / 100f
    }

    fun replay() {
        exercisesListPosition = 0
        loadExercises()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAll()
    }
}
