package org.nihongo.mochi.ui.games.shiritori

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun ShiritoriSetupScreen(
    viewModel: ShiritoriViewModel,
    onBackClick: () -> Unit,
    onStartGame: () -> Unit
) {
    val scoresHistory by viewModel.scoresHistory.collectAsState()
    val hasSavedGame by viewModel.hasSavedGame.collectAsState()

    // Auto-restauration
    LaunchedEffect(Unit) {
        viewModel.tryAutoRestore(onRestored = onStartGame)
    }

    GameSetupTemplate(
        title = stringResource(Res.string.game_shiritori_title),
        subtitle = "しりとり",
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

        // --- Rules ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.shiritori_rules),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(Res.string.shiritori_rules_desc), 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- Recent Scores ---
        GameHistoryCard(
            history = scoresHistory,
            emptyMessage = stringResource(Res.string.game_kana_link_no_history)
        ) { result ->
            GameHistoryRow(
                label = result.levelId.uppercase(),
                score = "${result.score} words",
                time = stringResource(Res.string.game_memorize_time_format, result.timeSeconds)
            )
        }
    }
}
