package org.nihongo.mochi.ui.gamerecap

import androidx.compose.animation.AnimatedVisibility
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
import org.nihongo.mochi.ui.components.ModeSelector
import org.nihongo.mochi.ui.components.PaginationControls
import org.nihongo.mochi.ui.components.PlayButton
import org.nihongo.mochi.ui.components.RecapKanjiGrid

@Composable
fun GameRecapScreen(
    levelTitle: String,
    kanjiListWithColors: List<Pair<KanjiEntry, Color>>,
    currentPage: Int,
    totalPages: Int,
    gameMode: String,
    readingMode: String,
    isMeaningEnabled: Boolean,
    isReadingEnabled: Boolean,
    onKanjiClick: (KanjiEntry) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onGameModeChange: (String) -> Unit,
    onReadingModeChange: (String) -> Unit,
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
                text = stringResource(R.string.game_recap_title),
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

            // Kanji Grid
            Column(modifier = Modifier.weight(1f)) {
                RecapKanjiGrid(
                    kanjiList = kanjiListWithColors,
                    onKanjiClick = onKanjiClick
                )
            }

            // Bottom Controls
            PaginationControls(
                currentPage = currentPage,
                totalPages = totalPages,
                onPrevClick = onPrevPage,
                onNextClick = onNextPage
            )

            ModeSelector(
                options = listOf(
                    stringResource(R.string.game_recap_meaning) to "meaning",
                    stringResource(R.string.game_recap_reading) to "reading"
                ),
                selectedOption = gameMode,
                onOptionSelected = onGameModeChange,
                // This is a simplified enabled logic. The original disabled specific buttons.
                // We might need to pass individual enabled states if that's required.
                enabled = isMeaningEnabled || isReadingEnabled 
            )
            
            AnimatedVisibility(visible = gameMode == "reading") {
                ModeSelector(
                    options = listOf(
                        stringResource(R.string.game_recap_common_pronunciations) to "common",
                        stringResource(R.string.game_recap_random_pronunciations) to "random"
                    ),
                    selectedOption = readingMode,
                    onOptionSelected = onReadingModeChange
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            PlayButton(onClick = onPlayClick)
        }
    }
}
