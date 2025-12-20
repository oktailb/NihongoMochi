package org.nihongo.mochi.data

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import org.nihongo.mochi.R
import org.nihongo.mochi.ui.settings.ADD_WRONG_ANSWERS_PREF_KEY
import org.nihongo.mochi.ui.settings.REMOVE_GOOD_ANSWERS_PREF_KEY

object ScoreManager {

    private const val PREFS_NAME = "KanjiMoriScores"
    private const val USER_LIST_PREFS_NAME = "KanjiMoriUserLists"

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

        // Handle user list based on settings
        val settingsPrefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val shouldAddWrongAnswers = settingsPrefs.getBoolean(ADD_WRONG_ANSWERS_PREF_KEY, true)
        val shouldRemoveGoodAnswers = settingsPrefs.getBoolean(REMOVE_GOOD_ANSWERS_PREF_KEY, true)

        if (!wasCorrect && shouldAddWrongAnswers) {
            addToUserList(context, key)
        } else if (wasCorrect && shouldRemoveGoodAnswers) {
            // Check if the word is mastered (balance of 10)
            val balance = newSuccesses - newFailures
            if (balance >= 10) {
                removeFromUserList(context, key)
            }
        }
    }

    private fun addToUserList(context: Context, key: String) {
        val userListPrefs = context.getSharedPreferences(USER_LIST_PREFS_NAME, Context.MODE_PRIVATE)
        val listName = "Default" // Assuming a default list for now
        val currentList = userListPrefs.getStringSet(listName, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        
        currentList.add(key)
        
        userListPrefs.edit().putStringSet(listName, currentList).apply()
    }

    private fun removeFromUserList(context: Context, key: String) {
        val userListPrefs = context.getSharedPreferences(USER_LIST_PREFS_NAME, Context.MODE_PRIVATE)
        val listName = "Default"
        val currentList = userListPrefs.getStringSet(listName, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        currentList.remove(key)

        userListPrefs.edit().putStringSet(listName, currentList).apply()
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

    fun getScoreColor(context: Context, score: KanjiScore): Int {
        val balance = score.successes - score.failures
        val percentage = (balance.toFloat() / 10.0f).coerceIn(-1.0f, 1.0f)
        val baseColor = ContextCompat.getColor(context, R.color.recap_grid_base_color)

        return when {
            percentage > 0 -> lerpColor(baseColor, Color.GREEN, percentage)
            percentage < 0 -> lerpColor(baseColor, Color.RED, -percentage)
            else -> baseColor
        }
    }

    private fun lerpColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startA = Color.alpha(startColor)
        val startR = Color.red(startColor)
        val startG = Color.green(startColor)
        val startB = Color.blue(startColor)

        val endA = Color.alpha(endColor)
        val endR = Color.red(endColor)
        val endG = Color.green(endColor)
        val endB = Color.blue(endColor)

        val a = (startA + fraction * (endA - startA)).toInt()
        val r = (startR + fraction * (endR - startR)).toInt()
        val g = (startG + fraction * (endG - startG)).toInt()
        val b = (startB + fraction * (endB - startB)).toInt()

        return Color.argb(a, r, g, b)
    }
}