package org.nihongo.mochi.ui.games.crossword

import kotlinx.serialization.Serializable

enum class CrosswordMode {
    KANAS,
    KANJIS
}

enum class CrosswordHintType {
    KANJI,
    MEANING
}

@Serializable
data class CrosswordCell(
    val r: Int,
    val c: Int,
    val solution: String = "",
    val userInput: String = "",
    val isBlack: Boolean = true,
    val number: Int? = null,
    val isCorrect: Boolean = false
)

@Serializable
data class CrosswordWord(
    val number: Int,
    val word: String, // Phonetics/Kana
    val kanji: String,
    val meaning: String,
    val row: Int,
    val col: Int,
    val isHorizontal: Boolean,
    val isSolved: Boolean = false
)

@Serializable
data class CrosswordGameResult(
    val wordCount: Int,
    val mode: CrosswordMode,
    val timeSeconds: Int,
    val completionPercentage: Int,
    val timestamp: Long
)
