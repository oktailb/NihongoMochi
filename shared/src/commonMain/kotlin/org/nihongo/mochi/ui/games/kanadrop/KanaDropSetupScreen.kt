package org.nihongo.mochi.ui.games.kanadrop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameHistoryCard
import org.nihongo.mochi.ui.components.GameHistoryRow
import org.nihongo.mochi.ui.components.GameSetupTemplate

@Composable
fun KanaDropSetupScreen(
    viewModel: KanaDropViewModel,
    onBackClick: () -> Unit,
    onStartGame: (KanaLinkMode) -> Unit
) {
    val history by viewModel.history.collectAsState()
    var selectedMode by remember { mutableStateOf(KanaLinkMode.TIME_ATTACK) }

    GameSetupTemplate(
        title = stringResource(Res.string.game_kana_link_title),
        subtitle = "カナリンク",
        onPlayClick = {
            onStartGame(selectedMode)
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
                    text = stringResource(Res.string.game_config_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KanaLinkMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(mode.name.replace("_", " ")) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Recent Scores
        GameHistoryCard(
            history = history,
            emptyMessage = stringResource(Res.string.game_memorize_no_scores)
        ) { result ->
            GameHistoryRow(
                label = result.levelId.uppercase(),
                score = result.score.toString(),
                time = stringResource(Res.string.game_memorize_time_format, result.timeSeconds)
            )
        }
    }
}
