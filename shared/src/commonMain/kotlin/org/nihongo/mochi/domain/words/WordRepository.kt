package org.nihongo.mochi.domain.words

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.nihongo.mochi.domain.levels.LevelsRepository

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

class WordRepository(
    private val resourceLoader: ResourceLoader,
    private val levelsRepository: LevelsRepository
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val cachedWords = mutableMapOf<String, List<WordEntry>>()
    private var allWordsCache: List<WordEntry>? = null
    
    private var knownListsLoaded = false
    private val _knownLists = mutableListOf<String>()

    private suspend fun ensureKnownListsLoaded() {
        if (knownListsLoaded) return
        try {
            val files = levelsRepository.getAllDataFilesForActivity("READING")
            
            _knownLists.clear()
            _knownLists.addAll(files)
            knownListsLoaded = true
            
        } catch (e: Exception) {
             if (_knownLists.isEmpty()) {
                _knownLists.addAll(listOf(
                    "bccwj_wordlist_1000", "bccwj_wordlist_2000", "bccwj_wordlist_3000", 
                    "bccwj_wordlist_4000", "bccwj_wordlist_5000", "bccwj_wordlist_6000", 
                    "bccwj_wordlist_7000", "bccwj_wordlist_8000",
                    "jlpt_wordlist_n5", "jlpt_wordlist_n4", "jlpt_wordlist_n3",
                    "jlpt_wordlist_n2", "jlpt_wordlist_n1"
                ))
            }
            e.printStackTrace()
        }
    }
    
    fun getWordsForLevel(fileName: String): List<String> {
        return getWordEntriesForLevel(fileName).map { it.text }
    }

    fun getWordEntriesForLevel(fileName: String): List<WordEntry> {
        return runBlocking {
             getWordEntriesForLevelSuspend(fileName)
        }
    }

    suspend fun getWordEntriesForLevelSuspend(fileName: String): List<WordEntry> {
        if (cachedWords.containsKey(fileName)) {
            return cachedWords[fileName]!!
        }
        
        return try {
            val jsonString = resourceLoader.loadJson("words/$fileName.json")
            val root = json.decodeFromString<WordListRoot>(jsonString)
            cachedWords[fileName] = root.words.word
            root.words.word
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllWordEntries(): List<WordEntry> {
        return runBlocking {
            getAllWordEntriesSuspend()
        }
    }

    suspend fun getAllWordEntriesSuspend(): List<WordEntry> {
        if (allWordsCache != null) {
            return allWordsCache!!
        }
        
        ensureKnownListsLoaded()
        
        val allEntries = mutableListOf<WordEntry>()
        for (listName in _knownLists) {
            val entries = getWordEntriesForLevelSuspend(listName)
            allEntries.addAll(entries)
        }
        allWordsCache = allEntries
        return allEntries
    }

    fun getWordsContainingKanji(kanji: String): List<WordEntry> {
        return getAllWordEntries().filter { it.text.contains(kanji) }
    }

    fun getWordEntriesByText(texts: List<String>): List<WordEntry> {
        val all = getAllWordEntries()
        val textSet = texts.toSet()
        // We might have duplicates if a word is in multiple levels, we take the first found
        return all.filter { it.text in textSet }.distinctBy { it.text }
    }
}
