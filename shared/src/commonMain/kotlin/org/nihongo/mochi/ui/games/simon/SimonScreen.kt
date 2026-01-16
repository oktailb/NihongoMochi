package org.nihongo.mochi.ui.games.simon

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
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
import org.nihongo.mochi.domain.game.QuestionDirection
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.util.TextSizeCalculator
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameAnswerButton
import org.nihongo.mochi.ui.components.GameHUD
import org.nihongo.mochi.ui.components.GameResultOverlay
import org.nihongo.mochi.ui.components.GameSetupTemplate
import org.nihongo.mochi.ui.gojuon.QuizQuestionCard

@Composable
fun SimonSetupScreen(
    viewModel: SimonViewModel,
    onBackClick: () -> Unit,
    onStartGame: () -> Unit
) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val scoresHistory by viewModel.scoresHistory.collectAsState()
    val isKanaLevel by viewModel.isKanaLevel.collectAsState()

    GameSetupTemplate(
        title = stringResource(Res.string.game_simon_title),
        subtitle = stringResource(Res.string.game_simon_japanese_title),
        onPlayClick = {
            viewModel.startGame()
            onStartGame()
        }
    ) {
        // Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth(),
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
    }
}

private fun calculateFontSize(text: String, direction: QuestionDirection): Int {
    return TextSizeCalculator.calculateButtonTextSize(text.length, direction).toInt()
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
    val finalTime by viewModel.gameTimeSeconds.collectAsState()

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.abandonGame()
        }
    }

    val bestScore = remember(scoresHistory) {
        scoresHistory.maxOfOrNull { it.maxSequence } ?: 0
    }

    MochiBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Factorized HUD
                GameHUD(
                    primaryStat = stringResource(Res.string.game_simon_score_label, "") to score.toString(),
                    secondaryStat = stringResource(Res.string.game_simon_record_label, "") to bestScore.toString(),
                    timerFlow = viewModel.gameTimeSeconds,
                    initialTimerValue = finalTime
                )

                // Main Area
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val alpha by animateFloatAsState(
                        targetValue = if (isKanjiVisible) 1f else 0f, 
                        animationSpec = tween(300), 
                        label = "kanjiAlpha"
                    )
                    
                    Box(modifier = Modifier.alpha(alpha)) {
                        QuizQuestionCard(
                            direction = org.nihongo.mochi.domain.models.KanaQuestionDirection.NORMAL,
                            questionText = currentPlayable?.character ?: ""
                        )
                    }
                }

                // Buttons Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .alpha(if (isButtonsVisible) 1f else 0f)
                ) {
                    val chunks = answers.chunked(2)
                    chunks.forEach { rowAnswers ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowAnswers.forEach { (playable, label) ->
                                GameAnswerButton(
                                    text = label,
                                    state = AnswerButtonState.DEFAULT,
                                    enabled = isButtonsVisible,
                                    modifier = Modifier.weight(1f).padding(8.dp),
                                    fontSizeSp = calculateFontSize(label, QuestionDirection.REVERSE) * 3 / 2,
                                    onClick = { viewModel.onAnswerClick(playable) }
                                )
                            }
                            if (rowAnswers.size < 2) {
                                Spacer(Modifier.weight(1f).padding(8.dp))
                            }
                        }
                    }
                }
            }

            // Game Result Overlay
            if (gameState == SimonGameState.GAME_OVER) {
                GameResultOverlay(
                    isVictory = false,
                    score = score.toString(),
                    bestScore = bestScore.toString(),
                    stats = listOf(
                        stringResource(Res.string.game_taquin_time_label) to formatGameTime(finalTime)
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
