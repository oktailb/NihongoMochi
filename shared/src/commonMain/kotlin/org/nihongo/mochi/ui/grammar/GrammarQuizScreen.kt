package org.nihongo.mochi.ui.grammar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.domain.grammar.ExercisePayload
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.ui.components.GameAnswerButton
import org.nihongo.mochi.ui.components.GameProgressBar
import org.nihongo.mochi.ui.components.GameQuestionCard
import org.nihongo.mochi.ui.components.ExitConfirmationDialog
import org.nihongo.mochi.ui.components.GameResultOverlay
import org.nihongo.mochi.ui.theme.AppTheme
import org.nihongo.mochi.shared.generated.resources.*

@Composable
fun GrammarQuizScreen(
    viewModel: GrammarQuizViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    var showResultOverlay by remember { mutableStateOf(false) }

    BackHandler(enabled = state.gameState != GameState.Finished && !showResultOverlay) {
        showExitDialog = true
    }

    LaunchedEffect(state.gameState) {
        if (state.gameState == GameState.Finished) {
            showResultOverlay = true
        }
    }

    AppTheme {
        MochiBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameProgressBar(
                            statuses = state.progressHistory,
                            maxItems = 10
                        )
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    if (state.gameState == GameState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        QuizContent(state, viewModel)
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
                        isVictory = state.gameState == GameState.Finished,
                        score = "${(state.globalMasteryPercent * 100).toInt()}%",
                        stats = listOf(
                            stringResource(Res.string.game_result_title_session)  to "${(state.sessionMasteryPercent * 100).toInt()}%",
                            stringResource(Res.string.game_result_title_global) to "${(state.globalMasteryPercent * 100).toInt()}%"
                        ),
                        title = stringResource(Res.string.game_result_lot_mastery_grammar),
                        onReplayClick = {
                            showResultOverlay = false
                            viewModel.replay()
                        },
                        onMenuClick = {
                            showResultOverlay = false
                            onBackClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuizContent(state: GrammarQuizState, viewModel: GrammarQuizViewModel) {
    val payload = state.currentExercisePayload ?: return

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1/ Question area centered vertically with fixed size card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val questionText = when (payload) {
                is ExercisePayload.FillBlank -> payload.sentence.replace("__", " ___★___ ")
                is ExercisePayload.Underline -> payload.sentence.replace("[", "【").replace("]", "】")
                is ExercisePayload.Paraphrase -> payload.baseSentence
                is ExercisePayload.WordUsage -> "「${payload.word}」の使い方が正しいものを選んでください。"
                is ExercisePayload.SentenceOrder -> {
                    val holes = List(payload.blocks.size) { i -> if (i == state.currentStarIndex) " ★ " else " ___ " }
                    "${payload.prefix} ${holes.joinToString("")} ${payload.suffix}"
                }
            }

            GameQuestionCard(
                text = questionText,
                fontSize = if (payload is ExercisePayload.WordUsage || payload is ExercisePayload.Paraphrase) 18.sp else 22.sp,
                modifier = Modifier.size(width = 340.dp, height = 240.dp)
            )
        }

        // 2/ Answer buttons area at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            state.currentOptions.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { option ->
                        val isSelected = state.selectedOption == option
                        val isActuallyCorrect = checkIsCorrect(option, payload, state.currentStarIndex)
                        
                        val buttonState = when {
                            isSelected && state.isAnswerCorrect == true -> AnswerButtonState.CORRECT
                            isSelected && state.isAnswerCorrect == false -> AnswerButtonState.INCORRECT
                            state.selectedOption != null && isActuallyCorrect -> AnswerButtonState.CORRECT
                            else -> AnswerButtonState.DEFAULT
                        }
                        
                        GameAnswerButton(
                            text = option,
                            state = buttonState,
                            enabled = state.selectedOption == null,
                            modifier = Modifier.weight(1f).height(100.dp),
                            fontSizeSp = if (payload is ExercisePayload.WordUsage || payload is ExercisePayload.Paraphrase) 14 else 18,
                            onClick = { viewModel.onOptionSelected(option) }
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun checkIsCorrect(option: String, payload: ExercisePayload, starIndex: Int): Boolean {
    return when (payload) {
        is ExercisePayload.FillBlank -> option == payload.correct
        is ExercisePayload.Underline -> option == payload.correct
        is ExercisePayload.Paraphrase -> option == payload.correct
        is ExercisePayload.WordUsage -> payload.options.find { it.text == option }?.is_correct ?: false
        is ExercisePayload.SentenceOrder -> {
            if (starIndex < payload.blocks.size) {
                option == payload.blocks[starIndex]
            } else false
        }
    }
}
