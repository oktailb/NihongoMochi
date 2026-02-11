package org.nihongo.mochi.ui.games.simon

import kotlinx.serialization.Serializable

@Serializable
enum class SimonGameState {
    IDLE,
    SHOWING_SEQUENCE,
    AWAITING_INPUT,
    GAME_OVER,
    PAUSED
}

@Serializable
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
enum class PlayableType { KANJI, HIRAGANA, KATAKANA }

@Serializable
data class SimonPlayable(
    val id: String,
    val character: String,
    val meanings: List<String>,
    val readings: List<String>,
    val type: PlayableType
)

@Serializable
data class SimonGameStateData(
    val targetSequence: List<SimonPlayable>,
    val currentScore: Int,
    val gameTimeSeconds: Int,
    val selectedMode: SimonMode,
    val gameState: SimonGameState
)

@Serializable
data class SimonGameResult(
    val levelId: String,
    val mode: SimonMode = SimonMode.KANJI,
    val maxSequence: Int,
    val timeSeconds: Int,
    val timestamp: Long
)
