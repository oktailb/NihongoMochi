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
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaType
import org.nihongo.mochi.domain.kana.KanaUtils
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.ViewModel

// Wrapper to treat both Kanji and Kana as playable items in Simon game
data class SimonPlayable(
    val id: String,
    val character: String,
    val meanings: List<String>,
    val readings: List<String>,
    val type: PlayableType
)

enum class PlayableType { KANJI, HIRAGANA, KATAKANA }

class SimonViewModel(
    private val kanjiRepository: KanjiRepository,
    private val kanaRepository: KanaRepository,
    private val meaningRepository: MeaningRepository,
    private val settingsRepository: SettingsRepository,
    private val levelContentProvider: LevelContentProvider,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val REVISION_LEVEL_ID = "user_custom_list"
    private val _gameState = MutableStateFlow(SimonGameState.IDLE)
    val gameState: StateFlow<SimonGameState> = _gameState.asStateFlow()

    private val _selectedMode = MutableStateFlow(SimonMode.KANJI)
    val selectedMode: StateFlow<SimonMode> = _selectedMode.asStateFlow()

    private val _isKanaLevel = MutableStateFlow(false)
    val isKanaLevel: StateFlow<Boolean> = _isKanaLevel.asStateFlow()

    private val _targetSequence = MutableStateFlow<List<SimonPlayable>>(emptyList())
    val targetSequence: StateFlow<List<SimonPlayable>> = _targetSequence.asStateFlow()

    private val _currentPlayable = MutableStateFlow<SimonPlayable?>(null)
    val currentPlayable: StateFlow<SimonPlayable?> = _currentPlayable.asStateFlow()

    private val _answers = MutableStateFlow<List<Pair<SimonPlayable, String>>>(emptyList())
    val answers: StateFlow<List<Pair<SimonPlayable, String>>> = _answers.asStateFlow()

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
    private var allPlayablesInLevel: List<SimonPlayable> = emptyList()
    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadScoresHistory()
        checkLevelType()
    }

    private fun checkLevelType() {
        val levelId = settingsRepository.getSelectedLevel()
        val isKana = levelId.equals("hiragana", ignoreCase = true) || levelId.equals("katakana", ignoreCase = true)
        _isKanaLevel.value = isKana
        if (isKana) {
            _selectedMode.value = SimonMode.KANA_SAME
        } else {
            _selectedMode.value = SimonMode.KANJI
        }
    }

    private fun loadScoresHistory() {
        try {
            val historyJson = scoreRepository.getSimonHistory()
            if (historyJson.isNotEmpty()) {
                val history = json.decodeFromString<List<SimonGameResult>>(historyJson)
                _scoresHistory.value = history
            }
        } catch (e: Exception) {
            _scoresHistory.value = emptyList()
        }
    }

    fun onModeSelected(mode: SimonMode) {
        _selectedMode.value = mode
    }

    fun startGame() {
        val levelId = settingsRepository.getSelectedLevel()
        
        viewModelScope.launch {
            val locale = settingsRepository.getAppLocale()
            val meaningsMap = meaningRepository.getMeanings(locale)

            allPlayablesInLevel = if (levelId.equals("hiragana", ignoreCase = true) || levelId.equals("katakana", ignoreCase = true)) {
                val type = if (levelId.equals("hiragana", ignoreCase = true)) KanaType.HIRAGANA else KanaType.KATAKANA
                val playableType = if (type == KanaType.HIRAGANA) PlayableType.HIRAGANA else PlayableType.KATAKANA
                kanaRepository.getKanaEntries(type).map { 
                    SimonPlayable(it.character, it.character, listOf(it.romaji), listOf(it.romaji), playableType)
                }
            } else if (levelId == REVISION_LEVEL_ID) {
                // Fetch user revisions (kanji only for now in mini-games)
                val kanjiChars = levelContentProvider.getCharactersForLevel(levelId)
                kanjiChars.mapNotNull { idOrChar ->
                    val kanji = kanjiRepository.getKanjiById(idOrChar) ?: kanjiRepository.getKanjiByCharacter(idOrChar)
                    kanji?.let { k ->
                        SimonPlayable(
                            id = k.id,
                            character = k.character,
                            meanings = meaningsMap[k.id] ?: emptyList(),
                            readings = k.readings?.reading?.map { it.value } ?: emptyList(),
                            type = PlayableType.KANJI
                        )
                    }
                }
            } else {
                val kanjis = kanjiRepository.getKanjiByLevel(levelId)
                kanjis.map { kanji ->
                    SimonPlayable(
                        id = kanji.id,
                        character = kanji.character,
                        meanings = meaningsMap[kanji.id] ?: emptyList(),
                        readings = kanji.readings?.reading?.map { it.value } ?: emptyList(),
                        type = PlayableType.KANJI
                    )
                }
            }

            if (allPlayablesInLevel.isEmpty()) return@launch

            _targetSequence.value = emptyList()
            _score.value = 0
            _gameTimeSeconds.value = 0
            _gameState.value = SimonGameState.SHOWING_SEQUENCE
            
            startTimer()
            nextRound()
        }
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
            
            val nextItem = allPlayablesInLevel.shuffled().first()
            val newSequence = _targetSequence.value + nextItem
            _targetSequence.value = newSequence
            
            newSequence.forEachIndexed { index, item ->
                _currentPlayable.value = item
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
        val distractors = allPlayablesInLevel
            .filter { it.id != correctAnswer.id }
            .shuffled()
            .take(3)
        
        val mode = _selectedMode.value
        val allOptions = (distractors + correctAnswer).shuffled()
        
        _answers.value = allOptions.map { item ->
            item to getLabelForPlayable(item, mode)
        }
    }

    private fun getLabelForPlayable(item: SimonPlayable, mode: SimonMode): String {
        return when (mode) {
            SimonMode.KANJI -> item.character
            SimonMode.MEANING -> item.meanings.firstOrNull() ?: item.character
            SimonMode.READING_COMMON -> item.readings.firstOrNull() ?: item.character
            SimonMode.READING_RANDOM -> item.readings.shuffled().firstOrNull() ?: item.character
            SimonMode.KANA_SAME -> item.character
            SimonMode.KANA_CROSS -> {
                if (item.type == PlayableType.HIRAGANA) KanaUtils.hiraganaToKatakana(item.character)
                else if (item.type == PlayableType.KATAKANA) KanaUtils.katakanaToHiragana(item.character)
                else item.character
            }
        }
    }

    fun onAnswerClick(item: SimonPlayable) {
        if (_gameState.value != SimonGameState.AWAITING_INPUT) return

        if (item.id == _targetSequence.value[inputIndex].id) {
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
            scoreRepository.saveSimonHistory(json.encodeToString(newHistory))
        } catch (e: Exception) {
        }
    }
    
    fun abandonGame() {
        if (_gameState.value == SimonGameState.SHOWING_SEQUENCE || _gameState.value == SimonGameState.AWAITING_INPUT) {
            saveFinalScore()
        }
        timerJob?.cancel()
        _gameState.value = SimonGameState.IDLE
        checkLevelType()
    }

    fun resetToIdle() {
        timerJob?.cancel()
        _gameState.value = SimonGameState.IDLE
        checkLevelType()
    }
}
