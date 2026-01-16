package org.nihongo.mochi.ui.games.taquin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameHUD
import org.nihongo.mochi.ui.components.GameResultOverlay
import kotlin.math.abs

@Composable
fun TaquinGameScreen(
    viewModel: TaquinViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()

    MochiBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            state?.let { gameState ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Factorized HUD (Contains Game Title and Stats)
                    GameHUD(
                        primaryStat = stringResource(Res.string.game_taquin_title) to "",
                        secondaryStat = stringResource(Res.string.game_taquin_moves_label) to gameState.moves.toString(),
                        timerFlow = viewModel.gameState.map { it?.timeSeconds ?: 0 },
                        initialTimerValue = gameState.timeSeconds
                    )

                    // The Puzzle Grid
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val boardPadding = 4.dp
                            val availableWidth = maxWidth - (boardPadding * 2)
                            val availableHeight = maxHeight - (boardPadding * 2)
                            
                            val pieceSizeWidth = availableWidth / gameState.cols
                            val pieceSizeHeight = availableHeight / gameState.rows
                            val pieceSize = minOf(pieceSizeWidth, pieceSizeHeight)
                            
                            val dynamicFontSize = (pieceSize.value * 0.6f).sp

                            Column(
                                modifier = Modifier
                                    .width(pieceSize * gameState.cols)
                                    .height(pieceSize * gameState.rows),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                for (r in 0 until gameState.rows) {
                                    Row(
                                        modifier = Modifier.height(pieceSize).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (c in 0 until gameState.cols) {
                                            val index = r * gameState.cols + c
                                            val piece = gameState.pieces.getOrNull(index)
                                            
                                            Box(modifier = Modifier.size(pieceSize).padding(1.dp)) {
                                                if (piece != null) {
                                                    TaquinPieceItem(
                                                        modifier = Modifier.fillMaxSize(),
                                                        piece = piece,
                                                        fontSize = dynamicFontSize,
                                                        onClick = { viewModel.onPieceClicked(index) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Game Result Overlay
                if (gameState.isSolved) {
                    GameResultOverlay(
                        isVictory = true,
                        stats = listOf(
                            stringResource(Res.string.game_taquin_moves_label) to gameState.moves.toString(),
                            stringResource(Res.string.game_taquin_time_label) to formatGameTimeHUD(gameState.timeSeconds)
                        ),
                        onReplayClick = { viewModel.startGame() },
                        onMenuClick = onBackClick
                    )
                }
            }
        }
    }
}

private fun formatGameTimeHUD(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}

@Composable
fun TaquinPieceItem(
    modifier: Modifier = Modifier,
    piece: TaquinPiece,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit
) {
    var dragAccumulatedX by remember { mutableStateOf(0f) }
    var dragAccumulatedY by remember { mutableStateOf(0f) }
    val dragThreshold = 20f // Sensibilité du slide

    val backgroundColor = if (piece.isBlank) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .pointerInput(piece.isBlank) {
                if (!piece.isBlank) {
                    detectDragGestures(
                        onDragStart = {
                            dragAccumulatedX = 0f
                            dragAccumulatedY = 0f
                        },
                        onDragEnd = {
                            if (abs(dragAccumulatedX) > dragThreshold || abs(dragAccumulatedY) > dragThreshold) {
                                onClick()
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulatedX += dragAmount.x
                            dragAccumulatedY += dragAmount.y
                        }
                    )
                }
            }
            .clickable(enabled = !piece.isBlank) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!piece.isBlank) {
            Text(
                text = piece.character,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
