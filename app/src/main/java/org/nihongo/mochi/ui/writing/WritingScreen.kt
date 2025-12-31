package org.nihongo.mochi.ui.writing

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

data class WritingLevelInfoState(
    val levelKey: String,
    val displayName: String,
    val percentage: Int
)

@Composable
fun WritingScreen(
    levelInfos: List<WritingLevelInfoState>,
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
                text = stringResource(R.string.writing_title),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = stringResource(R.string.writing_subtitle),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // JLPT
            WritingCard(title = stringResource(R.string.recognition_jlpt)) {
                val levels = listOf("N5", "N4", "N3", "N2", "N1")
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        levels.take(3).forEach { key ->
                            WritingLevelButton(
                                info = levelInfos.find { it.levelKey == key },
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        levels.drop(3).take(2).forEach { key ->
                            WritingLevelButton(
                                info = levelInfos.find { it.levelKey == key },
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                    }
                }
            }

            // Primary School
            WritingCard(title = stringResource(R.string.recognition_primary_school)) {
                val levels = listOf(
                    "Grade 1", "Grade 2",
                    "Grade 3", "Grade 4",
                    "Grade 5", "Grade 6"
                )
                Column {
                    for (i in levels.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            WritingLevelButton(
                                info = levelInfos.find { it.levelKey == levels[i] },
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                            if (i + 1 < levels.size) {
                                WritingLevelButton(
                                    info = levelInfos.find { it.levelKey == levels[i + 1] },
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

            // High School
            WritingCard(title = stringResource(R.string.recognition_high_school)) {
                val levels = listOf(
                    "Grade 7", "Grade 8",
                    "Grade 9", "Grade 10"
                )
                Column {
                    for (i in levels.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            WritingLevelButton(
                                info = levelInfos.find { it.levelKey == levels[i] },
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                            if (i + 1 < levels.size) {
                                WritingLevelButton(
                                    info = levelInfos.find { it.levelKey == levels[i + 1] },
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

            // User Lists
            WritingCard(title = stringResource(R.string.writing_user_lists)) {
                WritingLevelButton(
                    info = levelInfos.find { it.levelKey == "user_custom_list" },
                    onClick = onLevelClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Challenges
            WritingCard(title = stringResource(R.string.section_challenges)) {
                Column {
                    WritingLevelButton(
                        info = levelInfos.find { it.levelKey == "Native Challenge" },
                        onClick = onLevelClick,
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        hidePercentage = true
                    )
                    WritingLevelButton(
                        info = levelInfos.find { it.levelKey == "No Reading" },
                        onClick = onLevelClick,
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        hidePercentage = true
                    )
                    WritingLevelButton(
                        info = levelInfos.find { it.levelKey == "No Meaning" },
                        onClick = onLevelClick,
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        hidePercentage = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun WritingCard(
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
fun WritingLevelButton(
    info: WritingLevelInfoState?,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hidePercentage: Boolean = false
) {
    if (info == null) return

    Button(
        onClick = { onClick(info.levelKey) },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary, // Using primary color (Teal/Orange) like Reading/Recognition
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        val text = if (hidePercentage) {
            info.displayName
        } else {
            "${info.displayName}\n${info.percentage}%"
        }
        
        Text(
            text = text,
            textAlign = TextAlign.Center
        )
    }
}
