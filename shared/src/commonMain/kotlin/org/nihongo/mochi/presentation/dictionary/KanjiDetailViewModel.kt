package org.nihongo.mochi.presentation.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.words.WordRepository

class KanjiDetailViewModel(
    private val kanjiRepository: KanjiRepository,
    private val meaningRepository: MeaningRepository,
    private val wordRepository: WordRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    data class KanjiDetailUiState(
        val isLoading: Boolean = false,
        val kanjiCharacter: String? = null,
        val kanjiStrokes: Int = 0,
        val jlptLevel: String? = null,
        val schoolGrade: String? = null,
        val kanjiStructure: String? = null,
        val kanjiMeanings: List<String> = emptyList(),
        val onReadings: List<ReadingItem> = emptyList(),
        val kunReadings: List<ReadingItem> = emptyList(),
        val components: List<ComponentItem> = emptyList(),
        val examples: List<ExampleItem> = emptyList(),
        val componentTree: ComponentNode? = null
    )

    data class ReadingItem(val type: String, val reading: String, val frequency: Int)
    data class ExampleItem(val word: String, val reading: String)
    data class ComponentItem(val character: String, val kanjiRef: String?)

    // Recursive structure for the tree
    data class ComponentNode(
        val id: String?,
        val character: String,
        val onReadings: List<String> = emptyList(),
        val children: List<ComponentNode> = emptyList()
    )

    private val _uiState = MutableStateFlow(KanjiDetailUiState())
    val uiState: StateFlow<KanjiDetailUiState> = _uiState.asStateFlow()

    fun loadKanji(kanjiId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val entry = kanjiRepository.getKanjiById(kanjiId)
            
            if (entry != null) {
                // Parse readings
                val allReadings = entry.readings?.reading?.map { r ->
                    ReadingItem(
                        type = r.type,
                        reading = r.value,
                        frequency = r.frequency?.toIntOrNull() ?: 0
                    )
                } ?: emptyList()

                val onReadings = allReadings.filter { it.type == "on" }
                val kunReadings = allReadings.filter { it.type == "kun" }

                // Parse components
                val components = entry.components?.component?.map { c ->
                    ComponentItem(
                        character = c.text ?: c.kanjiRef ?: "",
                        kanjiRef = c.kanjiRef
                    )
                } ?: emptyList()

                // Build tree recursively
                val treeRoot = buildComponentTree(entry.character, entry.id, 0)

                // Load meanings
                val locale = settingsRepository.getAppLocale()
                val meanings = meaningRepository.getMeanings(locale)[kanjiId] ?: emptyList()

                // Load examples
                val examples = wordRepository.getWordsContainingKanji(entry.character)
                    .map { ExampleItem(it.text, it.phonetics) }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        kanjiCharacter = entry.character,
                        kanjiStrokes = entry.strokes?.toIntOrNull() ?: 0,
                        jlptLevel = entry.jlptLevel,
                        schoolGrade = entry.schoolGrade,
                        kanjiStructure = entry.components?.structure,
                        kanjiMeanings = meanings,
                        onReadings = onReadings,
                        kunReadings = kunReadings,
                        components = components,
                        examples = examples,
                        componentTree = treeRoot
                    )
                }
            } else {
                 _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun buildComponentTree(character: String, kanjiId: String?, depth: Int): ComponentNode {
        // Limit depth to avoid infinite recursion if any circular ref exists (though rare in Kanji)
        if (depth > 5) return ComponentNode(kanjiId, character)
        
        val children = mutableListOf<ComponentNode>()
        
        // If we have an ID, we can look up its components
        // If not, we try to find the ID by character
        val entry = if (kanjiId != null) {
            kanjiRepository.getKanjiById(kanjiId)
        } else {
            kanjiRepository.getKanjiByCharacter(character)
        }
        
        val onReadings = entry?.readings?.reading
            ?.filter { it.type == "on" }
            ?.map { it.value } 
            ?: emptyList()

        if (entry != null) {
             entry.components?.component?.forEach { comp ->
                 val char = comp.text ?: comp.kanjiRef ?: ""
                 val refId = comp.kanjiRef?.let { ref -> 
                     // Optimization: If ref is the character itself, it's just the char. 
                     // Usually ref is the character string.
                     // We need the ID to look deeper.
                     kanjiRepository.getKanjiByCharacter(ref)?.id
                 }
                 
                 // Avoid self-reference loop immediately
                 if (char != character) {
                     children.add(buildComponentTree(char, refId, depth + 1))
                 }
             }
        }
        
        return ComponentNode(
            id = entry?.id,
            character = character,
            onReadings = onReadings,
            children = children
        )
    }
    
    // Helper needed for navigation logic from UI
    fun findKanjiIdByCharacter(character: String): String? {
        val entry = kanjiRepository.getKanjiByCharacter(character)
        return entry?.id
    }
}
