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
                // Logic for word type filtering if available in metadata
            }
            
            include
        }
    }

    fun getDisplayedWords(): List<WordEntry> = filteredWords

    fun calculateMasteryPercentage(levelKey: String): Double {
        val wordTexts = wordRepository.getWordsForLevel(levelKey)
        if (wordTexts.isEmpty()) return 0.0

        val scores = scoreRepository.getAllScores(ScoreManager.ScoreType.READING)
        if (scores.isEmpty()) return 0.0

        val totalMasteryPoints = wordTexts.sumOf { wordText ->
            val score = scoreRepository.getScore(wordText, ScoreManager.ScoreType.READING)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = wordTexts.size * 10.0
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
