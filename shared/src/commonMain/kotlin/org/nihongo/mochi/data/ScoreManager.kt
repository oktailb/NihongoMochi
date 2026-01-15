package org.nihongo.mochi.data

import com.russhwolf.settings.Settings
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.nihongo.mochi.db.MochiDatabase
import org.nihongo.mochi.settings.ADD_WRONG_ANSWERS_PREF_KEY
import org.nihongo.mochi.settings.REMOVE_GOOD_ANSWERS_PREF_KEY
import org.nihongo.mochi.ui.games.simon.SimonGameResult
import org.nihongo.mochi.ui.games.memorize.MemorizeGameResult
import org.nihongo.mochi.ui.games.taquin.TaquinGameResult
import org.nihongo.mochi.ui.games.kanadrop.KanaLinkResult
import org.nihongo.mochi.ui.games.crossword.CrosswordGameResult
import org.nihongo.mochi.ui.games.crossword.CrosswordMode

class ScoreManager(
    private val database: MochiDatabase,
    private val scoresSettings: Settings,
    private val userListSettings: Settings,
    private val appSettings: Settings
) : ScoreRepository {

    companion object {
        const val RECOGNITION_LIST = "Recognition_List"
        const val READING_LIST = "Reading_List"
        const val WRITING_LIST = "Writing_List"
        const val GRAMMAR_LIST = "Grammar_List"
        private const val MIGRATION_DONE_KEY = "sql_migration_done_v1"
    }

    private val queries = database.mochiDatabaseQueries

    init {
        migrateIfNeeded()
    }

    private fun migrateIfNeeded() {
        if (!appSettings.getBoolean(MIGRATION_DONE_KEY, false)) {
            // Migrate Scores
            scoresSettings.keys.forEach { storedKey ->
                val value = scoresSettings.getString(storedKey, "")
                if (value.isNotEmpty()) {
                    try {
                        val parts = value.split("-")
                        val successes = parts[0].toLong()
                        val failures = parts[1].toLong()
                        val lastDate = if (parts.size > 2) parts[2].toLong() else 0L
                        
                        val type = when {
                            storedKey.startsWith("reading_") -> ScoreType.READING
                            storedKey.startsWith("writing_") -> ScoreType.WRITING
                            storedKey.startsWith("grammar_") -> ScoreType.GRAMMAR
                            else -> ScoreType.RECOGNITION
                        }
                        
                        val cleanKey = when(type) {
                            ScoreType.READING -> storedKey.removePrefix("reading_")
                            ScoreType.WRITING -> storedKey.removePrefix("writing_")
                            ScoreType.GRAMMAR -> storedKey.removePrefix("grammar_")
                            ScoreType.RECOGNITION -> storedKey
                        }

                        queries.insertScore(cleanKey, type.name, successes, failures, lastDate)
                    } catch (_: Exception) {}
                }
            }

            // Migrate User Lists
            userListSettings.keys.forEach { listName ->
                val serialized = userListSettings.getString(listName, "")
                if (serialized.isNotEmpty()) {
                    try {
                        val list = Json.decodeFromString<Set<String>>(serialized)
                        list.forEach { itemKey ->
                            queries.addItemToList(listName, itemKey)
                        }
                    } catch (_: Exception) {}
                }
            }
            
            appSettings.putBoolean(MIGRATION_DONE_KEY, true)
        }
    }

    enum class ScoreType {
        RECOGNITION, READING, WRITING, GRAMMAR
    }

    override fun saveScore(key: String, wasCorrect: Boolean, type: ScoreType) {
        val current = queries.getScore(key, type.name).executeAsOneOrNull()
        
        val newSuccesses = (current?.successes ?: 0L) + if (wasCorrect) 1L else 0L
        val newFailures = (current?.failures ?: 0L) + if (!wasCorrect) 1L else 0L
        val currentTime = Clock.System.now().toEpochMilliseconds()

        queries.insertScore(key, type.name, newSuccesses, newFailures, currentTime)

        val shouldAddWrongAnswers = appSettings.getBoolean(ADD_WRONG_ANSWERS_PREF_KEY, true)
        val shouldRemoveGoodAnswers = appSettings.getBoolean(REMOVE_GOOD_ANSWERS_PREF_KEY, true)

        val targetList = when(type) {
            ScoreType.RECOGNITION -> RECOGNITION_LIST
            ScoreType.READING -> READING_LIST
            ScoreType.WRITING -> WRITING_LIST
            ScoreType.GRAMMAR -> GRAMMAR_LIST
        }

        if (!wasCorrect && shouldAddWrongAnswers) {
            queries.addItemToList(targetList, key)
        } else if (wasCorrect && shouldRemoveGoodAnswers) {
            val balance = newSuccesses - newFailures
            if (balance >= 10) {
                queries.removeItemFromList(targetList, key)
            }
        }
    }

    override fun getScore(key: String, type: ScoreType): LearningScore {
        val entity = queries.getScore(key, type.name).executeAsOneOrNull()
        return KanjiScore(
            successes = entity?.successes?.toInt() ?: 0,
            failures = entity?.failures?.toInt() ?: 0,
            lastReviewDate = entity?.lastReviewDate ?: 0L
        )
    }

    override fun getAllScores(type: ScoreType): Map<String, LearningScore> {
        return queries.getAllScoresByType(type.name).executeAsList().associate {
            it.key to KanjiScore(it.successes.toInt(), it.failures.toInt(), it.lastReviewDate)
        }
    }

    override fun getListItems(listName: String): List<String> {
        return queries.getListItems(listName).executeAsList()
    }

    override fun decayScores(): Boolean {
        var anyScoreDecayed = false
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000L

        val allScores = queries.getAllScores().executeAsList()
        allScores.forEach { entity ->
            val weeksPassed = (currentTime - entity.lastReviewDate) / ONE_WEEK_MS
            if (weeksPassed >= 1 && entity.successes > 0) {
                val decayPercent = (weeksPassed * 0.10).coerceAtMost(0.50)
                val newSuccesses = (entity.successes * (1.0 - decayPercent)).toLong()
                
                queries.insertScore(entity.key, entity.type, newSuccesses, entity.failures, currentTime)
                anyScoreDecayed = true
            }
        }
        return anyScoreDecayed
    }

    // --- Game History ---

    override fun saveMemorizeResult(result: MemorizeGameResult) {
        queries.insertGameResult(
            "MEMORIZE",
            result.totalPairs.toLong(),
            result.moves.toLong(),
            result.timeSeconds.toLong(),
            null,
            result.totalPairs.toLong(),
            null,
            null,
            result.timestamp,
            result.gridSizeLabel
        )
    }

    override fun getMemorizeHistory(): List<MemorizeGameResult> {
        return queries.getTopScoresByTotalPairs("MEMORIZE").executeAsList().map {
            MemorizeGameResult(
                moves = it.moves?.toInt() ?: 0,
                totalPairs = it.totalPairs?.toInt() ?: 0,
                gridSizeLabel = it.metadata ?: "", 
                timeSeconds = it.timeSeconds?.toInt() ?: 0,
                timestamp = it.timestamp
            )
        }
    }

    override fun saveSimonResult(result: SimonGameResult) {
        queries.insertGameResult(
            "SIMON",
            result.maxSequence.toLong(),
            null,
            result.timeSeconds.toLong(),
            result.maxSequence.toLong(),
            null,
            null,
            null,
            result.timestamp,
            result.levelId
        )
    }

    override fun getSimonHistory(): List<SimonGameResult> {
        return queries.getTopScoresByMaxSequence("SIMON").executeAsList().map {
            SimonGameResult(
                levelId = it.metadata ?: "", 
                mode = org.nihongo.mochi.ui.games.simon.SimonMode.KANJI, 
                maxSequence = it.maxSequence?.toInt() ?: 0,
                timeSeconds = it.timeSeconds?.toInt() ?: 0,
                timestamp = it.timestamp
            )
        }
    }

    override fun saveTaquinResult(result: TaquinGameResult) {
        queries.insertGameResult(
            "TAQUIN",
            result.rows.toLong(),
            result.moves.toLong(),
            result.timeSeconds.toLong(),
            null,
            null,
            result.rows.toLong(),
            null,
            result.timestamp,
            null
        )
    }

    override fun getTaquinHistory(): List<TaquinGameResult> {
        return queries.getTopScoresByRows("TAQUIN").executeAsList().map {
            TaquinGameResult(
                mode = org.nihongo.mochi.ui.games.taquin.TaquinMode.HIRAGANA,
                rows = it.rows?.toInt() ?: 0,
                moves = it.moves?.toInt() ?: 0,
                timeSeconds = it.timeSeconds?.toInt() ?: 0,
                timestamp = it.timestamp
            )
        }
    }

    override fun saveKanaLinkResult(result: KanaLinkResult) {
        queries.insertGameResult(
            "KANALINK",
            result.score.toLong(),
            null,
            result.timeSeconds.toLong(),
            null,
            null,
            null,
            result.wordsFound.toLong(),
            result.timestamp,
            result.levelId
        )
    }

    override fun getKanaLinkHistory(): List<KanaLinkResult> {
        return queries.getTopScoresByKanaLink("KANALINK").executeAsList().map {
            KanaLinkResult(
                score = it.score.toInt(),
                wordsFound = it.wordsFound?.toInt() ?: 0,
                timeSeconds = it.timeSeconds?.toInt() ?: 0,
                levelId = it.metadata ?: "",
                timestamp = it.timestamp
            )
        }
    }

    override fun saveCrosswordResult(result: CrosswordGameResult) {
        queries.insertGameResult(
            "CROSSWORD",
            result.wordCount.toLong(),
            null,
            result.timeSeconds.toLong(),
            null,
            null,
            null,
            result.wordCount.toLong(),
            result.timestamp,
            result.mode.name
        )
    }

    override fun getCrosswordHistory(): List<CrosswordGameResult> {
        return queries.getTopScoresByWordsFound("CROSSWORD").executeAsList().map {
            CrosswordGameResult(
                wordCount = it.wordsFound?.toInt() ?: 0,
                mode = try { CrosswordMode.valueOf(it.metadata ?: "KANAS") } catch(e: Exception) { CrosswordMode.KANAS },
                timeSeconds = it.timeSeconds?.toInt() ?: 0,
                completionPercentage = it.score.toInt(),
                timestamp = it.timestamp
            )
        }
    }

    // --- Import/Export ---

    override fun getAllDataJson(): String {
        val allScores = queries.getAllScores().executeAsList().associate { 
            val prefix = when(it.type) {
                ScoreType.READING.name -> "reading_"
                ScoreType.WRITING.name -> "writing_"
                ScoreType.GRAMMAR.name -> "grammar_"
                else -> ""
            }
            "$prefix${it.key}" to "${it.successes}-${it.failures}-${it.lastReviewDate}"
        }

        val allLists = queries.getAllLists().executeAsList()
            .groupBy { it.listName }
            .mapValues { entry -> entry.value.map { it.itemKey } }

        val jsonObject = buildJsonObject {
            put("scores", buildJsonObject {
                allScores.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
            })
            put("user_lists", buildJsonObject {
                allLists.forEach { (k, v) -> 
                    putJsonArray(k) { v.forEach { add(JsonPrimitive(it)) } }
                }
            })
            // History is now internal, but for backup we might want to keep it as JSON or similar
            // For simplicity in backup, let's keep encoding lists to JSON for the export format
            put("memorize_history", Json.encodeToJsonElement(getMemorizeHistory()))
            put("simon_history", Json.encodeToJsonElement(getSimonHistory()))
            put("taquin_history", Json.encodeToJsonElement(getTaquinHistory()))
            put("kana_link_history", Json.encodeToJsonElement(getKanaLinkHistory()))
            put("crossword_history", Json.encodeToJsonElement(getCrosswordHistory()))
        }

        return Json.encodeToString(jsonObject)
    }

    override fun restoreDataFromJson(jsonStr: String) {
        try {
            val jsonElement = Json.parseToJsonElement(jsonStr)
            if (jsonElement is JsonObject) {
                database.transaction {
                    // Restore Scores
                    jsonElement["scores"]?.jsonObject?.forEach { (storedKey, value) ->
                        val scoreStr = (value as? JsonPrimitive)?.content ?: ""
                        if (scoreStr.isNotEmpty()) {
                            val parts = scoreStr.split("-")
                            val type = when {
                                storedKey.startsWith("reading_") -> ScoreType.READING
                                storedKey.startsWith("writing_") -> ScoreType.WRITING
                                storedKey.startsWith("grammar_") -> ScoreType.GRAMMAR
                                else -> ScoreType.RECOGNITION
                            }
                            val cleanKey = when(type) {
                                ScoreType.READING -> storedKey.removePrefix("reading_")
                                ScoreType.WRITING -> storedKey.removePrefix("writing_")
                                ScoreType.GRAMMAR -> storedKey.removePrefix("grammar_")
                                ScoreType.RECOGNITION -> storedKey
                            }
                            queries.insertScore(cleanKey, type.name, parts[0].toLong(), parts[1].toLong(), parts.getOrNull(2)?.toLong() ?: 0L)
                        }
                    }

                    // Restore User Lists
                    jsonElement["user_lists"]?.jsonObject?.forEach { (listName, value) ->
                        (value as? JsonArray)?.forEach { item ->
                            val itemKey = (item as? JsonPrimitive)?.content ?: ""
                            if (itemKey.isNotEmpty()) {
                                queries.addItemToList(listName, itemKey)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }
}
