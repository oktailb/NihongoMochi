package org.nihongo.mochi.ui.games.memorize

import kotlinx.serialization.Serializable

@Serializable
data class MemorizePlayable(
    val id: String,
    val character: String
)

@Serializable
data class MemorizeCardState(
    val id: Int,
    val item: MemorizePlayable,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

@Serializable
enum class MemorizeGameMode {
    KANJI_KANJI,
}

@Serializable
data class MemorizeGridSize(
    val rows: Int,
    val cols: Int
) {
    val totalCards: Int get() = rows * cols
    val pairsCount: Int get() = totalCards / 2
    
    override fun toString(): String = "${cols}x${rows}"
}

@Serializable
data class MemorizeGameState(
    val cards: List<MemorizeCardState>,
    val moves: Int,
    val gameTimeSeconds: Int,
    val selectedGridSize: MemorizeGridSize,
    val isKanaLevel: Boolean,
    val isFinished: Boolean
)

@Serializable
data class MemorizeGameResult(
    val moves: Int,
    val totalPairs: Int,
    val gridSizeLabel: String,
    val timeSeconds: Int,
    val timestamp: Long
)
