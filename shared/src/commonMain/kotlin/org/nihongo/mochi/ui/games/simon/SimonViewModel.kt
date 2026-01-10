package org.nihongo.mochi.ui.games.simon

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.presentation.ViewModel

class SimonViewModel(
    private val kanjiRepository: KanjiRepository,
    private val meaningRepository: MeaningRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(SimonGameState.IDLE)
    val gameState: StateFlow<SimonGameState> = _gameState.asStateFlow()

    private val _selectedMode = MutableStateFlow(SimonMode.KANJI)
    val selectedMode: StateFlow<SimonMode> = _selectedMode.asStateFlow()

    private val _targetSequence = MutableStateFlow<List<KanjiEntry>>(emptyList())
    val targetSequence: StateFlow<List<KanjiEntry>> = _targetSequence.asStateFlow()

    private val _currentKanji = MutableStateFlow<KanjiEntry?>(null)
    val currentKanji: StateFlow<KanjiEntry?> = _currentKanji.asStateFlow()

    private val _answers = MutableStateFlow<List<Pair<KanjiEntry, String>>>(emptyList())
    val answers: StateFlow<List<Pair<KanjiEntry, String>>> = _answers.asStateFlow()

    private val _isKanjiVisible = MutableStateFlow(false)
    val isKanjiVisible: StateFlow<Boolean> = _isKanjiVisible.asStateFlow()

    private val _isButtonsVisible = MutableStateFlow(false)
    val isButtonsVisible: StateFlow<Boolean> = _isButtonsVisible.asStateFlow()

    private val _gameTimeSeconds = MutableStateFlow(0)
    val gameTimeSeconds: StateFlow<Int> = _gameTimeSeconds.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _scoresHistory = MutableStateFlow<List<SimonGameResult>>(emptyList())
    val scoresHistory: StateFlow<List<SimonGameResult>> = _scoresHistory.asStateFlow()

    private var inputIndex = 0
    private var timerJob: Job? = null
    private var allKanjiInLevel: List<KanjiEntry> = emptyList()
    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadScoresHistory()
    }

    private fun loadScoresHistory() {
        try {
            val historyJson = ScoreManager.getSimonHistory()
            val history = json.decodeFromString<List<SimonGameResult>>(historyJson)
            _scoresHistory.value = history
        } catch (e: Exception) {
            _scoresHistory.value = emptyList()
        }
    }

    fun onModeSelected(mode: SimonMode) {
        _selectedMode.value = mode
    }

    fun startGame() {
        val levelId = settingsRepository.getSelectedLevel()
        allKanjiInLevel = kanjiRepository.getKanjiByLevel(levelId)
        if (allKanjiInLevel.isEmpty()) return

        _targetSequence.value = emptyList()
        _score.value = 0
        _gameTimeSeconds.value = 0
        _gameState.value = SimonGameState.SHOWING_SEQUENCE
        
        startTimer()
        nextRound()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _gameTimeSeconds.value++
            }
        }
    }

    private fun nextRound() {
        viewModelScope.launch {
            _gameState.value = SimonGameState.SHOWING_SEQUENCE
            _isButtonsVisible.value = false
            
            val nextKanji = allKanjiInLevel.shuffled().first()
            val newSequence = _targetSequence.value + nextKanji
            _targetSequence.value = newSequence
            
            newSequence.forEachIndexed { index, kanji ->
                _currentKanji.value = kanji
                val showDuration = if (index == newSequence.size - 1) 2000L else 1000L
                
                _isKanjiVisible.value = true
                delay(showDuration)
                _isKanjiVisible.value = false
                delay(300)
            }
            
            inputIndex = 0
            refreshAnswersForIndex(0)
            _gameState.value = SimonGameState.AWAITING_INPUT
            _isButtonsVisible.value = true
        }
    }

    private fun refreshAnswersForIndex(index: Int) {
        val correctAnswer = _targetSequence.value[index]
        val distractors = allKanjiInLevel
            .filter { it.id != correctAnswer.id }
            .shuffled()
            .take(3)
        
        val mode = _selectedMode.value
        val allOptions = (distractors + correctAnswer).shuffled()
        
        _answers.value = allOptions.map { kanji ->
            kanji to getLabelForKanji(kanji, mode)
        }
    }

    private fun getLabelForKanji(kanji: KanjiEntry, mode: SimonMode): String {
        return when (mode) {
            SimonMode.KANJI -> kanji.character
            SimonMode.MEANING -> {
                val locale = settingsRepository.getAppLocale()
                val meanings = meaningRepository.getMeanings(locale)
                meanings[kanji.id]?.firstOrNull() ?: kanji.character
            }
            SimonMode.READING_COMMON -> {
                kanji.readings?.reading?.firstOrNull { it.frequency == "common" }?.value 
                    ?: kanji.readings?.reading?.firstOrNull()?.value 
                    ?: kanji.character
            }
            SimonMode.READING_RANDOM -> {
                kanji.readings?.reading?.shuffled()?.firstOrNull()?.value ?: kanji.character
            }
        }
    }

    fun onAnswerClick(kanji: KanjiEntry) {
        if (_gameState.value != SimonGameState.AWAITING_INPUT) return

        if (kanji.id == _targetSequence.value[inputIndex].id) {
            inputIndex++
            if (inputIndex >= _targetSequence.value.size) {
                _score.value = _targetSequence.value.size
                nextRound()
            } else {
                refreshAnswersForIndex(inputIndex)
            }
        } else {
            _gameState.value = SimonGameState.GAME_OVER
            timerJob?.cancel()
            saveFinalScore()
        }
    }

    private fun saveFinalScore() {
        val levelId = settingsRepository.getSelectedLevel()
        val result = SimonGameResult(
            levelId = levelId,
            mode = _selectedMode.value,
            maxSequence = _score.value,
            timeSeconds = _gameTimeSeconds.value,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        
        val currentHistory = _scoresHistory.value.toMutableList()
        currentHistory.add(0, result)
        val newHistory = currentHistory.take(10)
        _scoresHistory.value = newHistory
        
        try {
            ScoreManager.saveSimonHistory(json.encodeToString(newHistory))
        } catch (e: Exception) {
        }
    }
    
    fun resetToIdle() {
        _gameState.value = SimonGameState.IDLE
    }
}
