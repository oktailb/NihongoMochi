package org.nihongo.mochi.domain.words

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

    suspend fun getAllWordEntriesSuspend(): List<WordEntry> {
        if (allWordsCache != null) {
            return allWordsCache!!
        }
        
        return try {
            val jsonString = resourceLoader.loadJson("words/$MERGED_FILE.json")
            val root = json.decodeFromString<WordListRoot>(jsonString)
            allWordsCache = root.words
            root.words
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getAllWordEntries(): List<WordEntry> {
        return runBlocking {
            getAllWordEntriesSuspend()
        }
    }

    /**
     * Filtre les mots en fonction de la configuration de l'activité.
     * Supporte le niveau JLPT et les plages de fréquence (rank).
     */
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

    /**
     * Charge les entrées de mots pour un niveau donné.
     * Tente d'utiliser la configuration du niveau si possible.
     */
    suspend fun getWordEntriesForLevelSuspend(levelId: String): List<WordEntry> {
        // Tente d'abord de trouver la config dans levels.json pour cet ID de niveau
        val defs = levelsRepository.loadLevelDefinitions()
        val config = defs.sections.values.flatMap { it.levels }
            .firstOrNull { it.id == levelId }
            ?.activities?.get(org.nihongo.mochi.domain.statistics.StatisticsType.READING)

        if (config != null) {
            return getWordsByConfig(config)
        }

        // Fallback sur l'ancien comportement si on passe un nom de fichier brut ou un niveau non configuré
        val all = getAllWordEntriesSuspend()
        if (levelId.startsWith("jlpt_wordlist_n")) {
            val level = levelId.split("_").last().uppercase()
            return all.filter { it.jlpt == level }
        }
        
        if (levelId.startsWith("bccwj_wordlist_")) {
            val max = levelId.split("_").last().toIntOrNull() ?: 999999
            val min = max - 999
            return all.filter { 
                val r = it.rank?.toIntOrNull()
                r != null && r in min..max 
            }
        }
        
        return all
    }

    /**
     * Méthode de compatibilité pour l'ancien système basé sur les noms de fichiers.
     */
    fun getWordEntriesForLevel(levelIdOrFileName: String): List<WordEntry> {
        return runBlocking {
            getWordEntriesForLevelSuspend(levelIdOrFileName)
        }
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
