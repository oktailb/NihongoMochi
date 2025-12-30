package org.nihongo.mochi.ui.writinggame

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.R
import org.nihongo.mochi.domain.game.QuestionType
import org.nihongo.mochi.domain.kana.KanaUtils
import org.nihongo.mochi.domain.kana.RomajiToKana
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.components.GameProgressBar
import org.nihongo.mochi.ui.theme.AppTheme

@Composable
fun WritingGameScreen(
    kanji: KanjiDetail?,
    questionType: QuestionType,
    gameStatus: List<GameStatus>,
    onSubmitAnswer: (String) -> Unit,
    showCorrection: Boolean,
    isCorrect: Boolean?,
    processingAnswer: Boolean
) {
    var answerText by remember(kanji, questionType) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    AppTheme {
        MochiBackground {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Progress Bar
                    GameProgressBar(
                        statuses = gameStatus,
                        maxItems = 10
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kanji Card (Top part)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (kanji != null) {
                            KanjiCard(text = kanji.character)
                        }
                    }

                    // Input Area (Bottom part)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = if (questionType == QuestionType.MEANING) 
                                    stringResource(R.string.game_writing_label_meaning) 
                                else 
                                    stringResource(R.string.game_writing_label_reading),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = answerText,
                            onValueChange = { newValue ->
                                if (!processingAnswer) {
                                    if (questionType == QuestionType.READING) {
                                        // Auto-convert Romaji to Kana
                                        val replacement = RomajiToKana.checkReplacement(newValue)
                                        if (replacement != null) {
                                            val (suffixLen, kana) = replacement
                                            val start = newValue.length - suffixLen
                                            val prefix = newValue.substring(0, start)
                                            answerText = prefix + kana
                                        } else {
                                            answerText = newValue
                                        }
                                    } else {
                                        answerText = newValue
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            keyboardOptions = KeyboardOptions(
                                // Use Password type to disable suggestions/clipboard strip
                                keyboardType = KeyboardType.Password, 
                                imeAction = ImeAction.Done,
                                autoCorrect = false
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (answerText.isNotBlank() && !processingAnswer) {
                                        onSubmitAnswer(answerText)
                                    }
                                }
                            ),
                            enabled = !processingAnswer,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val buttonColor = when {
                            processingAnswer && isCorrect == true -> MaterialTheme.colorScheme.primary 
                            processingAnswer && isCorrect == false -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.secondary
                        }

                        Button(
                            onClick = { 
                                if (answerText.isNotBlank() && !processingAnswer) {
                                    onSubmitAnswer(answerText)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !processingAnswer && answerText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                        ) {
                            Text(stringResource(R.string.submit))
                        }
                    }
                }

                // Correction Popup (Floating)
                AnimatedVisibility(
                    visible = showCorrection && kanji != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    if (kanji != null) {
                        CorrectionCard(
                            kanji = kanji,
                            questionType = questionType
                        )
                    }
                }
            }
        }
    }
    
    // Request focus when question changes
    LaunchedEffect(kanji, questionType) {
        if (!processingAnswer) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun KanjiCard(text: String) {
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
            Text(
                text = text,
                fontSize = 180.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CorrectionCard(
    kanji: KanjiDetail,
    questionType: QuestionType
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer, 
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.game_writing_correction),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            if (questionType == QuestionType.MEANING) {
                Text(
                    text = kanji.meanings.joinToString(", "),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // ON Readings
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.game_writing_on),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        val onReadings = kanji.readings
                            .filter { it.type == "on" }
                            .joinToString("\n") { KanaUtils.hiraganaToKatakana(it.value) }
                        Text(
                            text = if (onReadings.isNotEmpty()) onReadings else "-",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier
                            .height(60.dp) 
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outline
                    )

                    // KUN Readings
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.game_writing_kun),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        val kunReadings = kanji.readings
                            .filter { it.type == "kun" }
                            .joinToString("\n") { it.value }
                        Text(
                            text = if (kunReadings.isNotEmpty()) kunReadings else "-",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
