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
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.dictionary.DictionaryItem
import org.nihongo.mochi.domain.dictionary.ReadingInfo
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.meaning.WordMeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.domain.services.TextToSpeech
import org.nihongo.mochi.domain.services.VoiceConfig
import org.nihongo.mochi.domain.services.VoiceGender

class WordDetailViewModel(
    private val wordRepository: WordRepository,
    private val kanjiRepository: KanjiRepository,
    private val meaningRepository: MeaningRepository,
    private val wordMeaningRepository: WordMeaningRepository,
    private val settingsRepository: SettingsRepository,
    private val scoreRepository: ScoreRepository,
    private val tts: TextToSpeech
) : ViewModel() {

    data class WordDetailUiState(
        val isLoading: Boolean = false,
        val wordId: String = "",
        val wordText: String = "",
        val phonetics: String = "",
        val meaning: String? = null,
        val kanjiComponents: List<DictionaryItem> = emptyList(),
        val currentLocaleCode: String = "",
        val ttsGender: VoiceGender = VoiceGender.FEMALE,
        val ttsRate: Float = 1.0f,
        val selectedVoiceId: String? = null,
        val isInRevisionList: Boolean = false
    )

    private val _uiState = MutableStateFlow(WordDetailUiState())
    val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    fun loadWord(wordText: String) {
        _uiState.update { it.copy(isLoading = true, wordText = wordText) }
        viewModelScope.launch(Dispatchers.IO) {
            val locale = settingsRepository.getAppLocale()
            val wordEntry = wordRepository.getWordEntriesByText(listOf(wordText)).firstOrNull()
            val wordId = wordEntry?.id ?: ""
            val wordMeaning = wordMeaningRepository.getWordMeaningsSuspend(locale)[wordId]
            
            // Fetch all Kanji meanings for the current locale
            val kanjiMeaningsMap = meaningRepository.getMeanings(locale)

            // Extract Kanji characters from the word (CJK Unified Ideographs block)
            val kanjiChars = wordText.filter { char ->
                char.code in 0x4E00..0x9FAF
            }.map { it.toString() }.distinct()

            // Fetch details for each Kanji
            val kanjiItems = kanjiChars.mapNotNull { char ->
                kanjiRepository.getKanjiByCharacter(char)?.let { entry ->
                    val meanings = kanjiMeaningsMap[entry.id] ?: emptyList()
                    
                    DictionaryItem(
                        id = entry.id,
                        character = entry.character,
                        readings = entry.readings?.reading?.map { r ->
                            ReadingInfo(r.value, r.type)
                        } ?: emptyList(),
                        meanings = meanings.toMutableList(), 
                        strokeCount = entry.strokes?.toIntOrNull() ?: 0,
                        displayLabelKeys = listOfNotNull(entry.jlptLevel).filter { it.isNotEmpty() }
                    )
                }
            }

            // Consistency check: check both text and id
            val isInRevision = scoreRepository.isInList(ScoreManager.READING_LIST, wordText) || 
                               (wordId.isNotEmpty() && scoreRepository.isInList(ScoreManager.READING_LIST, wordId))

            _uiState.update {
                it.copy(
                    isLoading = false,
                    wordId = wordId,
                    phonetics = wordEntry?.phonetics ?: "",
                    meaning = wordMeaning,
                    kanjiComponents = kanjiItems,
                    currentLocaleCode = locale,
                    ttsGender = settingsRepository.getTtsGender(locale),
                    ttsRate = settingsRepository.getTtsRate(),
                    selectedVoiceId = settingsRepository.getTtsVoiceId(),
                    isInRevisionList = isInRevision
                )
            }
        }
    }

    fun toggleRevisionList() {
        val wordText = _uiState.value.wordText
        if (wordText.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value.isInRevisionList
            if (current) {
                scoreRepository.removeItemFromList(ScoreManager.READING_LIST, wordText)
                // Also remove by ID just in case
                _uiState.value.wordId.let { id ->
                    if (id.isNotEmpty()) scoreRepository.removeItemFromList(ScoreManager.READING_LIST, id)
                }
            } else {
                scoreRepository.addItemToList(ScoreManager.READING_LIST, wordText)
            }
            _uiState.update { it.copy(isInRevisionList = !current) }
        }
    }

    fun speak() {
        val config = VoiceConfig(
            rate = _uiState.value.ttsRate,
            gender = _uiState.value.ttsGender,
            voiceId = _uiState.value.selectedVoiceId
        )
        
        val textToSpeak = _uiState.value.phonetics.ifEmpty { _uiState.value.wordText }
        if (textToSpeak.isNotEmpty()) {
            tts.speak(textToSpeak, "ja", config)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts.stop()
    }
}
