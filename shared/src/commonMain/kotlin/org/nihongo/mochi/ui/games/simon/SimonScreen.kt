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
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameAnswerButton
import org.nihongo.mochi.ui.components.PlayButton

@Composable
fun SimonScreen(
    viewModel: SimonViewModel,
    onBackClick: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val currentKanji by viewModel.currentKanji.collectAsState()
    val isKanjiVisible by viewModel.isKanjiVisible.collectAsState()
    val isButtonsVisible by viewModel.isButtonsVisible.collectAsState()
    val answers by viewModel.answers.collectAsState()
    val score by viewModel.score.collectAsState()
    val scoresHistory by viewModel.scoresHistory.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val timeSeconds by viewModel.gameTimeSeconds.collectAsState()

    // Dynamically find the best score for the current level
    val bestScore = remember(scoresHistory) {
        scoresHistory.maxOfOrNull { it.maxSequence } ?: 0
    }

    MochiBackground {
        if (gameState == SimonGameState.IDLE) {
            SimonSetupContent(
                selectedMode = selectedMode,
                scoresHistory = scoresHistory,
                onModeSelected = { viewModel.onModeSelected(it) },
                onStartGame = { viewModel.startGame() }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HUD
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(48.dp))
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(Res.string.game_memorize_score_format, score),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(Res.string.game_simon_record, bestScore),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(Res.string.game_memorize_time, timeSeconds),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Main Area
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (gameState == SimonGameState.GAME_OVER) {
                        SimonGameOverOverlay(score, timeSeconds, onRestart = { viewModel.startGame() }, onBack = { viewModel.resetToIdle() })
                    } else {
                        val alpha by animateFloatAsState(
                            targetValue = if (isKanjiVisible) 1f else 0f,
                            animationSpec = tween(durationMillis = 300)
                        )
                        
                        if (currentKanji != null) {
                            Surface(
                                modifier = Modifier.size(250.dp),
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
                                shadowElevation = 8.dp * alpha
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentKanji?.character ?: "",
                                        fontSize = 120.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .alpha(if (isButtonsVisible) 1f else 0f)
                ) {
                    val row1 = answers.take(2)
                    val row2 = answers.drop(2).take(2)

                    Row(modifier = Modifier.fillMaxWidth()) {
                        row1.forEach { (kanji, label) ->
                            GameAnswerButton(
                                text = label,
                                state = AnswerButtonState.DEFAULT,
                                enabled = isButtonsVisible,
                                modifier = Modifier.weight(1f).padding(4.dp),
                                fontSizeSp = if (label.length > 4) 16 else 24,
                                onClick = { viewModel.onAnswerClick(kanji) }
                            )
                        }
                        if (row1.size < 2) Spacer(Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row2.forEach { (kanji, label) ->
                            GameAnswerButton(
                                text = label,
                                state = AnswerButtonState.DEFAULT,
                                enabled = isButtonsVisible,
                                modifier = Modifier.weight(1f).padding(4.dp),
                                fontSizeSp = if (label.length > 4) 16 else 24,
                                onClick = { viewModel.onAnswerClick(kanji) }
                            )
                        }
                        if (row2.size < 2) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimonSetupContent(
    selectedMode: SimonMode,
    scoresHistory: List<SimonGameResult>,
    onModeSelected: (SimonMode) -> Unit,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
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
            text = "記憶",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Mode Selection
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.game_simon_btn_content),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SimonMode.entries.forEach { mode ->
                        val label = when(mode) {
                            SimonMode.KANJI -> stringResource(Res.string.game_simon_mode_kanji)
                            SimonMode.MEANING -> stringResource(Res.string.game_simon_mode_meaning)
                            SimonMode.READING_COMMON -> stringResource(Res.string.game_simon_mode_reading_std)
                            SimonMode.READING_RANDOM -> stringResource(Res.string.game_simon_mode_reading_rnd)
                        }
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { onModeSelected(mode) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        // Scores History
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.game_memorize_recent_scores),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (scoresHistory.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.game_memorize_no_scores),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    scoresHistory.take(5).forEach { result ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val modeLabel = when(result.mode) {
                                SimonMode.KANJI -> stringResource(Res.string.game_simon_mode_kanji)
                                SimonMode.MEANING -> stringResource(Res.string.game_simon_mode_meaning)
                                SimonMode.READING_COMMON -> stringResource(Res.string.game_simon_mode_reading_std)
                                SimonMode.READING_RANDOM -> stringResource(Res.string.game_simon_mode_reading_rnd)
                            }
                            Text(
                                text = modeLabel,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(Res.string.game_simon_max_sequence, result.maxSequence).split(":")[1].trim(),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(Res.string.game_memorize_time_format, result.timeSeconds),
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        PlayButton(onClick = onStartGame)
    }
}

@Composable
fun SimonGameOverOverlay(
    score: Int,
    timeSeconds: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.game_simon_game_over),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.game_simon_max_sequence, score),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(Res.string.game_memorize_time, timeSeconds),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.game_memorize_replay))
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(stringResource(Res.string.submit)) // Reusing 'Submit' or generic Back label
            }
        }
    }
}
