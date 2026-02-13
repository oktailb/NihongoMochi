package org.nihongo.mochi.ui.games.particle

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.ExitConfirmationDialog
import org.nihongo.mochi.ui.components.GameHUD
import org.nihongo.mochi.ui.components.GameQuestionCard
import org.nihongo.mochi.ui.components.GameResultOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticleDefenderScreen(
    viewModel: ParticleDefenderViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercepter le bouton retour physique
    BackHandler(enabled = !uiState.isGameOver) {
        showExitDialog = true
    }

    MochiBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = uiState.shipX + (dragAmount.x / size.width)
                        viewModel.onShipMove(newX)
                    }
                }
                .clickable { viewModel.onShoot() }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // HUD at the top
                GameHUD(
                    primaryStat = stringResource(Res.string.game_particles_title) to "",
                    secondaryStat = stringResource(Res.string.game_kana_link_score_label) to uiState.score.toString(),
                    modifier = Modifier.statusBarsPadding()
                )

                // Phrase Question Card
                GameQuestionCard(
                    text = "${uiState.currentSentencePrefix}「 ? 」${uiState.currentSentenceSuffix}",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Hearts Indicator (Dynamic based on lives)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(3) { index ->
                            Text(
                                text = if (index < uiState.lives) "♥" else "♡",
                                color = if (index < uiState.lives) Color.Red else Color.Gray.copy(alpha = 0.5f),
                                fontSize = 24.sp
                            )
                        }
                    }

                    // Enemies (Particles)
                    uiState.activeParticles.forEach { particle ->
                        val xPos = (particle.position.x * 0.8f + 0.1f)
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = (xPos * 360).dp, 
                                    y = (particle.position.y * 500).dp 
                                )
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = particle.char,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                        }
                    }

                    // Player Ship (Bottom)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                            .offset(x = ((uiState.shipX - 0.5f) * 300).dp)
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚀", fontSize = 32.sp)
                    }
                }
            }

            // Standard Close Button
            IconButton(
                onClick = { showExitDialog = true },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp)
            ) {
                Icon(
                    Icons.Default.Close, 
                    contentDescription = "Exit", 
                    tint = if (uiState.isGameOver) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Standard Game Over Overlay
            if (uiState.isGameOver) {
                GameResultOverlay(
                    isVictory = false,
                    score = uiState.score.toString(),
                    onReplayClick = { viewModel.startGame() },
                    onMenuClick = onBackClick
                )
            }

            // Correct implementation of ExitConfirmationDialog
            if (showExitDialog) {
                ExitConfirmationDialog(
                    onConfirm = {
                        showExitDialog = false
                        onBackClick()
                    },
                    onDismiss = { showExitDialog = false },
                    onPause = { viewModel.pauseGame() },
                    onResume = { viewModel.resumeGame() }
                )
            }
        }
    }
}
