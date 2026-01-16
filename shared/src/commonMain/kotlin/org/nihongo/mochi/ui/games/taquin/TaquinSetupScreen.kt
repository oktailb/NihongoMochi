package org.nihongo.mochi.ui.games.taquin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameSetupTemplate

@Composable
fun TaquinSetupScreen(
    viewModel: TaquinViewModel,
    onBackClick: () -> Unit,
    onStartGame: () -> Unit
) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val selectedRows by viewModel.selectedRows.collectAsState()
    val scoresHistory by viewModel.scoresHistory.collectAsState()

    GameSetupTemplate(
        title = stringResource(Res.string.game_taquin_title),
        subtitle = stringResource(Res.string.game_taquin_japanese_title),
        onPlayClick = {
            viewModel.startGame()
            onStartGame()
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
                    text = stringResource(Res.string.game_taquin_mode_label),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TaquinMode.entries.forEach { mode ->
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

        // Grid Size Selection (Rows)
        if (selectedMode != TaquinMode.NUMBERS) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.game_taquin_rows_label, selectedRows),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = selectedRows.toFloat(),
                        onValueChange = { viewModel.onRowsSelected(it.toInt()) },
                        valueRange = 2f..10f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
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
                            Text(
                                text = stringResource(Res.string.game_taquin_score_history_label, result.mode.name, result.rows),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = stringResource(Res.string.game_memorize_score_format, result.moves),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(Res.string.game_memorize_time_format, result.timeSeconds),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
