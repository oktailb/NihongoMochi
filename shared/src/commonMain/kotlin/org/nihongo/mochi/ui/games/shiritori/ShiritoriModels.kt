package org.nihongo.mochi.ui.games.shiritori

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock

@Serializable
data class ShiritoriWord(
    val word: String,
    val phonetics: String,
    val meaning: String,
    val isPlayer: Boolean,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
data class ShiritoriGameStateData(
    val playedWords: List<ShiritoriWord>,
    val lastKana: String,
    val score: Int,
    val gameTimeSeconds: Int,
    val usedPhonetics: Set<String>,
    val gameState: ShiritoriGameState
)

@Serializable
enum class ShiritoriGameState {
    IDLE,
    LOADING,
    PLAYER_TURN,
    AI_TURN,
    GAME_OVER,
    PAUSED
}

@Serializable
data class ShiritoriGameResult(
    val levelId: String,
    val score: Int,
    val timeSeconds: Int,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
