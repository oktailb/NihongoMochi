package org.nihongo.mochi.domain.meaning

import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import org.nihongo.mochi.domain.kana.ResourceLoader

class WordMeaningRepository(private val resourceLoader: ResourceLoader) {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }
    private val cachedMeanings = mutableMapOf<String, Map<String, String>>()

    fun getWordMeanings(locale: String): Map<String, String> {
        return runBlocking {
            getWordMeaningsSuspend(locale)
        }
    }

    suspend fun getWordMeaningsSuspend(locale: String): Map<String, String> {
        if (cachedMeanings.containsKey(locale)) {
            return cachedMeanings[locale]!!
        }

        val fileName = getFileName(locale)
        return try {
            val jsonString = resourceLoader.loadJson(fileName)
            if (jsonString == "{}" || jsonString.isEmpty()) throw Exception("File not found or empty: $fileName")

            val root = json.decodeFromString<WordMeaningRoot>(jsonString)
            val meaningsMap = root.word_meanings.entries.associate { it.id to it.meaning }
            cachedMeanings[locale] = meaningsMap
            meaningsMap
        } catch (e: Exception) {
            println("Error parsing word meanings for $locale (file: $fileName): ${e.message}")
            // Fallback to english (GB) if the locale is not found
            if (locale != "en_GB") {
                getWordMeaningsSuspend("en_GB")
            } else {
                emptyMap()
            }
        }
    }

    private fun getFileName(locale: String): String {
        val parts = locale.split("_")
        return if (parts.size == 2 && parts[1].length == 2 && parts[1][0].isUpperCase()) {
             "words/meanings/word_meanings_${parts[0]}_r${parts[1]}.json"
        } else {
            "words/meanings/word_meanings_${locale}.json"
        }
    }
}
