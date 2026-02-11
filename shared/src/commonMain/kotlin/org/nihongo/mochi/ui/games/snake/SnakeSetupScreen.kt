package org.nihongo.mochi.ui.games.snake

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SnakeSetupScreen(
    viewModel: SnakeViewModel,
    onBackClick: () -> Unit,
    onStartGame: () -> Unit
) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val scoresHistory by viewModel.scoresHistory.collectAsState()
    val gameState by viewModel.gameState.collectAsState()

    // On considère qu'une partie est sauvegardée si le label est rempli et score > 0 (simplification)
    // En réalité, on va vérifier via le ViewModel
    val hasSavedGame by viewModel.hasSavedGame.collectAsState()

    GameSetupTemplate(
        title = stringResource(Res.string.game_snake_title),
        subtitle = stringResource(Res.string.game_snake_japanese_title),
        onPlayClick = {
            viewModel.startGame()
            onStartGame()
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
                    text = stringResource(Res.string.game_taquin_mode_label),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SnakeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { viewModel.onModeSelected(mode) },
                            label = { Text(mode.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // Recent Scores
        GameHistoryCard(
            history = scoresHistory,
            emptyMessage = stringResource(Res.string.game_memorize_no_scores)
        ) { result ->
            GameHistoryRow(
                label = result.mode.name,
                score = stringResource(Res.string.game_snake_score, result.score),
                time = stringResource(Res.string.game_memorize_time_format, result.timeSeconds)
            )
        }
    }
}
