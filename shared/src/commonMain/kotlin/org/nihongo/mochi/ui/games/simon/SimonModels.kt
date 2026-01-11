package org.nihongo.mochi.ui.games.simon

import kotlinx.serialization.Serializable

enum class SimonGameState {
    IDLE,
    SHOWING_SEQUENCE,
    AWAITING_INPUT,
    GAME_OVER
}

enum class SimonMode {
    // Kanji / JLPT Modes
    KANJI,
    MEANING,
    READING_COMMON,
    READING_RANDOM,
    
    // Kana Specific Modes
    KANA_SAME,  // Same syllabary (e.g. Hiragana to Hiragana)
    KANA_CROSS  // Cross syllabary (e.g. Hiragana to Katakana)
}

@Serializable
data class SimonGameResult(
    val levelId: String,
    val mode: SimonMode = SimonMode.KANJI,
    val maxSequence: Int,
    val timeSeconds: Int,
    val timestamp: Long
)
