package org.nihongo.mochi.domain.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.recognition.HandwritingRecognizer
import org.nihongo.mochi.domain.recognition.ModelStatus
import org.nihongo.mochi.domain.recognition.RecognitionStroke
import org.nihongo.mochi.domain.settings.SettingsRepository

enum class SearchMode { READING, MEANING }

data class DictionaryItem(
    val id: String,
    val character: String,
    val readings: List<ReadingInfo>,
    val strokeCount: Int,
    val meanings: MutableList<String>,
    val jlptLevel: String?,
    val schoolGrade: String?
)

data class ReadingInfo(val text: String, val type: String)

class DictionaryViewModel(
    private val handwritingRecognizer: HandwritingRecognizer,
    private val kanjiRepository: KanjiRepository,
    private val meaningRepository: MeaningRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // --- Dictionary Data ---
    var isDataLoaded = false
    val allKanjiList = mutableListOf<DictionaryItem>()
    val kanjiDataMap = mutableMapOf<String, DictionaryItem>()
    
    private val _lastResults = MutableStateFlow<List<DictionaryItem>>(emptyList())
    val lastResults: StateFlow<List<DictionaryItem>> = _lastResults.asStateFlow()

    // --- Filter State ---
    var searchMode: SearchMode = SearchMode.READING
    var textQuery: String = ""
    var strokeQuery: String = ""
    var exactMatch: Boolean = false
    var selectedLevelCategory: String = "ALL" // "ALL", "N5", "N4", "G1", etc.
    
    val availableCategories = listOf(
        "ALL", "N5", "N4", "N3", "N2", "N1", 
        "Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5", "Grade 6", "Secondary"
    )
    
    private var drawingCandidates: List<String>? = null

    // --- Drawing Recognition State ---
    val modelStatus: StateFlow<ModelStatus> = handwritingRecognizer.modelStatus

    private val _recognitionResults = MutableStateFlow<List<String>?>(null)
    val recognitionResults: StateFlow<List<String>?> = _recognitionResults.asStateFlow()
    
    var lastStrokes: List<RecognitionStroke>? = null

    fun downloadModel() {
        handwritingRecognizer.downloadModel()
    }

    fun isRecognizerInitialized(): Boolean = handwritingRecognizer.isInitialized()

    fun recognizeInk(strokes: List<RecognitionStroke>) {
        if (!isRecognizerInitialized()) return
        lastStrokes = strokes
        
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
        lastStrokes = null
        _recognitionResults.value = null
        applyFilters()
    }
    
    // Logic to load data (ported from old ViewModel)
    fun loadDictionaryData() {
        if (isDataLoaded) return
        
        viewModelScope.launch {
            val locale = settingsRepository.getAppLocale()
            val meanings = meaningRepository.getMeanings(locale)
            val allKanji = kanjiRepository.getAllKanji()
            
            kanjiDataMap.clear()
            allKanjiList.clear()
            
            for (kanjiEntry in allKanji) {
                val readings = kanjiEntry.readings?.reading?.map {
                    ReadingInfo(it.value, it.type)
                } ?: emptyList()
                
                val strokes = kanjiEntry.strokes?.toIntOrNull() ?: 0
                val itemMeanings = meanings[kanjiEntry.id] ?: emptyList()
                
                val item = DictionaryItem(
                    id = kanjiEntry.id,
                    character = kanjiEntry.character,
                    readings = readings,
                    strokeCount = strokes,
                    meanings = itemMeanings.toMutableList(),
                    jlptLevel = kanjiEntry.jlptLevel,
                    schoolGrade = kanjiEntry.schoolGrade
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

        // 0. Level Category Filter
        if (selectedLevelCategory != "ALL") {
            filteredList = filteredList.filter { item ->
                when {
                    selectedLevelCategory.startsWith("N") -> item.jlptLevel == selectedLevelCategory
                    selectedLevelCategory.startsWith("Grade") -> {
                        val gradeNum = selectedLevelCategory.removePrefix("Grade ").trim()
                        item.schoolGrade == gradeNum
                    }
                    selectedLevelCategory == "Secondary" -> {
                        // Assuming Secondary is grade > 6 or specific code
                        val g = item.schoolGrade?.toIntOrNull()
                        g != null && g > 6
                    }
                    else -> true
                }
            }
        }

        // 1. Drawing filter
        drawingCandidates?.let { candidates ->
            val candidateSet = candidates.toSet()
            filteredList = filteredList.filter { candidateSet.contains(it.character) }
        }

        // 2. Stroke count filter
        val strokeCount = strokeQuery.toIntOrNull()
        if (strokeCount != null) {
            filteredList = filteredList.filter { it.strokeCount == strokeCount }
        }

        // 3. Text filter
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
