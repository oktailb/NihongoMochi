package org.nihongo.mochi.data

import com.russhwolf.settings.Settings
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import org.nihongo.mochi.settings.ADD_WRONG_ANSWERS_PREF_KEY
import org.nihongo.mochi.settings.REMOVE_GOOD_ANSWERS_PREF_KEY

object ScoreManager : ScoreRepository {

    private lateinit var scoresSettings: Settings
    private lateinit var userListSettings: Settings
    private lateinit var appSettings: Settings

    fun init(scoresSettings: Settings, userListSettings: Settings, appSettings: Settings) {
        this.scoresSettings = scoresSettings
        this.userListSettings = userListSettings
        this.appSettings = appSettings
    }

    enum class ScoreType {
        RECOGNITION, // Default, raw key
        READING,     // Prefixed with "reading_"
        WRITING      // Prefixed with "writing_"
    }

    override fun saveScore(key: String, wasCorrect: Boolean, type: ScoreType) {
        val actualKey = getActualKey(key, type)
        val currentScore = getScoreInternal(actualKey)

        val newSuccesses = currentScore.successes + if (wasCorrect) 1 else 0
        val newFailures = currentScore.failures + if (!wasCorrect) 1 else 0
        val currentTime = Clock.System.now().toEpochMilliseconds()

        scoresSettings.putString(actualKey, "$newSuccesses-$newFailures-$currentTime")

        val shouldAddWrongAnswers = appSettings.getBoolean(ADD_WRONG_ANSWERS_PREF_KEY, true)
        val shouldRemoveGoodAnswers = appSettings.getBoolean(REMOVE_GOOD_ANSWERS_PREF_KEY, true)

        if (!wasCorrect && shouldAddWrongAnswers) {
            addToUserList(key)
        } else if (wasCorrect && shouldRemoveGoodAnswers) {
            val balance = newSuccesses - newFailures
            if (balance >= 10) {
                removeFromUserList(key)
            }
        }
    }

    private fun getList(listName: String): MutableSet<String> {
        val serialized = userListSettings.getString(listName, "")
        if (serialized.isEmpty()) return mutableSetOf()
        return try {
             Json.decodeFromString<Set<String>>(serialized).toMutableSet()
        } catch (e: Exception) {
            // Fallback for CSV if we used that, or empty
            mutableSetOf()
        }
    }
    
    private fun saveList(listName: String, list: Set<String>) {
        val serialized = Json.encodeToString(list)
        userListSettings.putString(listName, serialized)
    }

    private fun addToUserList(key: String) {
        val listName = "Default"
        val currentList = getList(listName)
        if (currentList.add(key)) {
            saveList(listName, currentList)
        }
    }

    private fun removeFromUserList(key: String) {
        val listName = "Default"
        val currentList = getList(listName)
        if (currentList.remove(key)) {
            saveList(listName, currentList)
        }
    }

    override fun getScore(key: String, type: ScoreType): KanjiScore {
        val actualKey = getActualKey(key, type)
        return getScoreInternal(actualKey)
    }

    private fun getScoreInternal(actualKey: String): KanjiScore {
        val scoreString = scoresSettings.getString(actualKey, "0-0-0")

        return try {
            val parts = scoreString.split("-")
            val successes = parts[0].toInt()
            val failures = parts[1].toInt()
            val lastDate = if (parts.size > 2) parts[2].toLong() else 0L
            KanjiScore(successes, failures, lastDate)
        } catch (e: Exception) {
            KanjiScore(0, 0, 0L)
        }
    }

    private fun getActualKey(key: String, type: ScoreType): String {
        return when (type) {
            ScoreType.RECOGNITION -> key
            ScoreType.READING -> "reading_$key"
            ScoreType.WRITING -> "writing_$key"
        }
    }

    override fun getAllScores(type: ScoreType): Map<String, KanjiScore> {
        val prefix = when(type) {
            ScoreType.READING -> "reading_"
            ScoreType.WRITING -> "writing_"
            ScoreType.RECOGNITION -> "" 
        }
        
        return scoresSettings.keys.mapNotNull { storedKey ->
            val keyMatchesType = when (type) {
                ScoreType.RECOGNITION -> !storedKey.startsWith("reading_") && !storedKey.startsWith("writing_")
                ScoreType.READING -> storedKey.startsWith(prefix)
                ScoreType.WRITING -> storedKey.startsWith(prefix)
            }

            if (keyMatchesType) {
                val value = scoresSettings.getString(storedKey, "")
                if (value.isNotEmpty()) {
                    val cleanKey = if (prefix.isNotEmpty()) storedKey.removePrefix(prefix) else storedKey
                    try {
                        val parts = value.split("-")
                        val successes = parts[0].toInt()
                        val failures = parts[1].toInt()
                        val lastDate = if (parts.size > 2) parts[2].toLong() else 0L
                        cleanKey to KanjiScore(successes, failures, lastDate)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }.toMap()
    }

    override fun decayScores(): Boolean {
        var anyScoreDecayed = false
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000L

        scoresSettings.keys.forEach { key ->
            val value = scoresSettings.getString(key, "")
            if (value.isNotEmpty()) {
                try {
                    val parts = value.split("-")
                    var successes = parts[0].toInt()
                    val failures = parts[1].toInt()
                    val lastDate = if (parts.size > 2) parts[2].toLong() else 0L

                    if (currentTime - lastDate > ONE_WEEK_MS && successes > 0) {
                        successes /= 2
                        scoresSettings.putString(key, "$successes-$failures-$currentTime")
                        anyScoreDecayed = true
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
        return anyScoreDecayed
    }

    override fun getAllDataJson(): String {
        val scoresMap = mutableMapOf<String, String>()
        scoresSettings.keys.forEach { key ->
            val value = scoresSettings.getString(key, "")
            if (value.isNotEmpty()) scoresMap[key] = value
        }

        val userListsMap = mutableMapOf<String, List<String>>()
        userListSettings.keys.forEach { key ->
            // Here we assume keys in userListSettings are list names
            // And values are JSON strings (after migration)
            val listJson = userListSettings.getString(key, "")
            if (listJson.isNotEmpty()) {
                 try {
                     val list = Json.decodeFromString<List<String>>(listJson)
                     userListsMap[key] = list
                 } catch (e: Exception) {
                     // Could be old format or invalid
                 }
            }
        }

        val jsonObject = buildJsonObject {
            put("scores", buildJsonObject {
                scoresMap.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
            })
            put("user_lists", buildJsonObject {
                userListsMap.forEach { (k, v) -> 
                    putJsonArray(k) {
                        v.forEach { add(JsonPrimitive(it)) }
                    }
                }
            })
        }

        return Json.encodeToString(jsonObject)
    }

    override fun restoreDataFromJson(jsonString: String) {
        try {
            val jsonElement = Json.parseToJsonElement(jsonString)
            if (jsonElement is JsonObject) {
                
                val scoresElement = jsonElement["scores"]
                if (scoresElement is JsonObject) {
                    scoresElement.forEach { (key, value) ->
                        if (value is JsonPrimitive && value.isString) {
                            scoresSettings.putString(key, value.content)
                        }
                    }
                }

                val userListsElement = jsonElement["user_lists"]
                if (userListsElement is JsonObject) {
                    userListsElement.forEach { (key, value) ->
                        if (value is kotlinx.serialization.json.JsonArray) {
                            val list = value.map { (it as JsonPrimitive).content }
                            // Save as JSON string
                            val serialized = Json.encodeToString(list)
                            userListSettings.putString(key, serialized)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
