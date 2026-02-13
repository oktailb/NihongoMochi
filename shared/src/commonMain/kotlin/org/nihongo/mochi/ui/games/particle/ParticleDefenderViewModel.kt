package org.nihongo.mochi.ui.games.particle

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.grammar.ExercisePayload
import org.nihongo.mochi.domain.grammar.ExerciseRepository
import org.nihongo.mochi.domain.grammar.ExerciseType
import kotlin.random.Random

data class ParticleEnemy(
    val id: Long,
    val char: String,
    val position: Offset,
    val isCorrect: Boolean,
    val speed: Float
)

data class ParticleGameState(
    val currentSentencePrefix: String = "",
    val currentSentenceSuffix: String = "",
    val activeParticles: List<ParticleEnemy> = emptyList(),
    val score: Int = 0,
    val lives: Int = 3,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val isLoading: Boolean = true,
    val shipX: Float = 0.5f // 0.0 to 1.0
)

class ParticleDefenderViewModel(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParticleGameState())
    val uiState = _uiState.asStateFlow()

    private var gameJob: Job? = null
    private val particlesToUse = listOf("は", "が", "を", "に", "へ", "と", "も", "で")
    private var enemyIdCounter = 0L

    init {
        startGame()
    }

    fun startGame() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isGameOver = false, isPaused = false, lives = 3, score = 0) }
            loadNextExercise()
            _uiState.update { it.copy(isLoading = false) }
            runGameLoop()
        }
    }

    fun pauseGame() {
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        _uiState.update { it.copy(isPaused = false) }
    }

    private suspend fun loadNextExercise() {
        val tags = listOf("particule_ha", "particule_ga", "particule_ni", "particule_wo", "particule_de", "particule_he")
        val randomTag = tags.random()
        val exercises = exerciseRepository.getExercisesForTag(randomTag)
        
        val exercise = exercises.filter { it.type == ExerciseType.FILL_BLANK }.randomOrNull()
        
        if (exercise != null) {
            val payload = exerciseRepository.parsePayload(exercise) as? ExercisePayload.FillBlank
            if (payload != null) {
                val sentence = payload.sentence
                val correct = payload.correct
                val parts = sentence.split(correct, limit = 2)
                _uiState.update {
                    it.copy(
                        currentSentencePrefix = parts.getOrNull(0) ?: "",
                        currentSentenceSuffix = parts.getOrNull(1) ?: "",
                        activeParticles = emptyList()
                    )
                }
                spawnWave(correct)
                return
            }
        }
        
        _uiState.update {
            it.copy(
                currentSentencePrefix = "私はパン",
                currentSentenceSuffix = "食べます。",
                activeParticles = emptyList()
            )
        }
        spawnWave("を")
    }

    private fun spawnWave(correctParticle: String) {
        val newParticles = mutableListOf<ParticleEnemy>()
        val xSlots = listOf(0.15f, 0.38f, 0.62f, 0.85f).shuffled()
        
        newParticles.add(
            ParticleEnemy(
                id = enemyIdCounter++,
                char = correctParticle,
                position = Offset(xSlots[0], -0.1f),
                isCorrect = true,
                speed = 0.004f + (Random.nextFloat() * 0.002f)
            )
        )
        
        val distractors = particlesToUse.filter { it != correctParticle }.shuffled().take(3)
        distractors.forEachIndexed { index, char ->
            newParticles.add(
                ParticleEnemy(
                    id = enemyIdCounter++,
                    char = char,
                    position = Offset(xSlots[index + 1], -0.1f),
                    isCorrect = false,
                    speed = 0.004f + (Random.nextFloat() * 0.002f)
                )
            )
        }
        
        _uiState.update { it.copy(activeParticles = newParticles) }
    }

    private fun runGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (!_uiState.value.isGameOver) {
                if (!_uiState.value.isPaused) {
                    delay(16)
                    updateParticles()
                } else {
                    delay(100) // Lower CPU when paused
                }
            }
        }
    }

    private fun updateParticles() {
        _uiState.update { state ->
            val updated = state.activeParticles.map { 
                it.copy(position = it.position.copy(y = it.position.y + it.speed))
            }
            
            val correctReachedBottom = updated.any { it.isCorrect && it.position.y > 1.0f }
            
            if (correctReachedBottom) {
                val newLives = state.lives - 1
                if (newLives <= 0) {
                    state.copy(isGameOver = true, lives = 0, activeParticles = updated)
                } else {
                    state.copy(lives = newLives, activeParticles = emptyList())
                }
            } else {
                state.copy(activeParticles = updated.filter { it.position.y <= 1.1f })
            }
        }
        
        if (_uiState.value.activeParticles.isEmpty() && !_uiState.value.isGameOver) {
            viewModelScope.launch { loadNextExercise() }
        }
    }

    fun onShipMove(x: Float) {
        if (_uiState.value.isPaused) return
        _uiState.update { it.copy(shipX = x.coerceIn(0f, 1f)) }
    }

    fun onShoot() {
        val state = _uiState.value
        if (state.isGameOver || state.isPaused) return

        val shipX = state.shipX
        val target = state.activeParticles.filter { 
            it.position.y > 0.4f && kotlin.math.abs(it.position.x - shipX) < 0.12f
        }.minByOrNull { it.position.y }
        
        if (target != null) {
            if (target.isCorrect) {
                _uiState.update { it.copy(score = it.score + 10, activeParticles = emptyList()) }
                viewModelScope.launch { loadNextExercise() }
            } else {
                _uiState.update { 
                    val newLives = it.lives - 1
                    it.copy(lives = newLives, isGameOver = newLives <= 0) 
                }
            }
        }
    }

    override fun onCleared() {
        gameJob?.cancel()
        super.onCleared()
    }
}
