package org.nihongo.mochi.ui.games.memorize

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
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
import kotlin.math.roundToInt

@Composable
fun MemorizeSetupScreen(
    viewModel: MemorizeViewModel,
    onBackClick: () -> Unit,
    onStartGame: () -> Unit
) {
    val selectedGridSize by viewModel.selectedGridSize.collectAsState()
    val availableGridSizes by viewModel.availableGridSizes.collectAsState()
    val maxStrokes by viewModel.maxStrokes.collectAsState()
    val selectedMaxStrokes by viewModel.selectedMaxStrokes.collectAsState()
    val scoresHistory by viewModel.scoresHistory.collectAsState()
    val isKanaLevel by viewModel.isKanaLevel.collectAsState()
    val hasSavedGame by viewModel.hasSavedGame.collectAsState()

    GameSetupTemplate(
        title = stringResource(Res.string.game_memorize_title),
        subtitle = "神経衰弱",
        onPlayClick = {
            viewModel.startGame()
            onStartGame()
        }
    ) {
        // Option de restauration si une partie existe
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
                        text = "Une partie est en cours",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.restoreGame(onRestored = onStartGame)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reprendre la partie")
                    }
                    TextButton(
                        onClick = {
                            viewModel.startGame()
                            onStartGame()
                        }
                    ) {
                        Text("Nouvelle partie (effacer la précédente)")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Grid Size Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.game_memorize_grid_size),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    availableGridSizes.forEach { size ->
                        FilterChip(
                            selected = selectedGridSize == size,
                            onClick = { viewModel.onGridSizeSelected(size) },
                            label = { Text(stringResource(Res.string.game_memorize_grid_label, size.toString())) }
                        )
                    }
                }
            }
        }

        // Stroke Count Selection - Hidden if in Kana level
        if (!isKanaLevel) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.game_memorize_max_strokes, selectedMaxStrokes),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = selectedMaxStrokes.toFloat(),
                        onValueChange = { viewModel.onMaxStrokesChanged(it.roundToInt()) },
                        valueRange = 1f..maxStrokes.toFloat().coerceAtLeast(1f),
                        steps = (maxStrokes - 1).coerceAtLeast(0),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Recent Scores
        GameHistoryCard(
            history = scoresHistory,
            emptyMessage = stringResource(Res.string.game_memorize_no_scores)
        ) { result ->
            GameHistoryRow(
                label = stringResource(Res.string.game_memorize_grid_label, result.gridSizeLabel),
                score = stringResource(Res.string.game_memorize_score_format, result.moves),
                time = stringResource(Res.string.game_memorize_time_format, result.timeSeconds)
            )
        }
    }
}
