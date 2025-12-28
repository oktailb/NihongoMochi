package org.nihongo.mochi.domain.words

import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.util.TextUtils

class WordListEngine(private val wordRepository: WordRepository) {

    private var allWords: List<WordEntry> = emptyList()
    private var displayedWords: List<WordEntry> = emptyList()

    // Filters state
    var filterKanjiOnly = false
    var filterSimpleWords = false
    var filterCompoundWords = false
    var filterIgnoreKnown = false
    var filterWordType = "Tous"

    fun loadList(listName: String) {
        if (listName == "user_custom_list") {
            val scores = ScoreManager.getAllScores(ScoreManager.ScoreType.READING)
            allWords = scores.map { (word, _) ->
                WordEntry(text = word, type = "")
            }
        } else {
            allWords = wordRepository.getWordEntriesForLevel(listName)
        }
        applyFilters()
    }

    fun applyFilters(): List<WordEntry> {
        displayedWords = allWords.filter { word ->
            val kanjiCount = TextUtils.kanjiCount(word.text)
            val hasKana = TextUtils.containsKana(word.text)

            val typeFilter = when (filterWordType) {
                "Tous" -> true
                else -> word.type == filterWordType
            }

            val structureFilter = when {
                filterKanjiOnly && kanjiCount == 1 && !hasKana -> true
                filterSimpleWords && kanjiCount == 1 && hasKana -> true
                filterCompoundWords && kanjiCount > 1 -> true
                !filterKanjiOnly && !filterSimpleWords && !filterCompoundWords -> true
                else -> false
            }

            val knownFilter = if (filterIgnoreKnown) {
                val score = ScoreManager.getScore(word.text, ScoreManager.ScoreType.READING)
                (score.successes - score.failures) < 10
            } else {
                true
            }

            typeFilter && structureFilter && knownFilter
        }
        return displayedWords
    }
    
    fun getDisplayedWords(): List<WordEntry> = displayedWords
}
