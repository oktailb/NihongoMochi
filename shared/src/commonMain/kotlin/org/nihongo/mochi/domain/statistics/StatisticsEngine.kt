package org.nihongo.mochi.domain.statistics

import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.levels.LevelDefinition
import org.nihongo.mochi.domain.util.LevelContentProvider

class StatisticsEngine(
    private val levelContentProvider: LevelContentProvider,
    private val levelsRepository: LevelsRepository,
    private val scoreRepository: ScoreRepository
) {

    suspend fun loadLevelDefinitions() {
        levelsRepository.loadLevelDefinitions()
    }

    private fun getAllLevels(): List<LevelDefinition> {
        val defs = levelsRepository.getDefinitions() ?: return emptyList()
        return defs.sections.values.flatMap { it.levels }.sortedBy { it.globalStep }
    }
    
    // Returns cached definitions directly if needed for low-level access
    private fun getDefinitionsOrEmpty() = levelsRepository.getDefinitions() 
        ?: org.nihongo.mochi.domain.levels.LevelDefinitions()

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
        val defs = levelsRepository.getDefinitions() ?: return "Unknown"
        return defs.sections.entries.find { (_, section) -> 
            section.levels.any { it.id == level.id } 
        }?.value?.name ?: "Unknown"
    }
    
    fun getSagaMapSteps(tab: SagaTab): List<SagaStep> {
        val defs = levelsRepository.getDefinitions() ?: return emptyList()
        
        val sectionKeys = when(tab) {
            SagaTab.JLPT -> listOf("fundamentals", "jlpt") 
            SagaTab.SCHOOL -> listOf("fundamentals", "school")
            SagaTab.CHALLENGES -> listOf("challenge") 
        }
        
        val relevantLevels = sectionKeys.mapNotNull { defs.sections[it] }
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
        val scores = scoreRepository.getAllScores(scoreType)
        if (scores.isEmpty()) return 0.0

        val totalEncountered = scores.size
        val mastered = scores.count { entry -> (entry.value.successes - entry.value.failures) >= 10 }

        return if (totalEncountered > 0) {
            (mastered.toDouble() / totalEncountered.toDouble()) * 100.0
        } else {
            0.0
        }
    }

    private fun calculateMasteryPercentage(characterList: List<String>, scoreType: ScoreManager.ScoreType): Double {
        if (characterList.isEmpty()) return 0.0

        val totalMasteryPoints = characterList.sumOf { character ->
            val score = scoreRepository.getScore(character, scoreType)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = characterList.size * 10.0
        if (maxPossiblePoints == 0.0) return 0.0

        return (totalMasteryPoints / maxPossiblePoints) * 100.0
    }
}
