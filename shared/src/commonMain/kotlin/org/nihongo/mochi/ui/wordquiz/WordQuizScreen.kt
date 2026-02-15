package org.nihongo.mochi.ui.wordquiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.components.GameAnswerButton
import org.nihongo.mochi.ui.components.GameProgressBar
import org.nihongo.mochi.ui.components.GameQuestionCard
import org.nihongo.mochi.ui.components.ExitConfirmationDialog
import org.nihongo.mochi.ui.components.GameResultOverlay
import org.nihongo.mochi.ui.theme.AppTheme
import org.nihongo.mochi.shared.generated.resources.*

@Composable
fun WordQuizScreen(
    wordToGuess: String?,
    gameStatus: List<GameStatus>,
    answers: List<String>,
    buttonStates: List<AnswerButtonState>,
    buttonsEnabled: Boolean,
    gameState: GameState,
    globalMasteryPercent: Float,
    sessionMasteryPercent: Float,
    onAnswerClick: (Int, String) -> Unit,
    onReplay: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var showResultOverlay by remember { mutableStateOf(false) }

    BackHandler(enabled = gameState != GameState.Finished && !showResultOverlay) {
        showExitDialog = true
    }

    LaunchedEffect(gameState) {
        if (gameState == GameState.Finished) {
            showResultOverlay = true
        }
    }

    AppTheme {
        MochiBackground {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        if (gameState != GameState.Finished) showExitDialog = true 
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }

                // Progress Bar
                GameProgressBar(
                    statuses = gameStatus,
                    maxItems = 10
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Word Display Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (wordToGuess != null) {
                        val fontSize = when {
                            wordToGuess.length > 10 -> 40f
                            wordToGuess.length > 5 -> 60f
                            else -> 80f
                        }
                        
                        GameQuestionCard(
                            text = wordToGuess,
                            fontSize = fontSize.sp,
                            modifier = Modifier.size(width = 300.dp, height = 200.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Answers Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    val row1 = answers.take(2)
                    val row2 = answers.drop(2).take(2)
                    
                    fun getState(index: Int) = buttonStates.getOrElse(index) { AnswerButtonState.DEFAULT }

                    if (row1.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            GameAnswerButton(
                                text = row1[0],
                                state = getState(0),
                                enabled = buttonsEnabled,
                                modifier = Modifier.weight(1f).padding(4.dp),
                                fontSizeSp = 20,
                                onClick = { onAnswerClick(0, row1[0]) }
                            )
                            if (row1.size > 1) {
                                GameAnswerButton(
                                    text = row1[1],
                                    state = getState(1),
                                    enabled = buttonsEnabled,
                                    modifier = Modifier.weight(1f).padding(4.dp),
                                    fontSizeSp = 20,
                                    onClick = { onAnswerClick(1, row1[1]) }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                            }
                        }
                    }

                    if (row2.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            GameAnswerButton(
                                text = row2[0],
                                state = getState(2),
                                enabled = buttonsEnabled,
                                modifier = Modifier.weight(1f).padding(4.dp),
                                fontSizeSp = 20,
                                onClick = { onAnswerClick(2, row2[0]) }
                            )
                            if (row2.size > 1) {
                                GameAnswerButton(
                                    text = row2[1],
                                    state = getState(3),
                                    enabled = buttonsEnabled,
                                    modifier = Modifier.weight(1f).padding(4.dp),
                                    fontSizeSp = 20,
                                    onClick = { onAnswerClick(3, row2[1]) }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                            }
                        }
                    }
                }
            }

            if (showExitDialog) {
                ExitConfirmationDialog(
                    onConfirm = { 
                        showExitDialog = false
                        showResultOverlay = true 
                    },
                    onDismiss = { showExitDialog = false },
                    onPause = { },
                    onResume = { }
                )
            }

            if (showResultOverlay) {
                GameResultOverlay(
                    isVictory = gameState == GameState.Finished,
                    score = "${(globalMasteryPercent * 100).toInt()}%",
                    stats = listOf(
                        stringResource(Res.string.game_result_title_session) to "${(sessionMasteryPercent * 100).toInt()}%",
                        stringResource(Res.string.game_result_title_global) to "${(globalMasteryPercent * 100).toInt()}%"
                    ),
                    title = stringResource(Res.string.game_result_lot_mastery_reading),
                    onReplayClick = {
                        showResultOverlay = false
                        onReplay()
                    },
                    onMenuClick = {
                        showResultOverlay = false
                        onNavigateBack()
                    }
                )
            }
        }
    }
}
