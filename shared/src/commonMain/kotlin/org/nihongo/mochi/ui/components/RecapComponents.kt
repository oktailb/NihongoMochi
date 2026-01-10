package org.nihongo.mochi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.game_recap_play

@Composable
fun RecapKanjiGrid(
    kanjiList: List<Pair<KanjiEntry, Color>>,
    onKanjiClick: (KanjiEntry) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(kanjiList) { (kanjiEntry, color) ->
            RecapKanjiGridItem(
                kanji = kanjiEntry.character,
                color = color,
                onClick = { onKanjiClick(kanjiEntry) }
            )
        }
    }
}

@Composable
fun RecapKanjiGridItem(
    kanji: String,
    color: Color,
    onClick: () -> Unit
) {
    // Determine a readable text color based on background luminance
    val textColor = if (color.luminance() > 0.5f) Color.Black else Color.White

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .aspectRatio(1f) // Ensure square cells
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = kanji,
            fontSize = 24.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrevClick, enabled = currentPage > 0) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            text = "Page ${currentPage + 1} of $totalPages",
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = onNextClick, enabled = currentPage < totalPages - 1) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun <T> ModeSelector(
    title: String? = null,
    options: List<Pair<String, T>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        title?.let {
            Text(text = it, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp), color = MaterialTheme.colorScheme.onBackground)
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { (text, value) ->
                    Row(
                        modifier = Modifier
                            .weight(1f) // Give equal space to each option
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = enabled) { onOptionSelected(value) }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center // Center content inside the weighted space
                    ) {
                        RadioButton(
                            selected = (value == selectedOption),
                            onClick = { onOptionSelected(value) },
                            enabled = enabled
                        )
                        Text(
                            text = text, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = stringResource(Res.string.game_recap_play),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
