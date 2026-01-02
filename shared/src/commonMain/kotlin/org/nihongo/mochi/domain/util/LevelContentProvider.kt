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
        return when {
            levelKey == "Hiragana" -> kanaRepository.getKanaEntries(KanaType.HIRAGANA).map { it.character }
            levelKey == "Katakana" -> kanaRepository.getKanaEntries(KanaType.KATAKANA).map { it.character }
            // For older JSON configs or hardcoded values
            levelKey == "Native Challenge" -> kanjiRepository.getNativeKanji().map { it.character }
            levelKey == "No Reading" -> kanjiRepository.getNoReadingKanji().map { it.character }
            levelKey == "No Meaning" -> kanjiRepository.getNoMeaningKanji().map { it.character }
            levelKey == "user_custom_list" -> ScoreManager.getAllScores(ScoreManager.ScoreType.READING).keys.toList()
            // Map legacy or specific keys if needed
            levelKey.startsWith("bccwj_wordlist_") || levelKey.startsWith("jlpt_wordlist_") || levelKey.startsWith("reading_") -> {
                 val cleanKey = if (levelKey.startsWith("reading_n")) "jlpt_wordlist_${levelKey.removePrefix("reading_")}" else levelKey
                 wordRepository.getWordsForLevel(cleanKey)
            }
            // Standard Kanji data files from levels.json usually map to "kanji_details" 
            // BUT we need to filter by the level ID (e.g. N5, Grade1).
            // Since the new JSON system uses "kanji_details" as the data file for MANY levels,
            // we rely on the caller passing the specific logic or we deduce it here.
            // HOWEVER, the `StatisticsEngine` passes `config.dataFile` which is "kanji_details".
            // If we just get "kanji_details", we don't know which subset.
            // ACTUALLY, in StatisticsEngine.kt, it calls `calculatePercentage(config.dataFile, type)`.
            // If dataFile is "kanji_details", it asks for ALL kanji? That's wrong.
            // FIX: The StatisticsEngine should probably pass the level ID or we need a way to resolve the subset.
            // 
            // In the current LevelContentProvider logic:
            // It tries to parse "N5", "Grade 1" etc.
            // But the XML/JSON now says `dataFile: "kanji_details"`.
            // The `WritingRecapViewModel` calls `getCharactersForLevel(levelKey)` where levelKey comes from navigation args.
            // If navigation args pass "level_n5" (the ID) or "N5" (the old key), we are good.
            // Let's look at `levels.json`: id is "n5", name is "level_n5".
            // WritingFragment sends `levelKey` from `WritingLevelInfoState`.
            // WritingViewModel sets `levelKey = level.xmlName` (which is `dataFile`!).
            // So if `dataFile` is "kanji_details" for ALL levels, then `levelKey` is always "kanji_details".
            // This is the bug.
            
            else -> {
                val (type, value) = when {
                    levelKey.equals("n5", ignoreCase = true) || levelKey.equals("level_n5", ignoreCase = true) -> "jlpt" to "N5"
                    levelKey.equals("n4", ignoreCase = true) || levelKey.equals("level_n4", ignoreCase = true) -> "jlpt" to "N4"
                    levelKey.equals("n3", ignoreCase = true) || levelKey.equals("level_n3", ignoreCase = true) -> "jlpt" to "N3"
                    levelKey.equals("n2", ignoreCase = true) || levelKey.equals("level_n2", ignoreCase = true) -> "jlpt" to "N2"
                    levelKey.equals("n1", ignoreCase = true) || levelKey.equals("level_n1", ignoreCase = true) -> "jlpt" to "N1"
                    
                    levelKey.startsWith("grade", ignoreCase = true) -> {
                         // Extract number
                         val num = levelKey.filter { it.isDigit() }
                         if (num.isNotEmpty()) "grade" to num else "" to ""
                    }
                     levelKey.startsWith("level_grade_", ignoreCase = true) -> {
                         val num = levelKey.filter { it.isDigit() }
                         if (num.isNotEmpty()) "grade" to num else "" to ""
                    }
                    levelKey.startsWith("level_college_", ignoreCase = true) -> {
                         val num = levelKey.filter { it.isDigit() }
                         if (num.isNotEmpty()) "grade" to (num.toInt() + 6).toString() else "" to ""
                    }
                     levelKey.startsWith("level_lycee_", ignoreCase = true) -> {
                         val num = levelKey.filter { it.isDigit() }
                         if (num.isNotEmpty()) "grade" to (num.toInt() + 8).toString() else "" to ""
                    }
                    
                    else -> "" to ""
                }
                
                if(type.isNotEmpty()) {
                    kanjiRepository.getKanjiByLevel(type, value).map { it.character }
                } else {
                    // Fallback for direct matches like "kanji_details" -> return everything? No that's too much.
                    // Or maybe it is an old specific file like "kanji_n5"
                     emptyList()
                }
            }
        }
    }
}
