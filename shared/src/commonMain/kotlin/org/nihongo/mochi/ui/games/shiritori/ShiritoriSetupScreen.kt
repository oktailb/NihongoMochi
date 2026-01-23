package org.nihongo.mochi.ui.games.shiritori

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    GameSetupTemplate(
        title = stringResource(Res.string.game_shiritori_title),
        subtitle = "しりとり",
        onPlayClick = {
            onStartGame()
        }
    ) {
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
