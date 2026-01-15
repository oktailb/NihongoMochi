package org.nihongo.mochi.domain.words

import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository

class WordListEngine(
    private val wordRepository: WordRepository,
    private val scoreRepository: ScoreRepository
) {
    var isGameInitialized = false
    
    private var allWords = listOf<WordEntry>()
    private var filteredWords = listOf<WordEntry>()

    // Filters
    var filterKanjiOnly = false
    var filterSimpleWords = false
    var filterCompoundWords = false
    var filterIgnoreKnown = false
    var filterWordType: String = "Tous"

    fun loadList(listName: String) {
        allWords = wordRepository.getWordEntriesForLevel(listName)
        applyFilters()
    }

    fun setWords(words: List<WordEntry>) {
        allWords = words
        applyFilters()
    }

    fun applyFilters() {
        filteredWords = allWords.filter { word ->
            var include = true
            
            if (filterKanjiOnly) {
                include = include && word.text.any { it.isKanji() }
            }
            
            if (filterSimpleWords) {
                include = include && word.text.length == 1
            }
            
            if (filterCompoundWords) {
                include = include && word.text.length > 1
            }
            
            if (filterIgnoreKnown) {
                val score = scoreRepository.getScore(word.text, ScoreManager.ScoreType.READING)
                include = include && (score.successes - score.failures) < 10
            }
            
            if (filterWordType != "Tous") {
                include = include && word.type == filterWordType
            }
            
            include
        }
    }

    fun getDisplayedWords(): List<WordEntry> = filteredWords

    fun calculateMasteryPercentage(levelId: String): Double {
        val wordEntries = wordRepository.getWordEntriesForLevel(levelId)
        if (wordEntries.isEmpty()) return 0.0

        val totalMasteryPoints = wordEntries.sumOf { wordEntry ->
            val score = scoreRepository.getScore(wordEntry.text, ScoreManager.ScoreType.READING)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = wordEntries.size * 10.0
        return (totalMasteryPoints / maxPossiblePoints) * 100.0
    }

    fun getMasteryPoints(wordText: String): Int {
        val score = scoreRepository.getScore(wordText, ScoreManager.ScoreType.READING)
        return (score.successes - score.failures).coerceAtLeast(0)
    }

    private fun Char.isKanji(): Boolean {
        return this.code in 0x4E00..0x9FAF
    }
}
