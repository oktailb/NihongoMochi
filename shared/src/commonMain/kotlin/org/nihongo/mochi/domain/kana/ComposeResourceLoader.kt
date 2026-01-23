package org.nihongo.mochi.domain.kana

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.domain.services.LanguagePackManager

@OptIn(ExperimentalResourceApi::class)
class ComposeResourceLoader(
    private val languagePackManager: LanguagePackManager? = null,
    private val currentLocaleProvider: () -> String = { "en_GB" }
) : ResourceLoader {

    override suspend fun loadJson(fileName: String): String {
        val locale = currentLocaleProvider()
        
        // 1. Try to load from LanguagePackManager (Local downloaded files)
        // Normalize filename: "meanings/meanings_fr_rFR.json" -> "meanings.json"
        val normalizedName = when {
            fileName.contains("word_meanings") -> "word_meanings.json"
            fileName.contains("meanings") -> "meanings.json"
            else -> fileName.substringAfterLast("/")
        }
        
        val localData = languagePackManager?.loadLocalResource(normalizedName, locale)
        if (localData != null) {
            return localData
        }

        // 2. Fallback to Compose Resources (Embedded files)
        val path = "files/$fileName"
        return try {
            val bytes = Res.readBytes(path)
            bytes.decodeToString()
        } catch (e: Exception) {
            println("Error loading JSON resource $path: ${e.message}")
            "{}"
        }
    }

    override suspend fun loadHtml(fileName: String): String {
        val locale = currentLocaleProvider()
        
        // Grammar files are usually requested as "grammar/lessons/lesson_id.html"
        val lessonName = fileName.substringAfterLast("/")
        
        // 1. Try local storage (Language pack)
        val localData = languagePackManager?.loadLocalResource(lessonName, locale)
        if (localData != null) {
            return localData
        }

        // 2. Fallback to embedded (Compose Resources)
        // For embedded, we need to respect the directory structure: files/grammar/lessons/lesson_id.html
        val path = "files/$fileName"
        return try {
            val bytes = Res.readBytes(path)
            bytes.decodeToString()
        } catch (e: Exception) {
            println("Error loading HTML resource $path: ${e.message}")
            // Return a minimal valid HTML to avoid {} display
            "<html><body style='font-family: sans-serif; padding: 20px;'>Lesson content unavailable locally.</body></html>"
        }
    }
}
