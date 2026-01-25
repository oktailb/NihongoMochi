package org.nihongo.mochi.domain.grammar

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.nihongo.mochi.domain.kana.ResourceLoader

@Serializable
data class ExerciseRoot(
    val exercises: List<Exercise>
)

class ExerciseRepository(
    private val resourceLoader: ResourceLoader
) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        encodeDefaults = true
    }
    
    // Cache per rule tag
    private val cache = mutableMapOf<String, List<Exercise>>()

    suspend fun getExercisesForTag(tag: String, limit: Int = 10): List<Exercise> {
        val cached = cache[tag]
        if (cached != null) return cached.shuffled().take(limit)

        return try {
            // New paradigm: load {rule_id}.json from grammar folder
            // Tag is the rule id (e.g., "forme_te")
            val fileName = "grammar/$tag.json"
            val jsonString = resourceLoader.loadJson(fileName)
            val root = json.decodeFromString<ExerciseRoot>(jsonString)
            
            cache[tag] = root.exercises
            root.exercises.shuffled().take(limit)
        } catch (e: Exception) {
            println("Error loading exercises for tag $tag: ${e.message}")
            emptyList()
        }
    }

    fun parsePayload(exercise: Exercise): ExercisePayload? {
        return try {
            when (exercise.type) {
                ExerciseType.FILL_BLANK -> json.decodeFromJsonElement<ExercisePayload.FillBlank>(exercise.payload)
                ExerciseType.SENTENCE_ORDER -> json.decodeFromJsonElement<ExercisePayload.SentenceOrder>(exercise.payload)
                ExerciseType.UNDERLINE_READING, ExerciseType.UNDERLINE_WRITING -> json.decodeFromJsonElement<ExercisePayload.Underline>(exercise.payload)
                ExerciseType.PARAPHRASE -> json.decodeFromJsonElement<ExercisePayload.Paraphrase>(exercise.payload)
                ExerciseType.WORD_USAGE -> json.decodeFromJsonElement<ExercisePayload.WordUsage>(exercise.payload)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
