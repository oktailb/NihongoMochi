package org.nihongo.mochi.ui.grammar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.domain.grammar.ExercisePayload
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.components.GameAnswerButton
import org.nihongo.mochi.ui.components.GameProgressBar
import org.nihongo.mochi.ui.components.GameQuestionCard

@Composable
fun GrammarQuizScreen(
    viewModel: GrammarQuizViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Auto-close when finished
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            onBackClick()
        }
    }

    MochiBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    GameProgressBar(
                        statuses = state.progressHistory,
                        maxItems = state.exercises.size.coerceAtLeast(1)
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (!state.isFinished) {
                    QuizContent(state, viewModel)
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
