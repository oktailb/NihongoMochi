package org.nihongo.mochi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.shared.generated.resources.*

@Composable
fun GameResultOverlay(
    isVictory: Boolean,
    title: String = if (isVictory) stringResource(Res.string.game_success) else stringResource(Res.string.game_over),
    score: String? = null,
    bestScore: String? = null,
    stats: List<Pair<String, String>> = emptyList(),
    onReplayClick: () -> Unit,
    onMenuClick: () -> Unit,
    audioPlayer: org.nihongo.mochi.domain.services.AudioPlayer? = null
) {
    // Play sound based on result
    LaunchedEffect(isVictory) {
        audioPlayer?.let {
            if (isVictory) {
                it.playSound("sounds/victory.wav")
            } else {
                it.playSound("sounds/game_over.wav")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isVictory) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Score Area
                if (score != null) {
                    Text(
                        text = score,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(Res.string.game_score_label).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (bestScore != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.game_record_label, bestScore),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Additional Stats
                if (stats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    stats.forEach { (label, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = value, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Button(
                    onClick = onReplayClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.game_replay_button).uppercase(),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onMenuClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.game_menu_button).uppercase(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
