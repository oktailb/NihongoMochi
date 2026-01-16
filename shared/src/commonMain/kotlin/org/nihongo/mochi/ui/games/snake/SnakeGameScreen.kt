package org.nihongo.mochi.ui.games.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameResultOverlay
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeGameScreen(
    viewModel: SnakeViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    var dragAmount by remember { mutableStateOf(Offset.Zero) }
    val selectedMode by viewModel.selectedMode.collectAsState()

    MochiBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                text = stringResource(Res.string.game_snake_title),
                                color = MaterialTheme.colorScheme.onBackground
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    Icons.Default.ArrowBack, 
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                containerColor = Color.Transparent
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { dragAmount = Offset.Zero },
                                onDrag = { change, dragAmountDelta ->
                                    change.consume()
                                    dragAmount += dragAmountDelta
                                },
                                onDragEnd = {
                                    if (abs(dragAmount.x) > abs(dragAmount.y)) {
                                        if (dragAmount.x > 40) viewModel.onDirectionChanged(Direction.RIGHT)
                                        else if (dragAmount.x < -40) viewModel.onDirectionChanged(Direction.LEFT)
                                    } else {
                                        if (dragAmount.y > 40) viewModel.onDirectionChanged(Direction.DOWN)
                                        else if (dragAmount.y < -40) viewModel.onDirectionChanged(Direction.UP)
                                    }
                                }
                            )
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Info Header (Score)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GameStatItem(label = stringResource(Res.string.game_kana_link_score_label), value = state.score.toString())
                        if (state.wordsCompleted > 0) {
                            GameStatItem(label = stringResource(Res.string.game_kana_link_words_label), value = state.wordsCompleted.toString())
                        }
                    }

                    // Target Word/Item Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.currentTargetLabel,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            if (selectedMode == SnakeMode.WORDS) {
                                Text(
                                    text = stringResource(Res.string.game_snake_eat_phonetics),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Game Board
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .aspectRatio(state.gridWidth.toFloat() / state.gridHeight.toFloat())
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        val density = LocalDensity.current
                        val cellSize = maxWidth / state.gridWidth
                        val cellSizePx = with(density) { cellSize.toPx() }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw Target Item (Circle)
                            state.targetItem?.let { item ->
                                drawCircle(
                                    color = Color(0xFF4CAF50),
                                    radius = cellSizePx * 0.55f,
                                    center = Offset(
                                        item.position.x * cellSizePx + cellSizePx / 2,
                                        item.position.y * cellSizePx + cellSizePx / 2
                                    )
                                )
                            }

                            // Draw Distractions (Circle)
                            state.distractions.forEach { item ->
                                drawCircle(
                                    color = Color.Gray.copy(alpha = 0.4f),
                                    radius = cellSizePx * 0.45f,
                                    center = Offset(
                                        item.position.x * cellSizePx + cellSizePx / 2,
                                        item.position.y * cellSizePx + cellSizePx / 2
                                    )
                                )
                            }

                            // Draw Snake (Circles)
                            state.snake.forEachIndexed { index, point ->
                                val color = if (index == 0) Color(0xFF2E7D32) else Color(0xFF4CAF50)
                                val scale = if (index == 0) 0.55f else 0.45f // Head slightly larger
                                drawCircle(
                                    color = color,
                                    radius = cellSizePx * scale,
                                    center = Offset(
                                        point.x * cellSizePx + cellSizePx / 2,
                                        point.y * cellSizePx + cellSizePx / 2
                                    )
                                )
                            }
                        }

                        // Labels
                        state.targetItem?.let { item ->
                            SnakeItemLabel(item.character, item.position, cellSize, Color.White)
                        }
                        state.distractions.forEach { item ->
                            SnakeItemLabel(item.character, item.position, cellSize, MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Game Result Overlay
                if (state.isGameOver) {
                    GameResultOverlay(
                        isVictory = false,
                        score = state.score.toString(),
                        stats = if (state.wordsCompleted > 0) listOf(
                            stringResource(Res.string.game_kana_link_words_label) to state.wordsCompleted.toString()
                        ) else emptyList(),
                        onReplayClick = { viewModel.startGame() },
                        onMenuClick = onBackClick
                    )
                }
            }
        }
    }
}

@Composable
fun GameStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SnakeItemLabel(text: String, position: Point, cellSize: androidx.compose.ui.unit.Dp, color: Color) {
    Box(
        modifier = Modifier
            .offset(x = cellSize * position.x, y = cellSize * position.y)
            .size(cellSize),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = (cellSize.value * 0.75f).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
