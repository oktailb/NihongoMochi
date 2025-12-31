package org.nihongo.mochi.ui.recognition

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

data class LevelInfoState(
    val levelKey: String,
    val displayName: String,
    val percentage: Int
)

@Composable
fun RecognitionScreen(
    levelInfos: List<LevelInfoState>,
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
                text = stringResource(R.string.recognition_title),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = stringResource(R.string.recognition_subtitle),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Fundamentals
            RecognitionCard(title = stringResource(R.string.recognition_fundamentals)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    LevelButton(
                        info = levelInfos.find { it.levelKey == "Hiragana" },
                        onClick = onLevelClick,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )
                    LevelButton(
                        info = levelInfos.find { it.levelKey == "Katakana" },
                        onClick = onLevelClick,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }
            }

            // JLPT
            RecognitionCard(title = stringResource(R.string.recognition_jlpt)) {
                val levels = listOf("N5", "N4", "N3", "N2", "N1")
                // GridLayout with 3 columns logic
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        levels.take(3).forEach { key ->
                            LevelButton(
                                info = levelInfos.find { it.levelKey == key },
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        levels.drop(3).take(2).forEach { key ->
                            LevelButton(
                                info = levelInfos.find { it.levelKey == key },
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                        }
                        // Fill empty space to keep buttons same width as first row
                        Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                    }
                }
            }

            // Primary School
            RecognitionCard(title = stringResource(R.string.recognition_primary_school)) {
                val levels = listOf(
                    "Grade 1", "Grade 2",
                    "Grade 3", "Grade 4",
                    "Grade 5", "Grade 6"
                )
                // GridLayout with 2 columns
                Column {
                    for (i in levels.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            LevelButton(
                                info = levelInfos.find { it.levelKey == levels[i] },
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                            if (i + 1 < levels.size) {
                                LevelButton(
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
            RecognitionCard(title = stringResource(R.string.recognition_high_school)) {
                val levels = listOf(
                    "Grade 7", "Grade 8",
                    "Grade 9", "Grade 10"
                )
                // GridLayout with 2 columns
                Column {
                    for (i in levels.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            LevelButton(
                                info = levelInfos.find { it.levelKey == levels[i] },
                                onClick = onLevelClick,
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                            if (i + 1 < levels.size) {
                                LevelButton(
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

            // Challenges
            RecognitionCard(title = stringResource(R.string.section_challenges)) {
                Column {
                    LevelButton(
                        info = levelInfos.find { it.levelKey == "Native Challenge" },
                        onClick = onLevelClick,
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        hidePercentage = true
                    )
                    LevelButton(
                        info = levelInfos.find { it.levelKey == "No Reading" },
                        onClick = onLevelClick,
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        hidePercentage = true
                    )
                    LevelButton(
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
fun RecognitionCard(
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
fun LevelButton(
    info: LevelInfoState?,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hidePercentage: Boolean = false
) {
    if (info == null) return

    // Original XML used:
    // android:textColor="?attr/colorOnSecondary"
    // android:backgroundTint="?attr/colorSecondary"
    // In Theme.kt:
    // LightSecondary = Color(0xFF8BC34A) (leaf_green)
    // DarkSecondary = Color(0xFF8BC34A) (leaf_green)
    //
    // However, the XML colors.xml suggests:
    // <color name="button_background">#4DB6AC</color> (Teal 400) for Day
    // <color name="button_background">#FF6D00</color> (Dark Orange) for Night
    //
    // If the original buttons were using ?attr/colorSecondary, then they were Green in both modes?
    // Let's re-read the original XML.
    // android:backgroundTint="?attr/colorSecondary"
    //
    // And let's check Theme.kt again.
    // LightSecondary = Color(0xFF8BC34A) // leaf_green
    // DarkSecondary = Color(0xFF8BC34A) // leaf_green
    //
    // Wait, the user said "les boutons on la mauvaise couleur , en particulier en mode nuit".
    // If I used Secondary (Green), it stays Green in Night mode.
    //
    // But colors.xml says:
    // Day: button_background = Teal 400
    // Night: button_background = Dark Orange
    //
    // And Theme.kt maps Primary to these colors!
    // LightPrimary = Color(0xFF4DB6AC) // button_background
    // DarkPrimary = Color(0xFFFF6D00) // button_background
    //
    // So if the intention is to follow the "button_background" color which changes from Teal to Orange,
    // we should use Primary, not Secondary.
    //
    // However, the original XML explicitly said: android:backgroundTint="?attr/colorSecondary"
    // Let's check if the original XML had overrides or if I misread it.
    //
    // <Button ... android:backgroundTint="?attr/colorSecondary" ... />
    //
    // If the original XML used Secondary, then the original buttons were Green (leaf_green) in both modes.
    // Unless the theme definition in themes.xml maps colorSecondary to something else.
    // I don't have access to themes.xml content right now, only file list.
    //
    // But Theme.kt seems to try to replicate the logic.
    //
    // Let's look at colors.xml again.
    // <color name="button_background">#4DB6AC</color>
    //
    // If the user complains about wrong color especially in night mode, maybe they expect the Orange color (DarkPrimary)
    // which is defined as "button_background" in the comment in Theme.kt.
    //
    // Let's switch to Primary color for buttons.
    // LightPrimary is Teal (Day button background)
    // DarkPrimary is Orange (Night button background)
    //
    // This seems to match the "button_background" comment in Theme.kt better than Secondary (leaf_green).

    Button(
        onClick = { onClick(info.levelKey) },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary, // Was Secondary, now Primary to match button_background
            contentColor = MaterialTheme.colorScheme.onPrimary // Was OnSecondary
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
