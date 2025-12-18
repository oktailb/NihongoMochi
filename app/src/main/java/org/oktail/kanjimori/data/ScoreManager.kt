package org.oktail.kanjimori.data

import android.content.Context

object ScoreManager {

    private const val PREFS_NAME = "KanjiMoriScores"

    enum class ScoreType {
        RECOGNITION, // Default, raw key
        READING,     // Prefixed with "reading_"
        WRITING      // Prefixed with "writing_"
    }

    fun saveScore(context: Context, key: String, wasCorrect: Boolean, type: ScoreType) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val actualKey = getActualKey(key, type)
        val currentScore = getScoreInternal(context, actualKey)

        val newSuccesses = currentScore.successes + if (wasCorrect) 1 else 0
        val newFailures = currentScore.failures + if (!wasCorrect) 1 else 0

        editor.putString(actualKey, "$newSuccesses-$newFailures")
        editor.apply()
    }

    fun getScore(context: Context, key: String, type: ScoreType): KanjiScore {
        val actualKey = getActualKey(key, type)
        return getScoreInternal(context, actualKey)
    }

    private fun getScoreInternal(context: Context, actualKey: String): KanjiScore {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val scoreString = sharedPreferences.getString(actualKey, "0-0") ?: "0-0"

        return try {
            val parts = scoreString.split("-")
            val successes = parts[0].toInt()
            val failures = parts[1].toInt()
            KanjiScore(successes, failures)
        } catch (e: Exception) {
            KanjiScore(0, 0)
        }
    }

    private fun getActualKey(key: String, type: ScoreType): String {
        return when (type) {
            ScoreType.RECOGNITION -> key
            ScoreType.READING -> "reading_$key"
            ScoreType.WRITING -> "writing_$key"
        }
    }

    fun getAllScores(context: Context, type: ScoreType): Map<String, KanjiScore> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefix = when(type) {
            ScoreType.READING -> "reading_"
            ScoreType.WRITING -> "writing_"
            ScoreType.RECOGNITION -> "" // No prefix for recognition
        }

        return sharedPreferences.all.mapNotNull { (storedKey, value) ->
            if (value is String) {
                val keyMatchesType = when (type) {
                    ScoreType.RECOGNITION -> !storedKey.startsWith("reading_") && !storedKey.startsWith("writing_")
                    ScoreType.READING -> storedKey.startsWith(prefix)
                    ScoreType.WRITING -> storedKey.startsWith(prefix)
                }

                if (keyMatchesType) {
                    val cleanKey = if (prefix.isNotEmpty()) storedKey.removePrefix(prefix) else storedKey
                    try {
                        val parts = value.split("-")
                        val successes = parts[0].toInt()
                        val failures = parts[1].toInt()
                        cleanKey to KanjiScore(successes, failures)
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
}