package org.nihongo.mochi.domain.words

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.nihongo.mochi.domain.kana.ResourceLoader

@Serializable
data class WordEntry(
    @SerialName("@phonetics") val phonetics: String = "",
    @SerialName("#text") val text: String = "",
    @SerialName("@type") val type: String = ""
)

@Serializable
data class WordsList(
    val word: List<WordEntry>
)

@Serializable
data class WordListRoot(
    val words: WordsList
)

class WordRepository(private val resourceLoader: ResourceLoader) {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val cachedWords = mutableMapOf<String, List<WordEntry>>()
    private var allWordsCache: List<WordEntry>? = null

    private val allKnownLists = listOf(
        "bccwj_wordlist_1000", "bccwj_wordlist_2000", "bccwj_wordlist_3000", 
        "bccwj_wordlist_4000", "bccwj_wordlist_5000", "bccwj_wordlist_6000", 
        "bccwj_wordlist_7000", "bccwj_wordlist_8000",
        "jlpt_wordlist_n5", "jlpt_wordlist_n4", "jlpt_wordlist_n3",
        "jlpt_wordlist_n2", "jlpt_wordlist_n1"
    )

    fun getWordsForLevel(fileName: String): List<String> {
        return getWordEntriesForLevel(fileName).map { it.text }
    }

    fun getWordEntriesForLevel(fileName: String): List<WordEntry> {
        if (cachedWords.containsKey(fileName)) {
            return cachedWords[fileName]!!
        }
        
        val jsonString = resourceLoader.loadJson("words/$fileName.json")
        return try {
            val root = json.decodeFromString<WordListRoot>(jsonString)
            cachedWords[fileName] = root.words.word
            root.words.word
        } catch (e: Exception) {
            println("Error parsing word list $fileName: ${e.message}")
            emptyList()
        }
    }

    fun getAllWordEntries(): List<WordEntry> {
        if (allWordsCache != null) {
            return allWordsCache!!
        }
        val allEntries = mutableListOf<WordEntry>()
        for (listName in allKnownLists) {
            allEntries.addAll(getWordEntriesForLevel(listName))
        }
        allWordsCache = allEntries
        return allEntries
    }

    fun getWordsContainingKanji(kanji: String): List<WordEntry> {
        return getAllWordEntries().filter { it.text.contains(kanji) }
    }
}
