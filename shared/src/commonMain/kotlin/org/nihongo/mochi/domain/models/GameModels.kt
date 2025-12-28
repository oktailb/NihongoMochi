package org.nihongo.mochi.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Reading(val value: String, val type: String, val frequency: Int)

@Serializable
data class KanjiDetail(val id: String, val character: String, val meanings: List<String>, val readings: List<Reading>)

enum class GameStatus { NOT_ANSWERED, PARTIAL, CORRECT, INCORRECT }

enum class AnswerButtonState { DEFAULT, CORRECT, INCORRECT, NEUTRAL }

@Serializable
data class KanaCharacter(val kana: String, val romaji: String, val category: String)

@Serializable
data class KanaProgress(var normalSolved: Boolean = false, var reverseSolved: Boolean = false)

@Serializable
data class KanjiProgress(
    var normalSolved: Boolean = false, 
    var reverseSolved: Boolean = false,
    var meaningSolved: Boolean = false, 
    var readingSolved: Boolean = false
)

enum class KanaQuestionDirection { NORMAL, REVERSE } // NORMAL: Kana -> Romaji, REVERSE: Romaji -> Kana

@Serializable
data class Word(val text: String, val phonetics: String)
