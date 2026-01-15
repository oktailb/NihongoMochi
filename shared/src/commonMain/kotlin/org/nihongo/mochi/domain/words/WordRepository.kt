package org.nihongo.mochi.domain.words

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.levels.ActivityConfig

@Serializable
data class WordEntry(
    val id: String = "",
    val text: String = "",
    val phonetics: String = "",
    val type: String? = null,
    val jlpt: String? = null,
    val rank: String? = null
)

@Serializable
data class WordListRoot(
    val words: List<WordEntry> = emptyList()
)

class WordRepository(
    private val resourceLoader: ResourceLoader,
    private val levelsRepository: LevelsRepository
) {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }
    private var allWordsCache: List<WordEntry>? = null
    
    private val MERGED_FILE = "merged_wordlist"

    suspend fun getAllWordEntriesSuspend(): List<WordEntry> = withContext(Dispatchers.IO) {
        allWordsCache?.let { return@withContext it }
        
        try {
            val jsonString = resourceLoader.loadJson("words/$MERGED_FILE.json")
            if (jsonString.isEmpty()) return@withContext emptyList()
            
            val root = json.decodeFromString<WordListRoot>(jsonString)
            allWordsCache = root.words
            root.words
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getAllWordEntries(): List<WordEntry> = runBlocking {
        getAllWordEntriesSuspend()
    }

    suspend fun getWordsByConfig(config: ActivityConfig): List<WordEntry> {
        val allWords = getAllWordEntriesSuspend()
        return allWords.filter { word ->
            var match = true
            
            // Filtre par niveau JLPT
            if (config.jlpt != null) {
                match = match && word.jlpt == config.jlpt
            }
            
            // Filtre par rang de fréquence (BCCWJ)
            val wordRank = word.rank?.toIntOrNull()
            if (config.minRank != null || config.maxRank != null) {
                if (wordRank == null) {
                    match = false
                } else {
                    if (config.minRank != null) match = match && wordRank >= config.minRank
                    if (config.maxRank != null) match = match && wordRank <= config.maxRank
                }
            }
            
            match
        }
    }

    suspend fun getWordEntriesForLevelSuspend(levelId: String): List<WordEntry> {
        val defs = levelsRepository.loadLevelDefinitions()
        val levelDef = defs.sections.values.flatMap { it.levels }
            .find { it.id == levelId }
        
        val config = levelDef?.activities?.get(org.nihongo.mochi.domain.statistics.StatisticsType.READING)

        if (config != null) {
            return getWordsByConfig(config)
        }

        // Si le niveau est explicitement une liste JLPT ancienne
        if (levelId.startsWith("jlpt_wordlist_n")) {
            val level = levelId.split("_").last().uppercase()
            return getAllWordEntriesSuspend().filter { it.jlpt == level }
        }
        
        // Par défaut, on ne renvoie pas tout pour éviter les ANR
        return emptyList()
    }

    fun getWordEntriesForLevel(levelIdOrFileName: String): List<WordEntry> = runBlocking {
        getWordEntriesForLevelSuspend(levelIdOrFileName)
    }

    fun getWordsForLevel(levelId: String): List<String> {
        return getWordEntriesForLevel(levelId).map { it.text }
    }

    fun getWordsContainingKanji(kanji: String): List<WordEntry> {
        return getAllWordEntries().filter { it.text.contains(kanji) }
    }

    fun getWordEntriesByText(texts: List<String>): List<WordEntry> {
        val all = getAllWordEntries()
        val textSet = texts.toSet()
        return all.filter { it.text in textSet }.distinctBy { it.text }
    }
}
