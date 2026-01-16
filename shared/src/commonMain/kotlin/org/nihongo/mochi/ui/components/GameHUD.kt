package org.nihongo.mochi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.shared.generated.resources.*

@Composable
fun GameHUD(
    primaryStat: Pair<String, String>,
    secondaryStat: Pair<String, String>? = null,
    timerFlow: Flow<Int>? = null,
    initialTimerValue: Int = 0,
    isCountDown: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        // Fond semi-transparent pour une meilleure visibilité sur le MochiBackground
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Stats
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (primaryStat.second.isEmpty()) primaryStat.first else "${primaryStat.first}: ${primaryStat.second}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    // Utilisation explicite de onSurface pour gérer le mode nuit (évite le noir forcé)
                    color = MaterialTheme.colorScheme.onSurface
                )
                secondaryStat?.let { (label, value) ->
                    Text(
                        text = if (label.isEmpty()) value else "$label: $value",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right Side: Timer
            timerFlow?.let { flow ->
                val time by flow.collectAsState(initial = initialTimerValue)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(Res.string.game_taquin_time_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (isCountDown) time.toString() else formatGameTime(time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCountDown && time < 10 && time > 0) Color.Red else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatGameTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
