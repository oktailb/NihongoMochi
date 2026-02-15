package org.nihongo.mochi.ui.writingrecap

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.nihongo.mochi.domain.game.KanjiSortOrder
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.ResourceUtils
import org.nihongo.mochi.ui.components.*
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*

@Composable
fun WritingRecapScreen(
    levelTitle: String, // Technical key like "n5"
    kanjiListWithColors: List<Pair<KanjiEntry, Color>>,
    currentPage: Int,
    totalPages: Int,
    sortOrder: KanjiSortOrder,
    isReviewEnabled: Boolean,
    onKanjiClick: (KanjiEntry) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onSortOrderChange: (KanjiSortOrder) -> Unit,
    onPlayClick: (Int) -> Unit,
    onReviewClick: () -> Unit
) {
    val levelResource = ResourceUtils.resolveStringResource(levelTitle.lowercase())
    val resolvedTitle = if (levelResource != null) stringResource(levelResource) else levelTitle

    var quizSize by remember { mutableStateOf("80") }

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.writing_game_recap_title),
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropdownSelector(
                    options = listOf(
                        stringResource(Res.string.game_recap_sort_default) to KanjiSortOrder.DEFAULT,
                        stringResource(Res.string.game_recap_sort_frequency) to KanjiSortOrder.FREQUENCY,
                        stringResource(Res.string.game_recap_sort_strokes) to KanjiSortOrder.STROKES
                    ),
                    selectedOption = sortOrder,
                    onOptionSelected = onSortOrderChange,
                    modifier = Modifier.weight(0.7f)
                )

                QuizSizeInput(
                    size = quizSize,
                    onSizeChange = { quizSize = it },
                    modifier = Modifier.weight(0.3f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kanji Grid
            Column(modifier = Modifier.weight(1f)) {
                RecapKanjiGrid(
                    kanjiList = kanjiListWithColors,
                    onKanjiClick = onKanjiClick
                )
            }

            // Pagination Controls
            PaginationControls(
                currentPage = currentPage,
                totalPages = totalPages,
                onPrevClick = onPrevPage,
                onNextClick = onNextPage
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlayAndReviewButtons(
                onPlayClick = { onPlayClick(quizSize.toIntOrNull() ?: 80) },
                onReviewClick = onReviewClick,
                isReviewEnabled = isReviewEnabled
            )
        }
    }
}
