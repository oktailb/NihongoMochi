package org.nihongo.mochi.domain.util

import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaType
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.words.WordRepository

class LevelContentProvider(
    private val kanaRepository: KanaRepository,
    private val kanjiRepository: KanjiRepository,
    private val wordRepository: WordRepository,
    private val scoreRepository: ScoreRepository
) {

    private val kanaPoolCache = mutableMapOf<String, List<String>>()

    fun getCharactersForLevel(levelKey: String, scoreType: ScoreManager.ScoreType = ScoreManager.ScoreType.RECOGNITION): List<String> {
        val lowerKey = levelKey.lowercase()
        return when {
            // Static / Special mappings
            lowerKey == "hiragana" -> kanaRepository.getKanaEntries(KanaType.HIRAGANA).map { it.character }
            lowerKey == "katakana" -> kanaRepository.getKanaEntries(KanaType.KATAKANA).map { it.character }
            lowerKey == "native_challenge" || lowerKey == "native challenge" -> kanjiRepository.getNativeKanji().map { it.character }
            lowerKey == "no_reading" || lowerKey == "no reading" -> kanjiRepository.getNoReadingKanji().map { it.character }
            lowerKey == "no_meaning" || lowerKey == "no meaning" -> kanjiRepository.getNoMeaningKanji().map { it.character }
            lowerKey == "user_custom_list" -> {
                val listName = when(scoreType) {
                    ScoreManager.ScoreType.RECOGNITION -> ScoreManager.RECOGNITION_LIST
                    ScoreManager.ScoreType.READING -> ScoreManager.READING_LIST
                    ScoreManager.ScoreType.WRITING -> ScoreManager.WRITING_LIST
                    ScoreManager.ScoreType.GRAMMAR -> ScoreManager.RECOGNITION_LIST
                }
                scoreRepository.getListItems(listName)
            }
            
            // Word lists
            lowerKey.contains("wordlist") -> wordRepository.getWordsForLevel(levelKey)
            
            // Default
            else -> kanjiRepository.getKanjiByLevel(levelKey).map { it.character }
        }
    }

    /**
     * Optimized: returns a unique pool of kana used in the words of a specific level.
     * Cached to speed up game initialization (Kana Drop, Simon, etc.)
     */
    suspend fun getKanaPoolForLevel(levelKey: String): List<String> {
        kanaPoolCache[levelKey]?.let { return it }

        val wordEntries = try {
            wordRepository.getWordEntriesForLevelSuspend(levelKey)
        } catch (e: Exception) {
            emptyList()
        }
        
        val pool = wordEntries.flatMap { word ->
            word.phonetics.map { it.toString() }
        }.distinct().filter { 
            it.isNotBlank() && it.first() !in " ./()[]+-*=_\"'!?#%&"
        }

        val finalPool = pool.ifEmpty { 
            // Fallback to basic hiragana if no pool found
            kanaRepository.getKanaEntries(KanaType.HIRAGANA).map { it.character } 
        }
        
        kanaPoolCache[levelKey] = finalPool
        return finalPool
    }
}
