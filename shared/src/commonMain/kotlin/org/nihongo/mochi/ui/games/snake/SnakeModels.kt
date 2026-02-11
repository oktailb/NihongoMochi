package org.nihongo.mochi.ui.games.snake

import kotlinx.serialization.Serializable

@Serializable
enum class SnakeMode {
    HIRAGANA,
    KATAKANA,
    NUMBERS,
    WORDS
}

@Serializable
data class Point(val x: Int, val y: Int)

@Serializable
enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

@Serializable
data class SnakeItem(
    val character: String,
    val position: Point,
    val isTarget: Boolean
)

@Serializable
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
    val gridHeight: Int = 30,
    val mode: SnakeMode = SnakeMode.HIRAGANA,
    val sequenceIndex: Int = 0,
    val currentNumber: Int = 1,
    val tickDelay: Long = 220L
)

@Serializable
data class SnakeGameResult(
    val mode: SnakeMode,
    val score: Int,
    val wordsCompleted: Int,
    val timeSeconds: Int,
    val timestamp: Long
)
