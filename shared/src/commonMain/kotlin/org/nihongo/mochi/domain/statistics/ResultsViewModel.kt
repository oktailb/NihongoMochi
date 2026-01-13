package org.nihongo.mochi.domain.statistics

import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.services.CloudSaveService
import org.nihongo.mochi.presentation.SagaAction
import org.nihongo.mochi.presentation.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed class OneTimeEvent {
    data object ShowAchievements : OneTimeEvent()
    data object ShowSavedGames : OneTimeEvent()
}

class ResultsViewModel(
    private val cloudSaveService: CloudSaveService,
    private val statisticsEngine: StatisticsEngine,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: MutableStateFlow<Boolean> = _isAuthenticated

    private val _message = MutableStateFlow<String?>(null)
    val message: MutableStateFlow<String?> = _message
    
    private val _oneTimeEvent = MutableSharedFlow<OneTimeEvent>()
    val oneTimeEvent = _oneTimeEvent.asSharedFlow()

    private val _sagaSteps = MutableStateFlow<List<SagaStep>>(emptyList())
    val sagaSteps: MutableStateFlow<List<SagaStep>> = _sagaSteps
    
    private val _currentTab = MutableStateFlow(SagaTab.JLPT)
    val currentTab: MutableStateFlow<SagaTab> = _currentTab

    private var currentSaveName = "NihongoMochiSnapshot"

    init {
        checkSignInStatus()
        viewModelScope.launch {
            statisticsEngine.loadLevelDefinitions()
            refreshSagaMap()
        }
    }
    
    fun handleSagaAction(action: SagaAction) {
        viewModelScope.launch {
            when (action) {
                SagaAction.SIGN_IN -> signIn()
                SagaAction.ACHIEVEMENTS -> _oneTimeEvent.emit(OneTimeEvent.ShowAchievements)
                SagaAction.BACKUP -> _oneTimeEvent.emit(OneTimeEvent.ShowSavedGames)
                SagaAction.RESTORE -> _oneTimeEvent.emit(OneTimeEvent.ShowSavedGames)
            }
        }
    }

    fun handleSnapshotResult(snapshotName: String?, isNew: Boolean) {
        if (isNew) {
            val unique = Clock.System.now().toEpochMilliseconds().toString()
            setCurrentSaveName("NihongoMochiSnapshot-$unique")
            saveGame()
        } else if (snapshotName != null) {
            setCurrentSaveName(snapshotName)
            viewModelScope.launch {
                val data = cloudSaveService.loadGame(snapshotName)
                if (data != null) {
                    loadGame(data)
                }
            }
        }
    }
    
    fun setTab(tab: SagaTab) {
        _currentTab.value = tab
        refreshSagaMap()
    }
    
    fun refreshSagaMap() {
        _sagaSteps.value = statisticsEngine.getSagaMapSteps(_currentTab.value)
    }
    
    fun getSagaProgress(node: SagaNode): UserSagaProgress {
        return statisticsEngine.getSagaProgress(node)
    }

    fun checkSignInStatus() {
        viewModelScope.launch {
            _isAuthenticated.value = cloudSaveService.isAuthenticated()
        }
    }

    private fun signIn() {
        viewModelScope.launch {
            val success = cloudSaveService.signIn()
            _isAuthenticated.value = success
            if (!success) {
                _message.value = "Connexion échouée"
            }
        }
    }

    fun saveGame() {
        viewModelScope.launch {
            val data = scoreRepository.getAllDataJson()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val desc = "Backup ${now.date} ${now.hour}:${now.minute}"
            
            val success = cloudSaveService.saveGame(currentSaveName, data, desc)
            if (success) {
                _message.value = "Sauvegarde effectuée"
            } else {
                _message.value = "Erreur de sauvegarde"
            }
        }
    }

    fun loadGame(data: String) {
        scoreRepository.restoreDataFromJson(data)
        refreshSagaMap()
        _message.value = "Données restaurées"
    }
    
    fun setCurrentSaveName(name: String) {
        currentSaveName = name
    }

    fun clearMessage() {
        _message.value = null
    }
}
