package org.nihongo.mochi.domain.statistics

import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.util.LevelContentProvider

class StatisticsEngine(
    private val levelContentProvider: LevelContentProvider
) {

    private data class LevelDefinition(
        val name: String, 
        val xmlName: String, 
        val type: StatisticsType,
        val category: String,
        val sortOrder: Int
    )

    private fun getLevelDefinitions(): List<LevelDefinition> {
        return listOf(
            // --- RECOGNITION ---
            // Kanas - Same SortOrder = Parallel
            LevelDefinition("Hiragana", "Hiragana", StatisticsType.RECOGNITION, "Kanas", 1),
            LevelDefinition("Katakana", "Katakana", StatisticsType.RECOGNITION, "Kanas", 1),
            
            // JLPT
            LevelDefinition("JLPT N5", "N5", StatisticsType.RECOGNITION, "JLPT", 1),
            LevelDefinition("JLPT N4", "N4", StatisticsType.RECOGNITION, "JLPT", 2),
            LevelDefinition("JLPT N3", "N3", StatisticsType.RECOGNITION, "JLPT", 3),
            LevelDefinition("JLPT N2", "N2", StatisticsType.RECOGNITION, "JLPT", 4),
            LevelDefinition("JLPT N1", "N1", StatisticsType.RECOGNITION, "JLPT", 5),

            // School
            LevelDefinition("Grade 1", "Grade 1", StatisticsType.RECOGNITION, "School", 1),
            LevelDefinition("Grade 2", "Grade 2", StatisticsType.RECOGNITION, "School", 2),
            LevelDefinition("Grade 3", "Grade 3", StatisticsType.RECOGNITION, "School", 3),
            LevelDefinition("Grade 4", "Grade 4", StatisticsType.RECOGNITION, "School", 4),
            LevelDefinition("Grade 5", "Grade 5", StatisticsType.RECOGNITION, "School", 5),
            LevelDefinition("Grade 6", "Grade 6", StatisticsType.RECOGNITION, "School", 6),
            LevelDefinition("Collège", "Grade 7", StatisticsType.RECOGNITION, "School", 7),
            LevelDefinition("Lycée", "Grade 8", StatisticsType.RECOGNITION, "School", 8),

            // Challenges
            LevelDefinition("Native Challenge", "Native Challenge", StatisticsType.RECOGNITION, "Challenges", 1),
            LevelDefinition("No Reading", "No Reading", StatisticsType.RECOGNITION, "Challenges", 2),
            LevelDefinition("No Meaning", "No Meaning", StatisticsType.RECOGNITION, "Challenges", 3),

            // --- READING ---
            LevelDefinition("Reading User", "user_list", StatisticsType.READING, "User", 1),
            
            // JLPT
            LevelDefinition("Reading N5", "reading_n5", StatisticsType.READING, "JLPT", 1),
            LevelDefinition("Reading N4", "reading_n4", StatisticsType.READING, "JLPT", 2),
            LevelDefinition("Reading N3", "reading_n3", StatisticsType.READING, "JLPT", 3),
            LevelDefinition("Reading N2", "reading_n2", StatisticsType.READING, "JLPT", 4),
            LevelDefinition("Reading N1", "reading_n1", StatisticsType.READING, "JLPT", 5),
            
            // Frequency
            LevelDefinition("Reading 1000", "bccwj_wordlist_1000", StatisticsType.READING, "Frequency", 1),
            LevelDefinition("Reading 2000", "bccwj_wordlist_2000", StatisticsType.READING, "Frequency", 2),
            LevelDefinition("Reading 3000", "bccwj_wordlist_3000", StatisticsType.READING, "Frequency", 3),
            LevelDefinition("Reading 4000", "bccwj_wordlist_4000", StatisticsType.READING, "Frequency", 4),
            LevelDefinition("Reading 5000", "bccwj_wordlist_5000", StatisticsType.READING, "Frequency", 5),
            LevelDefinition("Reading 6000", "bccwj_wordlist_6000", StatisticsType.READING, "Frequency", 6),
            LevelDefinition("Reading 7000", "bccwj_wordlist_7000", StatisticsType.READING, "Frequency", 7),
            LevelDefinition("Reading 8000", "bccwj_wordlist_8000", StatisticsType.READING, "Frequency", 8),

            // --- WRITING ---
            LevelDefinition("Writing User", "user_list", StatisticsType.WRITING, "User", 1),

            // JLPT
            LevelDefinition("Writing JLPT N5", "N5", StatisticsType.WRITING, "JLPT", 1),
            LevelDefinition("Writing JLPT N4", "N4", StatisticsType.WRITING, "JLPT", 2),
            LevelDefinition("Writing JLPT N3", "N3", StatisticsType.WRITING, "JLPT", 3),
            LevelDefinition("Writing JLPT N2", "N2", StatisticsType.WRITING, "JLPT", 4),
            LevelDefinition("Writing JLPT N1", "N1", StatisticsType.WRITING, "JLPT", 5),
            
            // School
            LevelDefinition("Writing Grade 1", "Grade 1", StatisticsType.WRITING, "School", 1),
            LevelDefinition("Writing Grade 2", "Grade 2", StatisticsType.WRITING, "School", 2),
            LevelDefinition("Writing Grade 3", "Grade 3", StatisticsType.WRITING, "School", 3),
            LevelDefinition("Writing Grade 4", "Grade 4", StatisticsType.WRITING, "School", 4),
            LevelDefinition("Writing Grade 5", "Grade 5", StatisticsType.WRITING, "School", 5),
            LevelDefinition("Writing Grade 6", "Grade 6", StatisticsType.WRITING, "School", 6),
            LevelDefinition("Writing Collège", "Grade 7", StatisticsType.WRITING, "School", 7),
            LevelDefinition("Writing Lycée", "Grade 8", StatisticsType.WRITING, "School", 8)
        )
    }

    fun getAllStatistics(): List<LevelProgress> {
        val levels = getLevelDefinitions()
        return levels.map { level ->
            val percentage = calculatePercentage(level)
            LevelProgress(level.name, level.xmlName, percentage.toInt(), level.type, level.category, level.sortOrder)
        }
    }
    
    fun getSagaMapSteps(tab: SagaTab): List<SagaStep> {
        val allLevels = getLevelDefinitions()
        
        val categoryFilter = when(tab) {
            SagaTab.JLPT -> listOf("Kanas", "JLPT", "Frequency") 
            SagaTab.SCHOOL -> listOf("Kanas", "School", "Frequency")
            SagaTab.CHALLENGES -> listOf("Challenges", "Frequency") 
        }
        
        val relevantLevels = allLevels.filter { it.category in categoryFilter }
        
        // Group by (Category, SortOrder) to create a single node if it's the same level
        // BUT for Kanas, we want Hiragana and Katakana to be separate nodes in the same step.
        // So we need a finer key: (Category, SortOrder, GroupIdentity)
        // For Kanas: GroupIdentity is their Name (Hiragana vs Katakana).
        // For JLPT: GroupIdentity is their Name (N5, N4...).
        
        // Actually, we want to group by Step.
        // Step 0: Kanas (Hiragana, Katakana) -> 2 nodes
        // Step 1: JLPT N5 -> 1 node (with recog/read/write)
        
        // We need to assign a "Global Step Index" to group them properly.
        // Kanas = Step 0
        // JLPT = Step 10 + SortOrder
        // School = Step 20 + SortOrder
        // Frequency = Step 30 + SortOrder
        
        data class GlobalStepKey(val index: Int)
        
        val levelsWithStep = relevantLevels.map { level ->
             val stepIndex = when(level.category) {
                "Kanas" -> 0
                "JLPT" -> 10 + level.sortOrder
                "School" -> 20 + level.sortOrder
                "Challenges" -> 30 + level.sortOrder
                "Frequency" -> 40 + level.sortOrder
                else -> 999
            }
            stepIndex to level
        }
        
        val stepsGrouped = levelsWithStep.groupBy { it.first }
            .toSortedMap()
            
        return stepsGrouped.map { (stepIndex, pairs) ->
            val levelsInStep = pairs.map { it.second }
            
            // Within a step, we might have multiple distinct nodes (e.g. Hiragana and Katakana)
            // Or just one node that has multiple types (N5 Recog, N5 Read).
            
            // Strategy: Group by "Base Name" or something similar to merge Recog/Read/Write.
            // For Kanas: "Hiragana" and "Katakana" are distinct Base Names.
            // For JLPT: "N5" is the base name.
            
            // Let's use `xmlName` as key but clean it up? Or define a "NodeKey" manually.
            // For now, let's group by `xmlName` if type matches, or use mapping.
            // Hiragana (Recog) -> Key: Hiragana
            // N5 (Recog), N5 (Write), reading_n5 (Read) -> Key: N5 (we need to map reading_n5 back to N5)
            
            val nodeGroups = levelsInStep.groupBy { level ->
                // Normalize Key
                when {
                    level.xmlName.startsWith("reading_") -> level.xmlName.removePrefix("reading_").uppercase() // reading_n5 -> N5
                    level.category == "Kanas" -> level.xmlName // Hiragana != Katakana
                    else -> level.xmlName.uppercase() // N5 -> N5
                }
            }
            
            val nodes = nodeGroups.map { (key, levels) ->
                 val recogLevel = levels.find { it.type == StatisticsType.RECOGNITION }
                 val readLevel = levels.find { it.type == StatisticsType.READING }
                 val writeLevel = levels.find { it.type == StatisticsType.WRITING }
                 
                 val mainType = when {
                    recogLevel != null -> StatisticsType.RECOGNITION
                    readLevel != null -> StatisticsType.READING
                    writeLevel != null -> StatisticsType.WRITING
                    else -> StatisticsType.RECOGNITION
                }
                
                val title = when(mainType) {
                    StatisticsType.RECOGNITION -> recogLevel!!.name
                    StatisticsType.READING -> readLevel!!.name
                    StatisticsType.WRITING -> writeLevel!!.name
                }
                
                val nodeId = when(mainType) {
                    StatisticsType.RECOGNITION -> recogLevel!!.xmlName
                    StatisticsType.READING -> readLevel!!.xmlName
                    StatisticsType.WRITING -> writeLevel!!.xmlName
                }

                SagaNode(
                    id = nodeId,
                    title = title,
                    recognitionId = recogLevel?.xmlName,
                    readingId = readLevel?.xmlName,
                    writingId = writeLevel?.xmlName,
                    mainType = mainType
                )
            }
            
            SagaStep(
                id = "step_$stepIndex",
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
        }
        
        return if (xmlName == "user_list") {
             calculateUserListPercentage(scoreType).toInt()
        } else {
             val characters = levelContentProvider.getCharactersForLevel(xmlName)
             calculateMasteryPercentage(characters, scoreType).toInt()
        }
    }
    
    private fun calculatePercentage(level: LevelDefinition): Double {
        return calculatePercentage(level.xmlName, level.type).toDouble()
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
