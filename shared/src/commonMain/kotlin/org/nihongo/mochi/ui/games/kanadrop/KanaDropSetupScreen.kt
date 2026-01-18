package org.nihongo.mochi.ui.games.kanadrop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.shared.generated.resources.*
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
                
                if (history.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.game_kana_link_no_history),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    history.take(5).forEach { result ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = result.levelId.uppercase().replace("JLPT_WORDLIST_", ""),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${result.score} pts",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(Res.string.game_kana_link_history_item_format, result.wordsFound),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
