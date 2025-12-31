package org.nihongo.mochi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.domain.models.AnswerButtonState
import org.nihongo.mochi.domain.models.GameStatus

@Composable
fun GameProgressBar(
    statuses: List<GameStatus>,
    maxItems: Int = 10
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 4.dp,
        color = Color.White.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 0 until maxItems) {
                val status = statuses.getOrNull(i)
                if (status != null) {
                    val iconRes = when (status) {
                        GameStatus.CORRECT -> android.R.drawable.presence_online
                        GameStatus.INCORRECT -> android.R.drawable.ic_delete
                        GameStatus.PARTIAL -> android.R.drawable.ic_menu_recent_history
                        else -> android.R.drawable.checkbox_off_background
                    }

                    val tint = when (status) {
                        GameStatus.CORRECT -> Color(0xFF66BB6A) // Green 400 for consistency with buttons
                        GameStatus.INCORRECT -> MaterialTheme.colorScheme.error
                        GameStatus.PARTIAL -> Color(0xFFFFA500) // Orangeish for Partial
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) // Faded grey for unanswered
                    }

                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(tint),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp) // Enforce fixed height for stable alignment
                            .padding(2.dp)
                    )
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun GameAnswerButton(
    text: String,
    state: AnswerButtonState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    fontSizeSp: Int = 24,
    onClick: () -> Unit
) {
    val backgroundColor = when (state) {
        AnswerButtonState.CORRECT -> Color(0xFF66BB6A) // Green 400
        AnswerButtonState.INCORRECT -> Color(0xFFEF5350) // Red 400
        AnswerButtonState.NEUTRAL -> Color(0xFFFFCA28) // Amber 400
        AnswerButtonState.DEFAULT -> MaterialTheme.colorScheme.primary // Use theme primary for default state
    }

    val contentColor = if (state == AnswerButtonState.DEFAULT) {
        MaterialTheme.colorScheme.onPrimary // Use theme onPrimary for default state
    } else {
        Color.White // Use plain white for colored feedback states
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp), // Less rounded corners
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
            contentColor = contentColor,
            disabledContentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp,
            disabledElevation = 4.dp // Keep some elevation for result state
        ),
        modifier = modifier.height(120.dp)
    ) {
        Text(text = text, fontSize = fontSizeSp.sp)
    }
}

@Composable
fun GameQuestionCard(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                lineHeight = (fontSize.value * 1.2).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
