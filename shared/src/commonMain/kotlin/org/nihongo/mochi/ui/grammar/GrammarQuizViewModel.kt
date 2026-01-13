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
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.presentation.ViewModel
import kotlin.random.Random

data class GrammarQuizState(
    val exercises: List<Exercise> = emptyList(),
    val currentIndex: Int = 0,
    val score: Int = 0,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val currentExercisePayload: ExercisePayload? = null,
    val currentOptions: List<String> = emptyList(),
    val selectedOption: String? = null,
    val isAnswerCorrect: Boolean? = null,
    val progressHistory: List<GameStatus> = emptyList(),
    val currentStarIndex: Int = 0
)

class GrammarQuizViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
    private val scoreRepository: ScoreRepository,
    private val grammarTags: List<String> // Changed to List
) : ViewModel() {

    private val _state = MutableStateFlow(GrammarQuizState())
    val state: StateFlow<GrammarQuizState> = _state.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            // Fetch exercises for ALL tags and limit total to 100
            val allPossibleExercises = mutableListOf<Exercise>()
            grammarTags.forEach { tag ->
                // Fetch up to 10 exercises per tag to ensure variety, then we'll shuffle and take 100
                allPossibleExercises.addAll(exerciseRepository.getExercisesForTag(tag, limit = 10))
            }
            
            val exercises = allPossibleExercises.shuffled().take(100)
            
            if (exercises.isNotEmpty()) {
                setupQuestion(exercises, 0, emptyList())
            } else {
                _state.value = _state.value.copy(isFinished = true, isLoading = false)
            }
        }
    }

    private fun setupQuestion(exercises: List<Exercise>, index: Int, history: List<GameStatus>) {
        val exercise = exercises[index]
        val payload = exerciseRepository.parsePayload(exercise)
        val options = generateOptions(payload)
        val starIndex = if (payload is ExercisePayload.SentenceOrder) Random.nextInt(payload.blocks.size) else 0
        
        _state.value = _state.value.copy(
            exercises = exercises,
            currentIndex = index,
            currentExercisePayload = payload,
            currentOptions = options,
            currentStarIndex = starIndex,
            selectedOption = null,
            isAnswerCorrect = null,
            progressHistory = history + List(exercises.size - history.size) { GameStatus.NOT_ANSWERED },
            isLoading = false
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
        if (currentState.selectedOption != null) return

        val isCorrect = checkAnswer(option, currentState.currentExercisePayload, currentState.currentStarIndex)
        
        // Save score for each tag associated with the rule
        val currentExercise = currentState.exercises.getOrNull(currentState.currentIndex)
        currentExercise?.tags?.forEach { tag ->
            scoreRepository.saveScore(tag, isCorrect, ScoreManager.ScoreType.GRAMMAR)
        }

        val newHistory = currentState.progressHistory.toMutableList()
        if (currentState.currentIndex < newHistory.size) {
            newHistory[currentState.currentIndex] = if (isCorrect) GameStatus.CORRECT else GameStatus.INCORRECT
        }

        _state.value = currentState.copy(
            selectedOption = option,
            isAnswerCorrect = isCorrect,
            score = if (isCorrect) currentState.score + 1 else currentState.score,
            progressHistory = newHistory
        )

        viewModelScope.launch {
            val factor = settingsRepository.getAnimationSpeed()
            delay((1000 * factor).toLong())
            nextQuestion()
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

    private fun nextQuestion() {
        val currentState = _state.value
        val nextIndex = currentState.currentIndex + 1
        
        if (nextIndex < currentState.exercises.size) {
            setupQuestion(currentState.exercises, nextIndex, currentState.progressHistory.take(nextIndex))
        } else {
            _state.value = _state.value.copy(isFinished = true)
        }
    }
}
