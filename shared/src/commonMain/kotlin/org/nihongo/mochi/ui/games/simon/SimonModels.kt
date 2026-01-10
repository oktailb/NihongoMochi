package org.nihongo.mochi.ui.games.simon

import kotlinx.serialization.Serializable
import org.nihongo.mochi.domain.kanji.KanjiEntry

enum class SimonGameState {
    IDLE,
    SHOWING_SEQUENCE,
    AWAITING_INPUT,
    GAME_OVER
}

enum class SimonMode {
    KANJI,
    MEANING,
    READING_COMMON,
    READING_RANDOM
}

@Serializable
data class SimonGameResult(
    val levelId: String,
    val mode: SimonMode = SimonMode.KANJI,
    val maxSequence: Int,
    val timeSeconds: Int,
    val timestamp: Long
)
