package org.nihongo.mochi.domain.kana

import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import org.nihongo.mochi.ui.games.taquin.NumberData
import org.nihongo.mochi.ui.games.taquin.NumberEntry

class KanaRepository(private val resourceLoader: ResourceLoader) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private var hiraganaCache: List<KanaEntry>? = null
    private var katakanaCache: List<KanaEntry>? = null

    fun getKanaEntries(type: KanaType): List<KanaEntry> {
        return runBlocking {
            getKanaEntriesSuspend(type)
        }
    }

    suspend fun getKanaEntriesSuspend(type: KanaType): List<KanaEntry> {
        if (type == KanaType.HIRAGANA && hiraganaCache != null) return hiraganaCache!!
        if (type == KanaType.KATAKANA && katakanaCache != null) return katakanaCache!!

        val fileName = when (type) {
            KanaType.HIRAGANA -> "kana/hiragana.json"
            KanaType.KATAKANA -> "kana/katakana.json"
        }
        
        val jsonString = resourceLoader.loadJson(fileName)
        val result = try {
            json.decodeFromString<KanaData>(jsonString).characters
        } catch (e: Exception) {
            println("Error parsing kana data: ${e.message}")
            emptyList()
        }
        
        if (type == KanaType.HIRAGANA) hiraganaCache = result
        if (type == KanaType.KATAKANA) katakanaCache = result
        
        return result
    }

    suspend fun getNumberEntries(): List<NumberEntry> {
        return try {
            val jsonString = resourceLoader.loadJson("numbers.json")
            json.decodeFromString<NumberData>(jsonString).numbers
        } catch (e: Exception) {
            println("Error parsing numbers data: ${e.message}")
            emptyList()
        }
    }
}
