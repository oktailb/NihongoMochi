package org.nihongo.mochi.ui.writingrecap

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.nihongo.mochi.R
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.components.PaginationControls
import org.nihongo.mochi.ui.components.PlayButton
import org.nihongo.mochi.ui.components.RecapKanjiGrid

@Composable
fun WritingRecapScreen(
    levelTitle: String,
    kanjiListWithColors: List<Pair<KanjiEntry, Color>>,
    currentPage: Int,
    totalPages: Int,
    onKanjiClick: (KanjiEntry) -> Unit,
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
                text = stringResource(R.string.title_game_recap),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = levelTitle,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kanji Grid (reuse from RecapComponents)
            Column(modifier = Modifier.weight(1f)) {
                RecapKanjiGrid(
                    kanjiList = kanjiListWithColors,
                    onKanjiClick = onKanjiClick
                )
            }

            // Pagination Controls (reuse from RecapComponents)
            PaginationControls(
                currentPage = currentPage,
                totalPages = totalPages,
                onPrevClick = onPrevPage,
                onNextClick = onNextPage
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Play Button
            PlayButton(onClick = onPlayClick)
        }
    }
}
