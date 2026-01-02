package org.nihongo.mochi.domain.statistics

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.shared.generated.resources.Res

class StatisticsEngine(
    private val levelContentProvider: LevelContentProvider
) {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Serializable
    data class ActivityConfig(
        val dataFile: String,
        val enabled: Boolean = false
    )

    @Serializable
    data class LevelDefinition(
        val id: String,
        val name: String,
        val description: String = "",
        val sortOrder: Int = 0,
        val globalStep: Int = 0,
        val dependencies: List<String> = emptyList(),
        val activities: Map<StatisticsType, ActivityConfig> = emptyMap()
    )

    @Serializable
    data class SectionDefinition(
        val name: String,
        val description: String = "",
        val prerequisiteFor: List<String> = emptyList(),
        val sortOrder: Int = 0,
        val levels: List<LevelDefinition> = emptyList()
    )

    @Serializable
    data class LevelDefinitions(
        val version: String = "1.0",
        val sections: Map<String, SectionDefinition> = emptyMap(),
        val activityTypes: Map<String, String> = emptyMap()
    )

    private var cachedSections: Map<String, SectionDefinition> = emptyMap()

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLevelDefinitions() {
        if (cachedSections.isNotEmpty()) return
        try {
            val jsonString = Res.readBytes("files/levels.json").decodeToString()
            val data = jsonParser.decodeFromString<LevelDefinitions>(jsonString)
            cachedSections = data.sections
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAllLevels(): List<LevelDefinition> {
        return cachedSections.values.flatMap { it.levels }.sortedBy { it.globalStep }
    }

    fun getAllStatistics(): List<LevelProgress> {
        val levels = getAllLevels()
        
        return levels.flatMap { level ->
             level.activities.mapNotNull { (type, config) ->
                 if (!config.enabled) return@mapNotNull null
                 
                 // If dataFile is generic ("kanji_details"), use the level ID to be specific
                 val resolvedKey = if (config.dataFile == "kanji_details") level.id else config.dataFile
                 
                 val percentage = calculatePercentage(resolvedKey, type)
                 
                 LevelProgress(
                    title = level.name, 
                    xmlName = resolvedKey, // Use the resolved key so UI navigation receives the ID, not "kanji_details"
                    percentage = percentage.toInt(),
                    type = type,
                    category = getCategoryForLevel(level),
                    sortOrder = level.sortOrder
                 )
             }
        }
    }
    
    private fun getCategoryForLevel(level: LevelDefinition): String {
        return cachedSections.entries.find { (_, section) -> 
            section.levels.any { it.id == level.id } 
        }?.value?.name ?: "Unknown"
    }
    
    fun getSagaMapSteps(tab: SagaTab): List<SagaStep> {
        val sectionKeys = when(tab) {
            SagaTab.JLPT -> listOf("fundamentals", "jlpt") 
            SagaTab.SCHOOL -> listOf("fundamentals", "school")
            SagaTab.CHALLENGES -> listOf("challenge") 
        }
        
        val relevantLevels = sectionKeys.mapNotNull { cachedSections[it] }
            .flatMap { it.levels }
        
        if (relevantLevels.isEmpty()) return emptyList()

        // --- Dependency-based topological sort / depth calculation ---
        
        val levelMap = relevantLevels.associateBy { it.id }
        val depthCache = mutableMapOf<String, Int>()
        
        fun getDepth(levelId: String, currentStack: Set<String>): Int {
            if (levelId in currentStack) return 0 
            if (depthCache.containsKey(levelId)) return depthCache[levelId]!!
            
            val level = levelMap[levelId] ?: return 0
            val validDependencies = level.dependencies.filter { levelMap.containsKey(it) }
            
            val depth = if (validDependencies.isEmpty()) {
                0
            } else {
                validDependencies.maxOf { getDepth(it, currentStack + levelId) } + 1
            }
            
            depthCache[levelId] = depth
            return depth
        }
        
        val levelsByDepth = relevantLevels.groupBy { level ->
            getDepth(level.id, emptySet())
        }
        .toSortedMap()

        return levelsByDepth.map { (depth, levels) ->
            
            val nodes = levels.map { level ->
                val recogActivity = level.activities[StatisticsType.RECOGNITION]
                val readActivity = level.activities[StatisticsType.READING]
                val writeActivity = level.activities[StatisticsType.WRITING]
                val grammarActivity = level.activities[StatisticsType.GRAMMAR]
                val gamesActivity = level.activities[StatisticsType.GAMES]
                
                val mainType = when {
                    recogActivity != null -> StatisticsType.RECOGNITION
                    readActivity != null -> StatisticsType.READING
                    writeActivity != null -> StatisticsType.WRITING
                    grammarActivity != null -> StatisticsType.GRAMMAR
                    gamesActivity != null -> StatisticsType.GAMES
                    else -> StatisticsType.RECOGNITION
                }

                // Resolve keys for Saga nodes too
                val recogId = recogActivity?.let { if (it.dataFile == "kanji_details") level.id else it.dataFile }
                val readId = readActivity?.let { if (it.dataFile == "kanji_details") level.id else it.dataFile }
                val writeId = writeActivity?.let { if (it.dataFile == "kanji_details") level.id else it.dataFile }

                SagaNode(
                    id = level.id,
                    title = level.name,
                    recognitionId = recogId,
                    readingId = readId,
                    writingId = writeId,
                    mainType = mainType
                )
            }
            
            SagaStep(
                id = "step_$depth",
                nodes = nodes
            )
        }
    }
    
    fun getSagaProgress(node: SagaNode): UserSagaProgress {
        val recogProgress = node.recognitionId?.let { calculatePercentage(it, StatisticsType.RECOGNITION) } ?: 0
        val readProgress = node.readingId?.let { calculatePercentage(it, StatisticsType.READING) } ?: 0
        val writeProgress = node.writingId?.let { calculatePercentage(it, StatisticsType.WRITING) } ?: 0
        
        val nodeMap = mutableMapOf<String, Int>()
        if (node.recognitionId != null) nodeMap[node.recognitionId] = recogProgress
        if (node.readingId != null) nodeMap[node.readingId] = readProgress
        if (node.writingId != null) nodeMap[node.writingId] = writeProgress
        
        return UserSagaProgress(
            recognitionIndex = recogProgress,
            readingIndex = readProgress,
            writingIndex = writeProgress,
            nodeProgress = nodeMap
        )
    }

    private fun calculatePercentage(xmlName: String, type: StatisticsType): Int {
        val scoreType = when(type) {
            StatisticsType.READING -> ScoreManager.ScoreType.READING
            StatisticsType.WRITING -> ScoreManager.ScoreType.WRITING
            StatisticsType.RECOGNITION -> ScoreManager.ScoreType.RECOGNITION
            else -> ScoreManager.ScoreType.RECOGNITION 
        }
        
        return if (xmlName == "user_list") {
             calculateUserListPercentage(scoreType).toInt()
        } else {
             val characters = levelContentProvider.getCharactersForLevel(xmlName)
             calculateMasteryPercentage(characters, scoreType).toInt()
        }
    }

    private fun calculateUserListPercentage(scoreType: ScoreManager.ScoreType): Double {
        val scores = ScoreManager.getAllScores(scoreType)
        if (scores.isEmpty()) return 0.0

        val totalEncountered = scores.size
        val mastered = scores.count { (_, score) -> (score.successes - score.failures) >= 10 }

        return if (totalEncountered > 0) {
            (mastered.toDouble() / totalEncountered.toDouble()) * 100.0
        } else {
            0.0
        }
    }

    private fun calculateMasteryPercentage(characterList: List<String>, scoreType: ScoreManager.ScoreType): Double {
        if (characterList.isEmpty()) return 0.0

        val totalMasteryPoints = characterList.sumOf { character ->
            val score = ScoreManager.getScore(character, scoreType)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = characterList.size * 10.0
        if (maxPossiblePoints == 0.0) return 0.0

        return (totalMasteryPoints / maxPossiblePoints) * 100.0
    }
}
