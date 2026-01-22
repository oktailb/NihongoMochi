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

        val jsonString = resourceLoader.loadJson("grammar/grammar.json")
        return json.decodeFromString<GrammarDefinition>(jsonString).also {
            grammarDefinition = it
            // Pre-cache all rules to avoid repeated list concatenations
            cachedAllRules = it.dependencies_basics + it.conjugaison + it.rules
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
            resourceLoader.loadJson("grammar/lessons/$fileName")
        } catch (e: Exception) {
             if (isDark) {
                 "body { font-family: sans-serif; padding: 16px; color: #E0E0E0; background-color: #121212; }"
             } else {
                 "body { font-family: sans-serif; padding: 16px; color: #333; background-color: #FFFFFF; }"
             }
        }
    }
    
    suspend fun loadLessonHtml(ruleId: String, languageCode: String): String {
        val safeLang = if (languageCode.length >= 2) languageCode.substring(0, 2).lowercase() else "en"
        val localizedPath = "grammar/lessons/$safeLang/$ruleId.html"
        val defaultLangPath = "grammar/lessons/en/$ruleId.html"
        val rootPath = "grammar/lessons/$ruleId.html"
        
        try {
            return resourceLoader.loadJson(localizedPath)
        } catch (e: Exception) {}

        if (safeLang != "en") {
            try {
                return resourceLoader.loadJson(defaultLangPath)
            } catch (e: Exception) {}
        }

        try {
            return resourceLoader.loadJson(rootPath)
        } catch (e: Exception) {
            return """
                <div style="text-align: center; padding: 20px;">
                    <h3>Lesson not found</h3>
                    <p>Could not load lesson content for ID: <b>$ruleId</b></p>
                    <p><small>Language: $safeLang</small></p>
                </div>
            """.trimIndent()
        }
    }
}
