package org.nihongo.mochi.domain.dictionary

import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.recognition.HandwritingRecognizer
import org.nihongo.mochi.domain.recognition.ModelStatus
import org.nihongo.mochi.domain.recognition.RecognitionStroke
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.presentation.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SearchMode { READING, MEANING }

data class DictionaryItem(
    val id: String,
    val character: String,
    val readings: List<ReadingInfo>,
    val strokeCount: Int,
    val meanings: MutableList<String>,
    val levelIds: List<String> = emptyList(),
    val displayLabelKeys: List<String> = emptyList()
)

data class ReadingInfo(val text: String, val type: String)

data class LevelFilterOption(
    val id: String,
    val labelKey: String
)

class DictionaryViewModel(
    private val handwritingRecognizer: HandwritingRecognizer,
    private val kanjiRepository: KanjiRepository,
    private val meaningRepository: MeaningRepository,
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository
) : ViewModel() {

    var isDataLoaded = false
    val allKanjiList = mutableListOf<DictionaryItem>()
    val kanjiDataMap = mutableMapOf<String, DictionaryItem>()
    
    private val _lastResults = MutableStateFlow<List<DictionaryItem>>(emptyList())
    val lastResults: StateFlow<List<DictionaryItem>> = _lastResults.asStateFlow()

    var searchMode: SearchMode = SearchMode.READING
    var textQuery: String = ""
    var strokeQuery: String = ""
    var exactMatch: Boolean = false
    var selectedLevelId: String = "ALL"
    
    private val _availableLevelOptions = MutableStateFlow<List<LevelFilterOption>>(listOf(LevelFilterOption("ALL", "word_type_all")))
    val availableLevelOptions: StateFlow<List<LevelFilterOption>> = _availableLevelOptions.asStateFlow()
    
    private var drawingCandidates: List<String>? = null

    val modelStatus: StateFlow<ModelStatus> = handwritingRecognizer.modelStatus

    private val _recognitionResults = MutableStateFlow<List<String>?>(null)
    val recognitionResults: StateFlow<List<String>?> = _recognitionResults.asStateFlow()
    
    private val _lastStrokes = MutableStateFlow<List<RecognitionStroke>?>(null)
    val lastStrokes: StateFlow<List<RecognitionStroke>?> = _lastStrokes.asStateFlow()

    init {
        viewModelScope.launch {
            val defs = levelsRepository.loadLevelDefinitions()
            val options = mutableListOf(LevelFilterOption("ALL", "word_type_all"))
            
            defs.sections.values
                .flatMap { it.levels }
                .filter { level -> level.activities.any { it.value.dataFile == "kanji_details" } }
                .sortedBy { it.globalStep }
                .forEach { level ->
                    options.add(LevelFilterOption(level.id, level.name))
                }
            
            _availableLevelOptions.value = options
        }
    }

    fun downloadModel() {
        handwritingRecognizer.downloadModel()
    }

    fun isRecognizerInitialized(): Boolean = handwritingRecognizer.isInitialized()

    fun recognizeInk(strokes: List<RecognitionStroke>) {
        if (!isRecognizerInitialized()) return
        _lastStrokes.value = strokes
        
        handwritingRecognizer.recognize(
            strokes,
            onSuccess = { candidates ->
                drawingCandidates = candidates
                _recognitionResults.value = candidates
                applyFilters()
            },
            onFailure = { e ->
                println("Recognition failed: ${e.message}")
                _recognitionResults.value = null
            }
        )
    }

    fun clearDrawingFilter() {
        drawingCandidates = null
        _lastStrokes.value = null
        _recognitionResults.value = null
        applyFilters()
    }
    
    fun loadDictionaryData() {
        if (isDataLoaded) return
        
        viewModelScope.launch {
            val locale = settingsRepository.getAppLocale()
            val meanings = meaningRepository.getMeanings(locale)
            val allKanji = kanjiRepository.getAllKanji()
            
            val defs = levelsRepository.loadLevelDefinitions()
            val levelIdToKey = defs.sections.values.flatMap { it.levels }.associate { it.id to it.name }
            
            kanjiDataMap.clear()
            allKanjiList.clear()
            
            for (kanjiEntry in allKanji) {
                val readings = kanjiEntry.readings?.reading?.map {
                    ReadingInfo(it.value, it.type)
                } ?: emptyList()
                
                val strokes = kanjiEntry.strokes?.toIntOrNull() ?: 0
                val itemMeanings = meanings[kanjiEntry.id] ?: emptyList()
                val labels = kanjiEntry.level.mapNotNull { id -> levelIdToKey[id] }
                
                val item = DictionaryItem(
                    id = kanjiEntry.id,
                    character = kanjiEntry.character,
                    readings = readings,
                    strokeCount = strokes,
                    meanings = itemMeanings.toMutableList(),
                    levelIds = kanjiEntry.level,
                    displayLabelKeys = labels
                )
                kanjiDataMap[kanjiEntry.id] = item
            }
            allKanjiList.addAll(kanjiDataMap.values.sortedBy { it.id.toIntOrNull() ?: 0 })
            isDataLoaded = true
            applyFilters()
        }
    }

    fun applyFilters() {
        var filteredList = allKanjiList.toList()

        if (selectedLevelId != "ALL") {
            when (selectedLevelId.lowercase()) {
                "native_challenge" -> {
                    filteredList = filteredList.filter { item ->
                        item.levelIds.isEmpty() && item.readings.isNotEmpty()
                    }
                }
                "no_reading" -> {
                    filteredList = filteredList.filter { item ->
                        item.levelIds.isEmpty() && item.readings.isEmpty() && item.meanings.isNotEmpty()
                    }
                }
                "no_meaning" -> {
                    filteredList = filteredList.filter { item ->
                        item.levelIds.isEmpty() && item.meanings.isEmpty()
                    }
                }
                else -> {
                    filteredList = filteredList.filter { item ->
                        item.levelIds.any { it.equals(selectedLevelId, ignoreCase = true) }
                    }
                }
            }
        }

        drawingCandidates?.let { candidates ->
            val candidateSet = candidates.toSet()
            filteredList = filteredList.filter { candidateSet.contains(it.character) }
        }

        val strokeCount = strokeQuery.toIntOrNull()
        if (strokeCount != null) {
            filteredList = filteredList.filter { it.strokeCount == strokeCount }
        }

        val query = textQuery.trim().lowercase()
        if (query.isNotEmpty()) {
            if (searchMode == SearchMode.READING) {
                 val cleanQuery = query.replace(".", "")
                 filteredList = filteredList.filter { item ->
                     item.readings.any { it.text.replace(".", "").lowercase().contains(cleanQuery) }
                 }
            } else { // MEANING
                 filteredList = filteredList.filter { item ->
                     item.meanings.any { it.lowercase().contains(query) }
                 }
            }
        }
        
        _lastResults.value = filteredList
    }
}
