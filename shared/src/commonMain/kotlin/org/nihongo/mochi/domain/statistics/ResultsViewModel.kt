package org.nihongo.mochi.domain.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.services.CloudSaveService

class ResultsViewModel(
    private val cloudSaveService: CloudSaveService,
    private val statisticsEngine: StatisticsEngine
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    
    // Saga Map State now uses Steps instead of just Nodes
    private val _sagaSteps = MutableStateFlow<List<SagaStep>>(emptyList())
    val sagaSteps: StateFlow<List<SagaStep>> = _sagaSteps.asStateFlow()
    
    private val _currentTab = MutableStateFlow(SagaTab.JLPT)
    val currentTab: StateFlow<SagaTab> = _currentTab.asStateFlow()

    private var currentSaveName = "NihongoMochiSnapshot"

    init {
        checkSignInStatus()
        refreshSagaMap()
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

    fun signIn() {
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
            val data = ScoreManager.getAllDataJson()
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
        ScoreManager.restoreDataFromJson(data)
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
