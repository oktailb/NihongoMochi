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
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.ResourceUtils
import org.nihongo.mochi.ui.components.ModeSelector
import org.nihongo.mochi.ui.components.PaginationControls
import org.nihongo.mochi.ui.components.PlayButton
import org.nihongo.mochi.ui.components.RecapKanjiGrid
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*

@Composable
fun GameRecapScreen(
    levelTitle: String, // Technical key like "n5"
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
    val levelResource = ResourceUtils.resolveStringResource(levelTitle.lowercase())
    val resolvedTitle = if (levelResource != null) stringResource(levelResource) else levelTitle

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.game_recap_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = resolvedTitle,
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
                    stringResource(Res.string.game_recap_meaning) to "meaning",
                    stringResource(Res.string.game_recap_reading) to "reading"
                ),
                selectedOption = gameMode,
                onOptionSelected = onGameModeChange,
                enabled = isMeaningEnabled || isReadingEnabled 
            )
            
            AnimatedVisibility(visible = gameMode == "reading") {
                ModeSelector(
                    options = listOf(
                        stringResource(Res.string.game_recap_common_pronunciations) to "common",
                        stringResource(Res.string.game_recap_random_pronunciations) to "random"
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
