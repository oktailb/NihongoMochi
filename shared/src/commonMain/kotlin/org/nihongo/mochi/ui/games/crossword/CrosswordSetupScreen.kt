package org.nihongo.mochi.ui.games.crossword

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameSetupTemplate
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun CrosswordSetupScreen(
    viewModel: CrosswordViewModel,
    onBackClick: () -> Unit,
    onStartGame: () -> Unit
) {
    val selectedMode by viewModel.selectedMode.collectAsState(CrosswordMode.KANAS)
    val wordCount by viewModel.wordCount.collectAsState(10)
    val isGenerating by viewModel.isGenerating.collectAsState(false)
    val scoresHistory by viewModel.scoresHistory.collectAsState(emptyList())

    GameSetupTemplate(
        title = stringResource(Res.string.game_crosswords_title),
        subtitle = "Mochi-Cross",
        onPlayClick = {
            viewModel.startGame(onGenerated = onStartGame)
        }
    ) {
        // Mode Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.game_crossword_setup_mode),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CrosswordMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { viewModel.onModeSelected(mode) },
                            label = { Text(mode.name) }
                        )
                    }
                }
            }
        }

        // Word Count Selection (Slider)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.game_crossword_setup_word_count, wordCount),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = wordCount.toFloat(),
                    onValueChange = { viewModel.onWordCountSelected(it.toInt()) },
                    valueRange = 5f..42f,
                    steps = 36
                )
            }
        }

        // Recent Scores
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.game_memorize_recent_scores),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (scoresHistory.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.game_memorize_no_scores),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    scoresHistory.take(5).forEach { result ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(Res.string.game_crossword_history_item, result.wordCount, result.mode.name),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                val date = Instant.fromEpochMilliseconds(result.timestamp)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                Text(
                                    text = "${date.dayOfMonth}/${date.monthNumber} ${date.hour}:${date.minute.toString().padStart(2, '0')}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatTime(result.timeSeconds),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        if (isGenerating) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
            Text(
                text = stringResource(Res.string.game_crossword_generating), 
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
