package org.nihongo.mochi.ui.games.snake

import kotlinx.serialization.Serializable

@Serializable
enum class SnakeMode {
    HIRAGANA,
    KATAKANA,
    NUMBERS,
    WORDS
}

data class Point(val x: Int, val y: Int)

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class SnakeItem(
    val character: String,
    val position: Point,
    val isTarget: Boolean
)

data class SnakeGameState(
    val snake: List<Point> = listOf(Point(10, 10), Point(10, 11), Point(10, 12)),
    val direction: Direction = Direction.UP,
    val targetItem: SnakeItem? = null,
    val distractions: List<SnakeItem> = emptyList(),
    val score: Int = 0,
    val wordsCompleted: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val timeSeconds: Int = 0,
    val currentTargetLabel: String = "",
    val gridWidth: Int = 20,
    val gridHeight: Int = 30
)

@Serializable
data class SnakeGameResult(
    val mode: SnakeMode,
    val score: Int,
    val wordsCompleted: Int,
    val timeSeconds: Int,
    val timestamp: Long
)
