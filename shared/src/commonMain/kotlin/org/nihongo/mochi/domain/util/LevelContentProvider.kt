package org.nihongo.mochi.domain.util

import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaType
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.words.WordRepository

class LevelContentProvider(
    private val kanaRepository: KanaRepository,
    private val kanjiRepository: KanjiRepository,
    private val wordRepository: WordRepository
) {

    fun getCharactersForLevel(levelKey: String): List<String> {
        val lowerKey = levelKey.lowercase()
        return when {
            // Static / Special mappings
            lowerKey == "hiragana" -> kanaRepository.getKanaEntries(KanaType.HIRAGANA).map { it.character }
            lowerKey == "katakana" -> kanaRepository.getKanaEntries(KanaType.KATAKANA).map { it.character }
            lowerKey == "native_challenge" || lowerKey == "native challenge" -> kanjiRepository.getNativeKanji().map { it.character }
            lowerKey == "no_reading" || lowerKey == "no reading" -> kanjiRepository.getNoReadingKanji().map { it.character }
            lowerKey == "no_meaning" || lowerKey == "no meaning" -> kanjiRepository.getNoMeaningKanji().map { it.character }
            lowerKey == "user_custom_list" -> {
                // Return all items with a score in RECOGNITION (Kanji) or GRAMMAR
                // This covers revisions for kanji and grammar rules
                val kanjiScores = ScoreManager.getAllScores(ScoreManager.ScoreType.RECOGNITION).keys
                val grammarScores = ScoreManager.getAllScores(ScoreManager.ScoreType.GRAMMAR).keys
                (kanjiScores + grammarScores).toList()
            }
            
            // Word lists (explicit filenames from levels.json)
            lowerKey.contains("wordlist") -> wordRepository.getWordsForLevel(levelKey)
            
            // Default: Assume it's a level ID (n5, grade1, etc.) and ask KanjiRepository directly
            else -> kanjiRepository.getKanjiByLevel(levelKey).map { it.character }
        }
    }
}
