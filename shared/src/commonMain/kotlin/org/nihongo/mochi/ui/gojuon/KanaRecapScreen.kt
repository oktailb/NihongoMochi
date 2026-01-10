package org.nihongo.mochi.ui.gojuon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.domain.kana.KanaEntry
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.components.PaginationControls
import org.nihongo.mochi.ui.components.PlayButton

@Composable
fun KanaRecapScreen(
    title: String,
    kanaListWithColors: List<Pair<KanaEntry, Color>>,
    linesToShow: List<Int>, // Which lines (rows) are we showing on this page
    charactersByLine: Map<Int, List<KanaEntry>>,
    kanaColors: Map<String, Color>,
    currentPage: Int,
    totalPages: Int,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onPlayClick: () -> Unit
) {
    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Grid for Gojuon (5 columns)
            Column(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Iterate over the lines we are supposed to show
                    for (lineKey in linesToShow) {
                        val charsInLine = charactersByLine[lineKey] ?: emptyList()
                        
                        // We use a map to place characters in their specific columns (1-5)
                        val columnMap = charsInLine.associateBy { it.column }

                        for (col in 1..5) {
                            val kana = columnMap[col]
                            if (kana != null) {
                                item(key = "${kana.character}_${lineKey}_$col") {
                                    val color = kanaColors[kana.character] ?: Color.LightGray
                                    KanaGridItem(kana.character, color)
                                }
                            } else {
                                // Add a spacer for empty columns (like in rows ya, wa, n)
                                item(key = "empty_${lineKey}_$col") {
                                    Spacer(modifier = Modifier.aspectRatio(1f))
                                }
                            }
                        }
                    }
                }
            }

            PaginationControls(
                currentPage = currentPage,
                totalPages = totalPages,
                onPrevClick = onPrevPage,
                onNextClick = onNextPage
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlayButton(onClick = onPlayClick)
        }
    }
}

@Composable
fun KanaGridItem(
    character: String,
    color: Color
) {
    // Determine a readable text color based on background luminance
    val textColor = if (color.luminance() > 0.5f) Color.Black else Color.White

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = character,
            fontSize = 24.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
