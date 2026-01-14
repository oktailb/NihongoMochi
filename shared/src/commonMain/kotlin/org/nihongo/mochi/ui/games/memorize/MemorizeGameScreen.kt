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
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*

@Composable
fun MemorizeGameScreen(
    viewModel: MemorizeViewModel,
    onBackClick: () -> Unit
) {
    val isFinished by viewModel.isGameFinished.collectAsState()

    // Ensure session is cleaned up when leaving the screen
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.abandonGame()
        }
    }

    MochiBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MemorizeHeader(viewModel)

            Box(modifier = Modifier.weight(1f)) {
                if (isFinished) {
                    val moves by viewModel.moves.collectAsState()
                    val timeSeconds by viewModel.gameTimeSeconds.collectAsState()
                    
                    MemorizeResultOverlay(
                        moves = moves,
                        timeSeconds = timeSeconds,
                        onRestart = { viewModel.startGame() },
                        onBack = onBackClick
                    )
                } else {
                    val cards by viewModel.cards.collectAsState()
                    val gridSize by viewModel.selectedGridSize.collectAsState()
                    
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
        }
    }
}

@Composable
private fun MemorizeHeader(
    viewModel: MemorizeViewModel
) {
    val moves by viewModel.moves.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val gridSize by viewModel.selectedGridSize.collectAsState()

    // HUD Aligned to Top End (like Simon)
    Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(stringResource(Res.string.game_memorize_moves, moves), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            
            // Optimized Timer: passing the StateFlow directly to avoid recomposing the entire Header
            TimerText(viewModel.gameTimeSeconds)

            Text(
                stringResource(Res.string.game_memorize_pairs_count, cards.count { it.isMatched } / 2, gridSize.pairsCount),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TimerText(timeFlow: StateFlow<Int>) {
    val timeSeconds by timeFlow.collectAsState()
    Text(
        text = stringResource(Res.string.game_memorize_time, timeSeconds),
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
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

@Composable
fun MemorizeResultOverlay(
    moves: Int,
    timeSeconds: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(Res.string.game_memorize_finished), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(stringResource(Res.string.game_memorize_congrats), textAlign = TextAlign.Center)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val label = stringResource(Res.string.game_memorize_moves, moves).split(":")[0]
                    Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$moves", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val label = stringResource(Res.string.game_memorize_time, timeSeconds).split(":")[0]
                    Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${timeSeconds}s", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
            }
            
            Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.game_memorize_replay))
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(stringResource(Res.string.game_memorize_back_menu))
            }
        }
    }
}
