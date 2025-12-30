package org.nihongo.mochi.ui.gojuon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.domain.game.KanaQuizViewModel
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanaQuestionDirection
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.components.GameAnswerButton
import org.nihongo.mochi.ui.components.GameProgressBar

@Composable
fun KanaQuizScreen(
    viewModel: KanaQuizViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val buttonStates by viewModel.buttonStates.collectAsState()

    val progressStatuses = viewModel.currentKanaSet.map {
        viewModel.kanaStatus[it] ?: org.nihongo.mochi.domain.models.GameStatus.NOT_ANSWERED
    }

    if (state == GameState.Finished) {
        onNavigateBack()
        return
    }

    MochiBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GameProgressBar(
                statuses = progressStatuses
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                QuizQuestionCard(
                    direction = viewModel.currentDirection,
                    questionText = if (viewModel.currentDirection == KanaQuestionDirection.NORMAL)
                        viewModel.currentQuestion.kana
                    else
                        viewModel.currentQuestion.romaji
                )
            }

            QuizAnswerGrid(
                answers = viewModel.currentAnswers,
                buttonStates = buttonStates,
                areButtonsEnabled = viewModel.areButtonsEnabled,
                direction = viewModel.currentDirection,
                onAnswerClick = { index, answer ->
                    viewModel.submitAnswer(answer, index)
                }
            )
        }
    }
}

@Composable
fun QuizQuestionCard(
    direction: KanaQuestionDirection,
    questionText: String
) {
    Card(
        modifier = Modifier
            .size(300.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val textSize = if (direction == KanaQuestionDirection.NORMAL) 200.sp else 120.sp
            Text(
                text = questionText,
                fontSize = textSize,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun QuizAnswerGrid(
    answers: List<String>,
    buttonStates: List<AnswerButtonState>,
    areButtonsEnabled: Boolean,
    direction: KanaQuestionDirection,
    onAnswerClick: (Int, String) -> Unit
) {
    val fontSize = if (direction == KanaQuestionDirection.REVERSE) 40 else 24

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            GameAnswerButton(
                text = answers.getOrNull(0) ?: "",
                state = buttonStates.getOrNull(0) ?: AnswerButtonState.DEFAULT,
                enabled = areButtonsEnabled,
                fontSizeSp = fontSize,
                modifier = Modifier.weight(1f).padding(8.dp),
                onClick = { onAnswerClick(0, answers.getOrNull(0) ?: "") }
            )
            GameAnswerButton(
                text = answers.getOrNull(1) ?: "",
                state = buttonStates.getOrNull(1) ?: AnswerButtonState.DEFAULT,
                enabled = areButtonsEnabled,
                fontSizeSp = fontSize,
                modifier = Modifier.weight(1f).padding(8.dp),
                onClick = { onAnswerClick(1, answers.getOrNull(1) ?: "") }
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            GameAnswerButton(
                text = answers.getOrNull(2) ?: "",
                state = buttonStates.getOrNull(2) ?: AnswerButtonState.DEFAULT,
                enabled = areButtonsEnabled,
                fontSizeSp = fontSize,
                modifier = Modifier.weight(1f).padding(8.dp),
                onClick = { onAnswerClick(2, answers.getOrNull(2) ?: "") }
            )
            GameAnswerButton(
                text = answers.getOrNull(3) ?: "",
                state = buttonStates.getOrNull(3) ?: AnswerButtonState.DEFAULT,
                enabled = areButtonsEnabled,
                fontSizeSp = fontSize,
                modifier = Modifier.weight(1f).padding(8.dp),
                onClick = { onAnswerClick(3, answers.getOrNull(3) ?: "") }
            )
        }
    }
}
