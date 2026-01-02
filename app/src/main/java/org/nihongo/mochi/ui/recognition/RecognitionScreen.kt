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
                    Column {
                        for (i in category.levels.indices step 2) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                RecognitionLevelButton(
                                    info = category.levels[i],
                                    onClick = onLevelClick,
                                    modifier = Modifier.weight(1f).padding(4.dp)
                                )
                                if (i + 1 < category.levels.size) {
                                    RecognitionLevelButton(
                                        info = category.levels[i + 1],
                                        onClick = onLevelClick,
                                        modifier = Modifier.weight(1f).padding(4.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
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
fun RecognitionLevelButton(
    info: LevelInfoState,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
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
        Text(
            text = "${info.displayName}\n${info.percentage}%",
            textAlign = TextAlign.Center
        )
    }
}
