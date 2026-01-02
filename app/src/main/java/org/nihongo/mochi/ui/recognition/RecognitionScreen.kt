package org.nihongo.mochi.ui.recognition

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.R
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.presentation.models.LevelInfoState
import org.nihongo.mochi.presentation.recognition.RecognitionCategory

@Composable
fun RecognitionScreen(
    categories: List<RecognitionCategory>,
    onLevelClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Helper to resolve string resource dynamically
    fun getCategoryTitle(key: String): String {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else key
    }

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.recognition_title),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = stringResource(R.string.recognition_subtitle),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Dynamic Categories from JSON
            categories.forEach { category ->
                RecognitionCard(title = getCategoryTitle(category.name)) {
                    // GridLayout logic (2 columns for example, or 3 if space permits or configured)
                    // For now, let's stick to 2 columns for general layout, or smart wrap
                    // The old code had 3 columns for JLPT, 2 for others.
                    // We can try to be adaptive or just stick to 2.
                    
                    Column {
                        val items = category.levels
                        val columns = if (items.size >= 5) 3 else 2 // Heuristic: if many items, use 3 columns (like JLPT)
                        
                        val rows = (items.size + columns - 1) / columns
                        for (r in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (c in 0 until columns) {
                                    val index = r * columns + c
                                    if (index < items.size) {
                                        val level = items[index]
                                        LevelButton(
                                            info = level,
                                            onClick = onLevelClick,
                                            modifier = Modifier.weight(1f).padding(4.dp),
                                            // Heuristic for hidePercentage: Challenges usually hide it or if explicitly set.
                                            // The old code hardcoded hidePercentage for Challenges.
                                            // We don't have this info in LevelInfoState yet.
                                            // For now, let's show it unless levelKey suggests otherwise or if percentage is 0 and it's a challenge?
                                            // The old code used hardcoded section check.
                                            // Let's assume we show it. Or check category.name contains "challenge"
                                            hidePercentage = category.name.contains("challenge", ignoreCase = true)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RecognitionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun LevelButton(
    info: LevelInfoState,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hidePercentage: Boolean = false
) {
    Button(
        onClick = { onClick(info.levelKey) },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        val text = if (hidePercentage) {
            info.displayName
        } else {
            "${info.displayName}\n${info.percentage}%"
        }
        
        Text(
            text = text,
            textAlign = TextAlign.Center
        )
    }
}
