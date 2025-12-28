package org.nihongo.mochi.domain.util

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
            levelKey == "Native Challenge" -> kanjiRepository.getNativeKanji().map { it.character }
            levelKey == "No Reading" -> kanjiRepository.getNoReadingKanji().map { it.character }
            levelKey == "No Meaning" -> kanjiRepository.getNoMeaningKanji().map { it.character }
            levelKey.startsWith("bccwj_wordlist_") || levelKey.startsWith("reading_") -> {
                 val cleanKey = if (levelKey.startsWith("reading_n")) "jlpt_wordlist_${levelKey.removePrefix("reading_")}" else levelKey
                 wordRepository.getWordsForLevel(cleanKey)
            }
            else -> {
                val (type, value) = when {
                    levelKey.startsWith("N") -> "jlpt" to levelKey
                    levelKey.startsWith("Grade") -> "grade" to levelKey.removePrefix("Grade").trim()
                    else -> "" to ""
                }
                if(type.isNotEmpty()) kanjiRepository.getKanjiByLevel(type, value).map { it.character } else emptyList()
            }
        }
    }
}
