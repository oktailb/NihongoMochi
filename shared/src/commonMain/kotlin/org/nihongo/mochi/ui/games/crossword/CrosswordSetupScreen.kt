package org.nihongo.mochi.ui.games.crossword

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameHistoryCard
import org.nihongo.mochi.ui.components.GameHistoryRow
import org.nihongo.mochi.ui.components.GameSetupTemplate

@Composable
fun CrosswordSetupScreen(
    viewModel: CrosswordViewModel,
    onBackClick: () -> Unit,
    onStartGame: () -> Unit
) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val wordCount by viewModel.wordCount.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val scoresHistory by viewModel.scoresHistory.collectAsState()
    val hasSavedGame: Boolean by viewModel.hasSavedGame.collectAsState(initial = false)

    GameSetupTemplate(
        title = stringResource(Res.string.game_crosswords_title),
        subtitle = "Mochi-Cross",
        onPlayClick = {
            viewModel.startGame(onGenerated = onStartGame)
        }
    ) {
        // Option de restauration
        if (hasSavedGame) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Une partie est en pause",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.restoreGame(onRestored = onStartGame)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reprendre la partie")
                    }
                    TextButton(
                        onClick = {
                            viewModel.startGame(onGenerated = onStartGame)
                        }
                    ) {
                        Text("Nouvelle partie (effacer la précédente)")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

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
        GameHistoryCard(
            history = scoresHistory,
            emptyMessage = stringResource(Res.string.game_memorize_no_scores)
        ) { result ->
            GameHistoryRow(
                label = stringResource(Res.string.game_crossword_history_item, result.wordCount, result.mode.name),
                score = "", 
                time = stringResource(Res.string.game_memorize_time_format, result.timeSeconds)
            )
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
