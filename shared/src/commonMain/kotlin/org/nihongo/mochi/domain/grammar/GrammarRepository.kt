package org.nihongo.mochi.domain.grammar

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.nihongo.mochi.domain.kana.ResourceLoader

@Serializable
data class GrammarDefinition(
    val version: String,
    val metadata: GrammarMetadata,
    val dependencies_basics: List<GrammarRule>,
    val conjugaison: List<GrammarRule> = emptyList(),
    val rules: List<GrammarRule>
)

@Serializable
data class GrammarMetadata(
    val levels: List<String>,
    val categories: List<String>
)

@Serializable
data class GrammarRule(
    val id: String,
    val description: String,
    val level: String,
    val dependencies: List<String>,
    val category: String? = null,
    val tags: List<String> = emptyList()
)

class GrammarRepository(
    private val resourceLoader: ResourceLoader
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var grammarDefinition: GrammarDefinition? = null
    private var cachedAllRules: List<GrammarRule>? = null

    suspend fun loadGrammarDefinition(): GrammarDefinition {
        grammarDefinition?.let { return it }

        return try {
            val jsonString = resourceLoader.loadJson("grammar/grammar.json")
            if (jsonString == "{}" || jsonString.isBlank()) {
                throw Exception("Grammar JSON is empty or default")
            }
            
            json.decodeFromString<GrammarDefinition>(jsonString).also {
                grammarDefinition = it
                // Pre-cache all rules to avoid repeated list concatenations
                cachedAllRules = it.dependencies_basics + it.conjugaison + it.rules
            }
        } catch (e: Exception) {
            println("Error loading grammar definition: ${e.message}")
            // Return an empty but valid structure to avoid crash
            val fallback = GrammarDefinition(
                version = "0",
                metadata = GrammarMetadata(emptyList(), emptyList()),
                dependencies_basics = emptyList(),
                conjugaison = emptyList(),
                rules = emptyList()
            )
            grammarDefinition = fallback
            cachedAllRules = emptyList()
            fallback
        }
    }

    suspend fun getCategories(): List<String> {
        return loadGrammarDefinition().metadata.categories
    }

    suspend fun getAllRules(): List<GrammarRule> {
        if (cachedAllRules == null) {
            loadGrammarDefinition()
        }
        return cachedAllRules ?: emptyList()
    }

    suspend fun getRuleById(id: String): GrammarRule? {
        return getAllRules().find { it.id == id }
    }

    suspend fun getRulesUntilLevel(maxLevelId: String): List<GrammarRule> {
        val def = loadGrammarDefinition()
        val levelsOrder = def.metadata.levels
        val maxLevelIndex = levelsOrder.indexOf(maxLevelId)
        
        if (maxLevelIndex == -1) return emptyList()

        return getAllRules().filter { rule ->
            val ruleLevelIndex = levelsOrder.indexOf(rule.level)
            ruleLevelIndex != -1 && ruleLevelIndex <= maxLevelIndex
        }
    }

    suspend fun getRulesByBlock(block: String, maxLevelId: String): List<GrammarRule> {
        val def = loadGrammarDefinition()
        val rulesInBlock = when (block) {
            "dependencies_basics" -> def.dependencies_basics
            "conjugaison" -> def.conjugaison
            "rules" -> def.rules
            else -> getAllRules()
        }

        val levelsOrder = def.metadata.levels
        val maxLevelIndex = levelsOrder.indexOf(maxLevelId)
        if (maxLevelIndex == -1) return rulesInBlock

        return rulesInBlock.filter { rule ->
            val ruleLevelIndex = levelsOrder.indexOf(rule.level)
            ruleLevelIndex != -1 && ruleLevelIndex <= maxLevelIndex
        }
    }
    
    suspend fun hasLesson(ruleId: String): Boolean {
        return getAllRules().any { it.id == ruleId }
    }
    
    suspend fun loadCss(isDark: Boolean): String {
        val fileName = if (isDark) "styles_dark.css" else "styles_light.css"
        return try {
            // Use loadHtml because CSS is also a raw text file similar to HTML
            resourceLoader.loadHtml("grammar/lessons/$fileName")
        } catch (e: Exception) {
             if (isDark) {
                 "body { font-family: sans-serif; padding: 16px; color: #E0E0E0; background-color: #121212; }"
             } else {
                 "body { font-family: sans-serif; padding: 16px; color: #333; background-color: #FFFFFF; }"
             }
        }
    }
    
    suspend fun loadLessonHtml(ruleId: String, languageCode: String): String {
        // Our Hybrid ResourceLoader already handles locale-based file picking.
        // It will first look into the downloaded pack for the current locale,
        // then fallback to embedded resources (English).
        
        val lessonPath = "grammar/lessons/$ruleId.html"
        
        return try {
            val content = resourceLoader.loadHtml(lessonPath)
            if (content.isBlank() || content.startsWith("<html><body>Error")) {
                throw Exception("Lesson empty or not found")
            }
            content
        } catch (e: Exception) {
            // Final fallback UI
            """
                <div style="text-align: center; padding: 20px; font-family: sans-serif;">
                    <h3>Lesson not found</h3>
                    <p>Could not load lesson content for: <b>$ruleId</b></p>
                    <p><small>Check your internet connection or language pack status.</small></p>
                </div>
            """.trimIndent()
        }
    }
}
