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

    private const val DEFAULT_LIST_NAME = "Default"
    private const val MEMORIZE_HISTORY_KEY = "memorize_history_json"
    private const val SIMON_HISTORY_KEY = "simon_history_json"
    private const val TAQUIN_HISTORY_KEY = "taquin_history_json"
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
        WRITING,      // Prefixed with "writing_"
        GRAMMAR      // Prefixed with "grammar_"
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

    // --- Memorize History ---
    fun saveMemorizeHistory(historyJson: String) {
        appSettings.putString(MEMORIZE_HISTORY_KEY, historyJson)
    }

    fun getMemorizeHistory(): String {
        return appSettings.getString(MEMORIZE_HISTORY_KEY, "[]")
    }

    // --- Simon History ---
    fun saveSimonHistory(historyJson: String) {
        appSettings.putString(SIMON_HISTORY_KEY, historyJson)
    }

    fun getSimonHistory(): String {
        return appSettings.getString(SIMON_HISTORY_KEY, "[]")
    }

    // --- Taquin History ---
    fun saveTaquinHistory(historyJson: String) {
        appSettings.putString(TAQUIN_HISTORY_KEY, historyJson)
    }

    fun getTaquinHistory(): String {
        return appSettings.getString(TAQUIN_HISTORY_KEY, "[]")
    }

    private fun getList(listName: String): MutableSet<String> {
        val serialized = userListSettings.getString(listName, "")
        if (serialized.isEmpty()) return mutableSetOf()
        return try {
             Json.decodeFromString<Set<String>>(serialized).toMutableSet()
        } catch (_: Exception) {
            mutableSetOf()
        }
    }
    
    private fun saveList(listName: String, list: Set<String>) {
        val serialized = Json.encodeToString(list)
        userListSettings.putString(listName, serialized)
    }

    private fun addToUserList(key: String) {
        val currentList = getList(DEFAULT_LIST_NAME)
        if (currentList.add(key)) {
            saveList(DEFAULT_LIST_NAME, currentList)
        }
    }

    private fun removeFromUserList(key: String) {
        val currentList = getList(DEFAULT_LIST_NAME)
        if (currentList.remove(key)) {
            saveList(DEFAULT_LIST_NAME, currentList)
        }
    }

    override fun getScore(key: String, type: ScoreType): LearningScore {
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
        } catch (_: Exception) {
            KanjiScore(0, 0, 0L)
        }
    }

    private fun getActualKey(key: String, type: ScoreType): String {
        return when (type) {
            ScoreType.RECOGNITION -> key
            ScoreType.READING -> "reading_$key"
            ScoreType.WRITING -> "writing_$key"
            ScoreType.GRAMMAR -> "grammar_$key"
        }
    }

    override fun getAllScores(type: ScoreType): Map<String, LearningScore> {
        val prefix = when(type) {
            ScoreType.READING -> "reading_"
            ScoreType.WRITING -> "writing_"
            ScoreType.GRAMMAR -> "grammar_"
            ScoreType.RECOGNITION -> "" 
        }
        
        return scoresSettings.keys.mapNotNull { storedKey ->
            val keyMatchesType = when (type) {
                ScoreType.RECOGNITION -> !storedKey.startsWith("reading_") && !storedKey.startsWith("writing_") && !storedKey.startsWith("grammar_")
                ScoreType.READING -> storedKey.startsWith(prefix)
                ScoreType.WRITING -> storedKey.startsWith(prefix)
                ScoreType.GRAMMAR -> storedKey.startsWith(prefix)
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
                    } catch (_: Exception) {
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
                    val successes = parts[0].toInt()
                    val failures = parts[1].toInt()
                    val lastDate = if (parts.size > 2) parts[2].toLong() else 0L

                    val weeksPassed = (currentTime - lastDate) / ONE_WEEK_MS

                    if (weeksPassed >= 1 && successes > 0) {
                        val decayPercent = (weeksPassed * 0.10).coerceAtMost(0.50)
                        val newSuccesses = (successes * (1.0 - decayPercent)).toInt()
                        
                        scoresSettings.putString(key, "$newSuccesses-$failures-$currentTime")
                        anyScoreDecayed = true
                    }
                } catch (_: Exception) {
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
            val listJson = userListSettings.getString(key, "")
            if (listJson.isNotEmpty()) {
                 try {
                     val list = Json.decodeFromString<List<String>>(listJson)
                     userListsMap[key] = list
                 } catch (_: Exception) {
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
            put("memorize_history", JsonPrimitive(getMemorizeHistory()))
            put("simon_history", JsonPrimitive(getSimonHistory()))
            put("taquin_history", JsonPrimitive(getTaquinHistory()))
        }

        return Json.encodeToString(jsonObject)
    }

    override fun restoreDataFromJson(json: String) {
        try {
            val jsonElement = Json.parseToJsonElement(json)
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
                            val serialized = Json.encodeToString(list)
                            userListSettings.putString(key, serialized)
                        }
                    }
                }
                
                val memorizeHistory = jsonElement["memorize_history"]
                if (memorizeHistory is JsonPrimitive && memorizeHistory.isString) {
                    saveMemorizeHistory(memorizeHistory.content)
                }

                val simonHistory = jsonElement["simon_history"]
                if (simonHistory is JsonPrimitive && simonHistory.isString) {
                    saveSimonHistory(simonHistory.content)
                }

                val taquinHistory = jsonElement["taquin_history"]
                if (taquinHistory is JsonPrimitive && taquinHistory.isString) {
                    saveTaquinHistory(taquinHistory.content)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
