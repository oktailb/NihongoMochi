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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    maxItems = 10
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
                        val fontSize = if (direction == QuestionDirection.NORMAL) {
                            200f
                        } else {
                            val lineCount = questionText.count { it == '\n' } + 1
                            TextSizeCalculator.calculateQuestionTextSize(questionText.length, lineCount, direction)
                        }

                        // Updated logic based on user request:
                        // Mode Meaning -> Flip ALWAYS shows Readings
                        // Mode Reading -> Flip ALWAYS shows Meanings
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
