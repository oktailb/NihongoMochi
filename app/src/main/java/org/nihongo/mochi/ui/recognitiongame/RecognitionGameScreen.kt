package org.nihongo.mochi.ui.recognitiongame

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.domain.game.QuestionDirection
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.util.TextSizeCalculator
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.components.GameAnswerButton
import org.nihongo.mochi.ui.components.GameProgressBar
import org.nihongo.mochi.ui.theme.AppTheme

@Composable
fun RecognitionGameScreen(
    kanji: KanjiDetail?,
    questionText: String?,
    gameStatus: List<GameStatus>,
    answers: List<String>,
    buttonStates: List<AnswerButtonState>,
    buttonsEnabled: Boolean,
    direction: QuestionDirection,
    onAnswerClick: (Int, String) -> Unit
) {
    AppTheme {
        MochiBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Progress Bar
                GameProgressBar(
                    statuses = gameStatus,
                    maxItems = 10 // Assuming 10 items for now, can be dynamic
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kanji Card Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (kanji != null && questionText != null) {
                        KanjiCard(
                            text = questionText,
                            direction = direction
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
                    
                    // Helper to get button state safely
                    fun getState(index: Int) = buttonStates.getOrElse(index) { AnswerButtonState.DEFAULT }

                    if (row1.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            GameAnswerButton(
                                text = row1[0],
                                state = getState(0),
                                enabled = buttonsEnabled,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp),
                                fontSizeSp = calculateFontSize(row1[0], direction) * 3 / 2,
                                onClick = { onAnswerClick(0, row1[0]) }
                            )
                            if (row1.size > 1) {
                                GameAnswerButton(
                                    text = row1[1],
                                    state = getState(1),
                                    enabled = buttonsEnabled,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    fontSizeSp = calculateFontSize(row1[1], direction) * 3 / 2,
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
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp),
                                fontSizeSp = calculateFontSize(row2[0], direction) * 3 / 2,
                                onClick = { onAnswerClick(2, row2[0]) }
                            )
                            if (row2.size > 1) {
                                GameAnswerButton(
                                    text = row2[1],
                                    state = getState(3),
                                    enabled = buttonsEnabled,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    fontSizeSp = calculateFontSize(row2[1], direction) * 3 / 2,
                                    onClick = { onAnswerClick(3, row2[1]) }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KanjiCard(
    text: String,
    direction: QuestionDirection
) {
    Card(
        modifier = Modifier.size(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val fontSize = if (direction == QuestionDirection.NORMAL) {
                180f
            } else {
                val lineCount = text.count { it == '\n' } + 1
                TextSizeCalculator.calculateQuestionTextSize(text.length, lineCount, direction)
            }
            
            Text(
                text = text,
                fontSize = fontSize.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = (fontSize * 1.2).sp
            )
        }
    }
}

private fun calculateFontSize(text: String, direction: QuestionDirection): Int {
    return TextSizeCalculator.calculateButtonTextSize(text.length, direction).toInt()
}
