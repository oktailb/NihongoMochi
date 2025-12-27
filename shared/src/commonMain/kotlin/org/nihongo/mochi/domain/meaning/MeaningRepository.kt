package org.nihongo.mochi.domain.meaning

import kotlinx.serialization.json.Json
import org.nihongo.mochi.domain.kana.ResourceLoader

class MeaningRepository(private val resourceLoader: ResourceLoader) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cachedMeanings = mutableMapOf<String, Map<String, List<String>>>()

    fun getMeanings(locale: String): Map<String, List<String>> {
        val fileName = getFileName(locale)
        if (cachedMeanings.containsKey(locale)) {
            return cachedMeanings[locale]!!
        }

        return try {
            val jsonString = resourceLoader.loadJson(fileName)
            if (jsonString == "{}") throw Exception("File not found or empty: $fileName")

            val root = json.decodeFromString<MeaningRoot>(jsonString)
            val meaningsMap = root.meanings.kanji.associate { it.id to it.meaning }
            cachedMeanings[locale] = meaningsMap
            meaningsMap
        } catch (e: Exception) {
            println("Error parsing meaning list for $locale (file: $fileName): ${e.message}")
            // Fallback to english (GB) if the locale is not found
            if (locale != "en_GB") {
                getMeanings("en_GB")
            } else {
                emptyMap()
            }
        }
    }

    private fun getFileName(locale: String): String {
        // Standard Android locale format is lang_REGION (e.g., fr_FR, en_GB)
        // File format seems to be meanings_lang_rREGION.json (e.g., meanings_fr_rFR.json)
        
        val parts = locale.split("_")
        // Check if it matches lang_REGION pattern (e.g. fr_FR) to insert the 'r' prefix
        if (parts.size == 2 && parts[1].length == 2 && parts[1][0].isUpperCase()) {
             return "meanings/meanings_${parts[0]}_r${parts[1]}.json"
        }
        
        // Default or fallback if format doesn't match expected pattern or is already formatted
        return "meanings/meanings_${locale}.json"
    }
}
