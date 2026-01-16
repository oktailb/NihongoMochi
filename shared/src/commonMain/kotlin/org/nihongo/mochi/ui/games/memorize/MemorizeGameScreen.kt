package org.nihongo.mochi.ui.games.memorize

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameHUD
import org.nihongo.mochi.ui.components.GameResultOverlay

@Composable
fun MemorizeGameScreen(
    viewModel: MemorizeViewModel,
    onBackClick: () -> Unit
) {
    val isFinished by viewModel.isGameFinished.collectAsState()
    val moves by viewModel.moves.collectAsState()
    val timeSeconds by viewModel.gameTimeSeconds.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val gridSize by viewModel.selectedGridSize.collectAsState()

    // Ensure session is cleaned up when leaving the screen
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.abandonGame()
        }
    }

    MochiBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Factorized HUD
                GameHUD(
                    primaryStat = stringResource(Res.string.game_memorize_moves, "").split(":")[0] to moves.toString(),
                    secondaryStat = "" to stringResource(Res.string.game_memorize_pairs_count, cards.count { it.isMatched } / 2, gridSize.pairsCount),
                    timerFlow = viewModel.gameTimeSeconds,
                    initialTimerValue = timeSeconds
                )

                Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridSize.cols),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = cards,
                            key = { _, card -> card.id }
                        ) { index, card ->
                            MemoryCard(
                                state = card,
                                onClick = { viewModel.onCardClicked(index) }
                            )
                        }
                    }
                }
            }

            // Game Result Overlay
            if (isFinished) {
                GameResultOverlay(
                    isVictory = true,
                    stats = listOf(
                        stringResource(Res.string.game_memorize_moves, "").split(":")[0] to moves.toString(),
                        stringResource(Res.string.game_memorize_time, "").split(":")[0] to formatGameTime(timeSeconds)
                    ),
                    onReplayClick = { viewModel.startGame() },
                    onMenuClick = onBackClick
                )
            }
        }
    }
}

private fun formatGameTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}

@Composable
fun MemoryCard(
    state: MemorizeCardState,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (state.isFaceUp || state.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardRotation"
    )

    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !state.isFaceUp && !state.isMatched, onClick = onClick)
    ) {
        if (rotation <= 90f) {
            // Back
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("?", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        } else {
            // Front
            Card(
                modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f },
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isMatched) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                    contentColor = if (state.isMatched) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.item.character,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
