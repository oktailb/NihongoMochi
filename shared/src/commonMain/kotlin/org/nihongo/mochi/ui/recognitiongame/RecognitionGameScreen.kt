package org.nihongo.mochi.ui.recognitiongame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.data.LearningScore
import org.nihongo.mochi.domain.game.QuestionDirection
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.util.TextSizeCalculator
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.components.GameAnswerButton
import org.nihongo.mochi.ui.components.GameProgressBar
import org.nihongo.mochi.ui.components.GameQuestionCard
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
    gameMode: String,
    currentScore: LearningScore?,
    onAnswerClick: (Int, String) -> Unit
) {
    AppTheme {
        MochiBackground {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Progress Bar
                GameProgressBar(
                    statuses = gameStatus,
                    maxItems = 10
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kanji Card Area
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (kanji != null && questionText != null) {
                        val fontSize = if (direction == QuestionDirection.NORMAL) {
                            200f
                        } else {
                            val lineCount = questionText.count { it == '\n' } + 1
                            TextSizeCalculator.calculateQuestionTextSize(questionText.length, lineCount, direction)
                        }

                        val secondaryInfo = if (gameMode == "meaning") {
                            formatReadingsForFlip(kanji)
                        } else {
                            formatMeaningsForFlip(kanji)
                        }
                        
                        GameQuestionCard(
                            text = questionText,
                            fontSize = fontSize.sp,
                            secondaryText = secondaryInfo,
                            secondaryFontSize = 24.sp,
                            modifier = Modifier.size(300.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Kanji Score Box
                        if (currentScore != null) {
                            ScoreDisplayBox(currentScore)
                        }
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
                                fontSizeSp = calculateFontSize(row1[0], direction) * 3 / 2,
                                onClick = { onAnswerClick(0, row1[0]) }
                            )
                            if (row1.size > 1) {
                                GameAnswerButton(
                                    text = row1[1],
                                    state = getState(1),
                                    enabled = buttonsEnabled,
                                    modifier = Modifier.weight(1f).padding(4.dp),
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
                                modifier = Modifier.weight(1f).padding(4.dp),
                                fontSizeSp = calculateFontSize(row2[0], direction) * 3 / 2,
                                onClick = { onAnswerClick(2, row2[0]) }
                            )
                            if (row2.size > 1) {
                                GameAnswerButton(
                                    text = row2[1],
                                    state = getState(3),
                                    enabled = buttonsEnabled,
                                    modifier = Modifier.weight(1f).padding(4.dp),
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
fun ScoreDisplayBox(score: LearningScore) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = score.successes.toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = score.failures.toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

private fun formatReadingsForFlip(kanji: KanjiDetail): String {
    val onReadings = kanji.readings.filter { it.type == "on" }.take(2).map { it.value }
    val kunReadings = kanji.readings.filter { it.type == "kun" }.take(2).map { it.value }
    
    return buildString {
        if (onReadings.isNotEmpty()) {
            append("ON:\n")
            append(onReadings.joinToString("\n"))
        }
        if (onReadings.isNotEmpty() && kunReadings.isNotEmpty()) {
            append("\n\n")
        }
        if (kunReadings.isNotEmpty()) {
            append("KUN:\n")
            append(kunReadings.joinToString("\n"))
        }
    }
}

private fun formatMeaningsForFlip(kanji: KanjiDetail): String {
    return kanji.meanings.take(4).joinToString("\n\n")
}

private fun calculateFontSize(text: String, direction: QuestionDirection): Int {
    return TextSizeCalculator.calculateButtonTextSize(text.length, direction).toInt()
}
