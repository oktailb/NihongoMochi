package org.nihongo.mochi.ui.games.kanadrop

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameHistoryCard
import org.nihongo.mochi.ui.components.GameHistoryRow
import org.nihongo.mochi.ui.components.GameSetupTemplate

@Composable
fun KanaDropSetupScreen(
    viewModel: KanaDropViewModel,
    levelId: String,
    onStartGame: (KanaLinkMode) -> Unit,
    onBackClick: () -> Unit
) {
    val history by viewModel.history.collectAsState()
    var selectedMode by remember { mutableStateOf(KanaLinkMode.TIME_ATTACK) }
    val hasSavedGame: Boolean by viewModel.hasSavedGame.collectAsState(initial = false)
    val state by viewModel.state.collectAsState()

    GameSetupTemplate(
        title = stringResource(Res.string.game_kana_link_title),
        subtitle = "カナリンク",
        onPlayClick = {
            viewModel.initGame(levelId, selectedMode)
            onStartGame(selectedMode)
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
                            viewModel.restoreGame(onRestored = {
                                state.config?.mode?.let { mode ->
                                    onStartGame(mode)
                                } ?: onStartGame(KanaLinkMode.TIME_ATTACK)
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reprendre la partie")
                    }
                    TextButton(
                        onClick = {
                            viewModel.initGame(levelId, selectedMode)
                            onStartGame(selectedMode)
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
                    text = stringResource(Res.string.game_kana_link_mode_settings),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    KanaLinkMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { 
                                Text(if (mode == KanaLinkMode.TIME_ATTACK) 
                                    stringResource(Res.string.game_kana_link_time_attack_title) 
                                    else stringResource(Res.string.game_kana_link_survival_title)) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                
                Text(
                    text = if (selectedMode == KanaLinkMode.TIME_ATTACK) 
                        stringResource(Res.string.game_kana_link_time_attack_desc)
                        else stringResource(Res.string.game_kana_link_survival_desc),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Recent Scores
        GameHistoryCard(
            history = history,
            emptyMessage = stringResource(Res.string.game_kana_link_no_history)
        ) { result ->
            GameHistoryRow(
                label = result.levelId.uppercase().replace("JLPT_WORDLIST_", ""),
                score = "${result.score} pts",
                scoreSubtitle = stringResource(Res.string.game_kana_link_history_item_format, result.wordsFound),
                time = stringResource(Res.string.game_memorize_time_format, result.timeSeconds)
            )
        }
    }
}
