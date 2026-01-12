package org.nihongo.mochi.ui.games.kanadrop

import kotlinx.serialization.Serializable
import org.nihongo.mochi.domain.words.WordEntry

@Serializable
enum class KanaLinkMode {
    TIME_ATTACK,
    SURVIVAL
}

@Serializable
data class KanaLinkResult(
    val score: Int,
    val wordsFound: Int,
    val timeSeconds: Int,
    val levelId: String,
    val timestamp: Long
)

data class KanaDropCell(
    val id: String,
    val char: String,
    val row: Int,
    val col: Int,
    val isSelected: Boolean = false,
    val isMatched: Boolean = false
)

data class KanaDropGameState(
    val grid: List<List<KanaDropCell>> = emptyList(),
    val selectedCells: List<KanaDropCell> = emptyList(),
    val currentWord: String = "",
    val score: Int = 0,
    val wordsFound: Int = 0,
    val timeRemaining: Int = 60,
    val isGameOver: Boolean = false,
    val lastValidWord: WordEntry? = null,
    val isLoading: Boolean = true,
    val errorFlash: Boolean = false
)

data class KanaDropConfig(
    val rows: Int = 10,
    val cols: Int = 7,
    val levelFileName: String = "",
    val mode: KanaLinkMode = KanaLinkMode.TIME_ATTACK,
    val initialTime: Int = 60
)
