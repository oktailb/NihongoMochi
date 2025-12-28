package org.nihongo.mochi.domain.kanji

import kotlinx.serialization.json.Json
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

    fun getAllKanji(): List<KanjiEntry> {
        if (cachedKanji != null) return cachedKanji!!
        
        val jsonString = resourceLoader.loadJson("kanji/kanji_details.json")
        return try {
            val root = json.decodeFromString<KanjiDetailsRoot>(jsonString)
            cachedKanji = root.kanjiDetails.kanji
            cachedKanji!!
        } catch (e: Exception) {
            println("Error parsing kanji details: ${e.message}")
            emptyList()
        }
    }

    fun getKanjiById(id: String): KanjiEntry? {
        return getAllKanji().find { it.id == id }
    }
    
    fun getKanjiByCharacter(char: String): KanjiEntry? {
        return getAllKanji().find { it.character == char }
    }
    
    fun getKanjiByLevel(levelType: String, levelValue: String): List<KanjiEntry> {
        return getAllKanji().filter { 
            when(levelType) {
                "jlpt" -> it.jlptLevel == levelValue
                "grade" -> it.schoolGrade == levelValue
                else -> false
            }
        }
    }

    fun getNativeKanji(): List<KanjiEntry> {
        return getAllKanji().filter { 
            it.jlptLevel == null && it.schoolGrade == null && it.readings?.reading?.isNotEmpty() == true
        }
    }

    fun getNoReadingKanji(): List<KanjiEntry> {
        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        return getAllKanji().filter { 
            it.jlptLevel == null && it.schoolGrade == null && it.readings?.reading?.isEmpty() == true && meanings.containsKey(it.id)
        }
    }

    fun getNoMeaningKanji(): List<KanjiEntry> {
        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        return getAllKanji().filter { 
            it.jlptLevel == null && it.schoolGrade == null && !meanings.containsKey(it.id) && it.readings?.reading?.isNotEmpty() == true
        }
    }
}
