package org.oktail.kanjimori.data

import android.content.Context

object ScoreManager {

    private const val PREFS_NAME = "KanjiMoriScores"

    fun saveScore(context: Context, kanjiCharacter: String, wasCorrect: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val currentScore = getScore(context, kanjiCharacter)
        val newSuccesses = currentScore.successes + if (wasCorrect) 1 else 0
        val newFailures = currentScore.failures + if (!wasCorrect) 1 else 0

        // Store as a simple string like "successes-failures"
        editor.putString(kanjiCharacter, "$newSuccesses-$newFailures")
        editor.apply()
    }

    fun getScore(context: Context, kanjiCharacter: String): KanjiScore {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val scoreString = sharedPreferences.getString(kanjiCharacter, "0-0") ?: "0-0"

        return try {
            val parts = scoreString.split("-")
            val successes = parts[0].toInt()
            val failures = parts[1].toInt()
            KanjiScore(successes, failures)
        } catch (e: Exception) {
            // In case of parsing error, return a default score
            KanjiScore(0, 0)
        }
    }

    fun getAllScores(context: Context): Map<String, KanjiScore> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.all.mapNotNull { (key, value) ->
            if (value is String) {
                try {
                    val parts = value.split("-")
                    val successes = parts[0].toInt()
                    val failures = parts[1].toInt()
                    key to KanjiScore(successes, failures)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }.toMap()
    }
}