package org.nihongo.mochi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.game_recap_play
import org.nihongo.mochi.shared.generated.resources.mode_revise
import org.nihongo.mochi.shared.generated.resources.previous_page
import org.nihongo.mochi.shared.generated.resources.next_page
import org.nihongo.mochi.shared.generated.resources.order_by
import org.nihongo.mochi.shared.generated.resources.size

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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.previous_page), tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            text = "Page ${currentPage + 1} of $totalPages",
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = onNextClick, enabled = currentPage < totalPages - 1) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(Res.string.next_page), tint = MaterialTheme.colorScheme.onBackground)
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
                            enabled = enabled,
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = text, 
                            color = if (value == selectedOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSelector(
    title: String? = null,
    options: List<Pair<String, T>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.find { it.second == selectedOption }?.first ?: ""

    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = stringResource(Res.string.order_by),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                ),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (text, value) ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            onOptionSelected(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuizSizeInput(
    size: String,
    onSizeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(Res.string.size),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        OutlinedTextField(
            value = size,
            onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) onSizeChange(it) },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun PlayAndReviewButtons(
    onPlayClick: () -> Unit,
    onReviewClick: () -> Unit,
    isReviewEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onPlayClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.game_recap_play),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Button(
            onClick = onReviewClick,
            enabled = isReviewEnabled,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.mode_revise),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
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
