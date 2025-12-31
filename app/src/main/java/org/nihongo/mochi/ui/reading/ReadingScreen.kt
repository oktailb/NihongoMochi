package org.nihongo.mochi.ui.reading

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.R
import org.nihongo.mochi.presentation.MochiBackground

data class ReadingLevelInfoState(
    val id: String,
    val displayName: String,
    val percentage: Int
)

@Composable
fun ReadingScreen(
    jlptLevels: List<ReadingLevelInfoState>,
    wordLevels: List<ReadingLevelInfoState>,
    userListInfo: ReadingLevelInfoState,
    onLevelClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.reading_title),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = stringResource(R.string.reading_subtitle),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // JLPT Section
            ReadingCard(title = stringResource(R.string.recognition_jlpt)) {
                // GridLayout 3 columns logic
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        jlptLevels.take(3).forEach { info ->
                            ReadingLevelButton(
                                info = info,
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        jlptLevels.drop(3).take(2).forEach { info ->
                            ReadingLevelButton(
                                info = info,
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                        }
                        // Fill empty space
                        Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                    }
                }
            }

            // Most Used Words Section
            ReadingCard(title = stringResource(R.string.writing_most_used_words)) {
                // GridLayout 2 columns logic
                Column {
                    for (i in wordLevels.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ReadingLevelButton(
                                info = wordLevels[i],
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                            if (i + 1 < wordLevels.size) {
                                ReadingLevelButton(
                                    info = wordLevels[i + 1],
                                    onClick = onLevelClick,
                                    modifier = Modifier.weight(1f).padding(4.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                            }
                        }
                    }
                }
            }

            // User Lists Section
            ReadingCard(title = stringResource(R.string.writing_user_lists)) {
                ReadingLevelButton(
                    info = userListInfo,
                    onClick = onLevelClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ReadingCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun ReadingLevelButton(
    info: ReadingLevelInfoState,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onClick(info.id) },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "${info.displayName}\n${info.percentage}%",
            textAlign = TextAlign.Center
        )
    }
}
