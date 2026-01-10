package org.nihongo.mochi.ui.games.taquin

import kotlinx.serialization.Serializable

@Serializable
enum class TaquinMode {
    HIRAGANA,
    KATAKANA,
    NUMBERS
}

data class TaquinPiece(
    val character: String,
    val targetLine: Int,
    val targetColumn: Int,
    val isBlank: Boolean = false
)

data class TaquinGameState(
    val pieces: List<TaquinPiece>,
    val rows: Int,
    val cols: Int,
    val moves: Int = 0,
    val timeSeconds: Int = 0,
    val isSolved: Boolean = false
)

@Serializable
data class TaquinGameResult(
    val mode: TaquinMode,
    val rows: Int,
    val moves: Int,
    val timeSeconds: Int,
    val timestamp: Long
)
