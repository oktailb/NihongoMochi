package org.nihongo.mochi.ui.games.memorize

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.PlayButton
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

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.game_memorize_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
            )

            Text(
                text = "神経衰弱",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Grid Size Selection
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                                    text = stringResource(Res.string.game_memorize_grid_label, result.gridSizeLabel),
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = stringResource(Res.string.game_memorize_score_format, result.moves),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(Res.string.game_memorize_time_format, result.timeSeconds),
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PlayButton(onClick = {
                viewModel.startGame()
                onStartGame()
            })
        }
    }
}
