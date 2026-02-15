package org.nihongo.mochi.ui.wordlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.nihongo.mochi.domain.words.WordEntry
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.ResourceUtils
import org.nihongo.mochi.ui.components.PaginationControls
import org.nihongo.mochi.ui.components.PlayAndReviewButtons
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    listTitle: String,
    wordsWithColors: List<Triple<WordEntry, Color, String?>>, // Word, Color, Meaning
    currentPage: Int,
    totalPages: Int,
    isReviewEnabled: Boolean,
    filterKanjiOnly: Boolean,
    filterSimpleWords: Boolean,
    filterCompoundWords: Boolean,
    filterIgnoreKnown: Boolean,
    selectedWordType: Pair<String, String>,
    wordTypeOptions: List<Pair<String, String>>,
    onFilterKanjiOnlyChange: (Boolean) -> Unit,
    onFilterSimpleWordsChange: (Boolean) -> Unit,
    onFilterCompoundWordsChange: (Boolean) -> Unit,
    onFilterIgnoreKnownChange: (Boolean) -> Unit,
    onWordTypeChange: (Pair<String, String>) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onPlayClick: () -> Unit,
    onReviewClick: () -> Unit,
    onWordClick: (String) -> Unit
) {
    val levelResource = ResourceUtils.resolveStringResource(listTitle.lowercase())
    val resolvedTitle = if (levelResource != null) stringResource(levelResource) else listTitle

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = resolvedTitle,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            // Filters
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ChipFilter(text = stringResource(Res.string.reading_kanji_solo), selected = filterKanjiOnly, onClick = { onFilterKanjiOnlyChange(!filterKanjiOnly) })
                        ChipFilter(text = stringResource(Res.string.reading_simple_words), selected = filterSimpleWords, onClick = { onFilterSimpleWordsChange(!filterSimpleWords) })
                        ChipFilter(text = stringResource(Res.string.reading_compound_words), selected = filterCompoundWords, onClick = { onFilterCompoundWordsChange(!filterCompoundWords) })
                    }

                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = selectedWordType.second)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            wordTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.second) },
                                    onClick = {
                                        onWordTypeChange(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    ChipFilter(text = stringResource(Res.string.reading_ignore_known_words), selected = filterIgnoreKnown, onClick = { onFilterIgnoreKnownChange(!filterIgnoreKnown) })
                }
            }

            // Word List with scrolling
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                 FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wordsWithColors.forEach { (word, color, meaning) ->
                        WordChip(
                            text = word.text,
                            meaning = meaning,
                            backgroundColor = color,
                            onClick = { onWordClick(word.text) }
                        )
                    }
                }
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
                onPlayClick = onPlayClick,
                onReviewClick = onReviewClick,
                isReviewEnabled = isReviewEnabled
            )
        }
    }
}

@Composable
fun ChipFilter(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = Modifier.padding(horizontal = 4.dp),
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        }
    )
}

@Composable
fun WordChip(
    text: String,
    meaning: String?,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!meaning.isNullOrEmpty()) {
                Text(
                    text = meaning,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }
        }
    }
}
