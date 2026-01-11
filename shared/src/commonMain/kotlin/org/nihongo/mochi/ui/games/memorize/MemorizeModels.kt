package org.nihongo.mochi.ui.games.memorize

import kotlinx.serialization.Serializable

// Wrapper to treat both Kanji and Kana as playable items in Memorize game
data class MemorizePlayable(
    val id: String,
    val character: String
)

data class MemorizeCardState(
    val id: Int,
    val item: MemorizePlayable,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

enum class MemorizeGameMode {
    KANJI_KANJI,
}

data class MemorizeGridSize(
    val rows: Int,
    val cols: Int
) {
    val totalCards: Int get() = rows * cols
    val pairsCount: Int get() = totalCards / 2
    
    override fun toString(): String = "${cols}x${rows}"
}

@Serializable
data class MemorizeGameResult(
    val moves: Int,
    val totalPairs: Int,
    val gridSizeLabel: String,
    val timeSeconds: Int,
    val timestamp: Long
)
