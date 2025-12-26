package org.nihongo.mochi.domain.kana

import kotlinx.serialization.json.Json

class KanaRepository(private val resourceLoader: ResourceLoader) {
    
    private val json = Json { ignoreUnknownKeys = true }

    fun getKanaEntries(type: KanaType): List<KanaEntry> {
        val fileName = when (type) {
            KanaType.HIRAGANA -> "kana/hiragana.json"
            KanaType.KATAKANA -> "kana/katakana.json"
        }
        
        val jsonString = resourceLoader.loadJson(fileName)
        return try {
            json.decodeFromString<KanaData>(jsonString).characters
        } catch (e: Exception) {
            println("Error parsing kana data: ${e.message}")
            emptyList()
        }
    }
}
