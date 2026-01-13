package org.nihongo.mochi.domain.kanji

import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository

class KanjiRepository(
    private val resourceLoader: ResourceLoader,
    private val meaningRepository: MeaningRepository,
    private val settingsRepository: SettingsRepository
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedKanji: List<KanjiEntry>? = null
    private var characterMap: Map<String, KanjiEntry>? = null

    fun getAllKanji(): List<KanjiEntry> {
        return runBlocking {
            getAllKanjiSuspend()
        }
    }

    suspend fun getAllKanjiSuspend(): List<KanjiEntry> {
        cachedKanji?.let { return it }
        
        return try {
            val jsonString = resourceLoader.loadJson("kanji/kanji_details.json")
            val root = json.decodeFromString<KanjiDetailsRoot>(jsonString)
            val entries = root.kanjiDetails.kanji
            cachedKanji = entries
            characterMap = entries.associateBy { it.character }
            entries
        } catch (e: Exception) {
            println("Error parsing kanji details: ${e.message}")
            emptyList()
        }
    }

    fun getKanjiById(id: String): KanjiEntry? {
        // IDs are generally unique but not as frequently searched as characters
        return getAllKanji().find { it.id == id }
    }
    
    fun getKanjiByCharacter(char: String): KanjiEntry? {
        if (characterMap == null) {
            getAllKanji() // This populates characterMap
        }
        return characterMap?.get(char)
    }
    
    fun getKanjiByLevel(levelId: String): List<KanjiEntry> {
        return getAllKanji().filter { kanji ->
            kanji.level.any { it.equals(levelId, ignoreCase = true) }
        }
    }
    
    fun getNativeKanji(): List<KanjiEntry> {
        return getAllKanji().filter { 
            it.category.isEmpty() && it.readings?.reading?.isNotEmpty() == true
        }
    }

    fun getNoReadingKanji(): List<KanjiEntry> {
        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        return getAllKanji().filter { 
            val hasMeanings = meanings[it.id]?.isNotEmpty() == true
            it.category.isEmpty() && it.readings?.reading?.isEmpty() == true && hasMeanings
        }
    }

    fun getNoMeaningKanji(): List<KanjiEntry> {
        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        return getAllKanji().filter { 
            val hasMeanings = meanings[it.id]?.isNotEmpty() == true
            it.category.isEmpty() && !hasMeanings
        }
    }
}
