package org.nihongo.mochi.ui.games.simon

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameAnswerButton
import org.nihongo.mochi.ui.components.PlayButton

@Composable
fun SimonSetupScreen(
    viewModel: SimonViewModel,
    onBackClick: () -> Unit,
    onStartGame: () -> Unit
) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val scoresHistory by viewModel.scoresHistory.collectAsState()
    val isKanaLevel by viewModel.isKanaLevel.collectAsState()

    MochiBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.game_simon_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
            )
            Text(
                text = stringResource(Res.string.game_simon_japanese_title),
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.game_config_title), 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SimonMode.entries.forEach { mode ->
                            val isVisible = if (isKanaLevel) mode == SimonMode.KANA_SAME || mode == SimonMode.KANA_CROSS else mode != SimonMode.KANA_SAME && mode != SimonMode.KANA_CROSS
                            if (isVisible) {
                                val label = when(mode) {
                                    SimonMode.KANJI -> stringResource(Res.string.game_simon_mode_kanji)
                                    SimonMode.MEANING -> stringResource(Res.string.game_simon_mode_meaning)
                                    SimonMode.READING_COMMON -> stringResource(Res.string.game_simon_mode_reading_std)
                                    SimonMode.READING_RANDOM -> stringResource(Res.string.game_simon_mode_reading_rnd)
                                    SimonMode.KANA_SAME -> stringResource(Res.string.game_simon_mode_kana_same)
                                    SimonMode.KANA_CROSS -> stringResource(Res.string.game_simon_mode_kana_cross)
                                }
                                FilterChip(
                                    selected = selectedMode == mode,
                                    onClick = { viewModel.onModeSelected(mode) },
                                    label = { Text(label) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // History Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.game_history_title), 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (scoresHistory.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.game_memorize_no_scores), 
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        scoresHistory.take(5).forEach { result ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                val modeLabel = when(result.mode) {
                                    SimonMode.KANJI -> stringResource(Res.string.game_simon_mode_kanji)
                                    SimonMode.MEANING -> stringResource(Res.string.game_simon_mode_meaning)
                                    SimonMode.READING_COMMON -> stringResource(Res.string.game_simon_mode_reading_std)
                                    SimonMode.READING_RANDOM -> stringResource(Res.string.game_simon_mode_reading_rnd)
                                    SimonMode.KANA_SAME -> stringResource(Res.string.game_simon_mode_kana_same)
                                    SimonMode.KANA_CROSS -> stringResource(Res.string.game_simon_mode_kana_cross)
                                }
                                Text(
                                    text = modeLabel, 
                                    fontSize = 12.sp, 
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${result.maxSequence}", 
                                    fontWeight = FontWeight.Bold, 
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f), 
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(Res.string.game_memorize_time_format, result.timeSeconds), 
                                    fontSize = 14.sp, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f), 
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            PlayButton(onClick = {
                viewModel.startGame()
                onStartGame()
            })
        }
    }
}

@Composable
fun SimonGameScreen(
    viewModel: SimonViewModel,
    onBackClick: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val currentPlayable by viewModel.currentPlayable.collectAsState(null)
    val isKanjiVisible by viewModel.isKanjiVisible.collectAsState()
    val isButtonsVisible by viewModel.isButtonsVisible.collectAsState()
    val answers by viewModel.answers.collectAsState()
    val score by viewModel.score.collectAsState()
    val scoresHistory by viewModel.scoresHistory.collectAsState()

    // Nettoyage automatique de la session lors de la fermeture de l'écran
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.abandonGame()
        }
    }

    val bestScore = remember(scoresHistory) {
        scoresHistory.maxOfOrNull { it.maxSequence } ?: 0
    }

    MochiBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HUD
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.TopEnd) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(Res.string.game_simon_score_label, score), 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(Res.string.game_simon_record_label, bestScore), 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Optimized Timer
                    TimerText(viewModel.gameTimeSeconds)
                }
            }

            // Main Area
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (gameState == SimonGameState.GAME_OVER) {
                    val finalTime by viewModel.gameTimeSeconds.collectAsState()
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(Res.string.game_simon_game_over), 
                                fontSize = 28.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(Res.string.game_simon_score_label, score), 
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(Res.string.game_simon_time_label, finalTime)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.startGame() }, 
                                modifier = Modifier.fillMaxWidth()
                            ) { 
                                Text(stringResource(Res.string.game_replay_button)) 
                            }
                            OutlinedButton(
                                onClick = onBackClick, 
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) { 
                                Text(stringResource(Res.string.game_menu_button)) 
                            }
                        }
                    }
                } else {
                    val alpha by animateFloatAsState(targetValue = if (isKanjiVisible) 1f else 0f, animationSpec = tween(300), label = "kanjiAlpha")
                    if (currentPlayable != null) {
                        Surface(modifier = Modifier.size(250.dp), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface.copy(alpha = alpha), shadowElevation = (8.dp.value * alpha).dp) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = currentPlayable?.character ?: "", fontSize = 120.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).alpha(if (isButtonsVisible) 1f else 0f)) {
                val row1 = answers.take(2)
                val row2 = answers.drop(2).take(2)
                Row(modifier = Modifier.fillMaxWidth()) {
                    row1.forEach { (playable, label) ->
                        GameAnswerButton(text = label, state = AnswerButtonState.DEFAULT, enabled = isButtonsVisible, modifier = Modifier.weight(1f).padding(4.dp), fontSizeSp = if (label.length > 4) 16 else 24, onClick = { viewModel.onAnswerClick(playable) })
                    }
                    if (row1.size < 2) Spacer(Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    row2.forEach { (playable, label) ->
                        GameAnswerButton(text = label, state = AnswerButtonState.DEFAULT, enabled = isButtonsVisible, modifier = Modifier.weight(1f).padding(4.dp), fontSizeSp = if (label.length > 4) 16 else 24, onClick = { viewModel.onAnswerClick(playable) })
                    }
                    if (row2.size < 2) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TimerText(timeFlow: StateFlow<Int>) {
    val timeSeconds by timeFlow.collectAsState()
    Text(
        text = stringResource(Res.string.game_simon_time_label, timeSeconds),
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}
