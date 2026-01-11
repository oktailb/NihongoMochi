package org.nihongo.mochi.ui.dictionary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.domain.dictionary.DictionaryItem
import org.nihongo.mochi.domain.dictionary.SearchMode
import org.nihongo.mochi.domain.dictionary.DictionaryViewModel
import org.nihongo.mochi.domain.recognition.RecognitionStroke
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.ResourceUtils
import kotlin.math.min
import kotlin.math.max

@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel,
    onOpenDrawing: () -> Unit,
    onClearDrawing: () -> Unit,
    onItemClick: (DictionaryItem) -> Unit
) {
    val results by viewModel.lastResults.collectAsState()
    val availableLevelOptions by viewModel.availableLevelOptions.collectAsState()
    val textQuery by viewModel.textQuery.collectAsState()
    val searchMode by viewModel.searchMode.collectAsState()
    val lastStrokes by viewModel.lastStrokes.collectAsState()
    
    val focusManager = LocalFocusManager.current
    var filterExpanded by remember { mutableStateOf(false) }
    
    @Composable
    fun resolveStringResource(key: String): String {
        val resource = ResourceUtils.resolveStringResource(key.lowercase())
        return if (resource != null) {
            stringResource(resource)
        } else {
            key.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
    
    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // --- Filter Section ---
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    
                    // Text Search & Drawing Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textQuery,
                            onValueChange = { viewModel.onSearchTextChange(it) },
                            label = { Text(stringResource(Res.string.dictionary_search_hint_text)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                        )
                        
                        IconButton(
                            onClick = onOpenDrawing,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(Res.string.dictionary_search_by_drawing_desc),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Level Filter Dropdown & Search Mode
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            OutlinedButton(
                                onClick = { filterExpanded = true },
                                modifier = Modifier.width(160.dp)
                            ) {
                                val selectedOption = availableLevelOptions.find { it.id == viewModel.selectedLevelId }
                                val displayLabelKey = selectedOption?.labelKey ?: viewModel.selectedLevelId
                                val displayLabel = resolveStringResource(displayLabelKey)
                                
                                Text(text = displayLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            
                            DropdownMenu(
                                expanded = filterExpanded,
                                onDismissRequest = { filterExpanded = false }
                            ) {
                                availableLevelOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(resolveStringResource(option.labelKey)) },
                                        onClick = {
                                            viewModel.selectedLevelId = option.id
                                            viewModel.applyFilters()
                                            filterExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = searchMode == SearchMode.READING, onClick = { viewModel.setSearchMode(SearchMode.READING) })
                            Text(text = "Read", style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable { viewModel.setSearchMode(SearchMode.READING) })
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(selected = searchMode == SearchMode.MEANING, onClick = { viewModel.setSearchMode(SearchMode.MEANING) })
                            Text(text = "Mean", style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable { viewModel.setSearchMode(SearchMode.MEANING) })
                        }
                    }

                    // Stroke Count, Exact Match, Native Thumbnail
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.strokeQuery,
                            onValueChange = { 
                                viewModel.strokeQuery = it
                                viewModel.applyFilters()
                            },
                            label = { Text(stringResource(Res.string.dictionary_search_hint_strokes)) },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 16.dp).clickable { 
                                viewModel.exactMatch = !viewModel.exactMatch
                                viewModel.applyFilters()
                            }
                        ) {
                            Checkbox(checked = viewModel.exactMatch, onCheckedChange = { viewModel.exactMatch = it; viewModel.applyFilters() })
                            Text(text = stringResource(Res.string.dictionary_match_exact), style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        
                        if (!lastStrokes.isNullOrEmpty()) {
                            DrawingThumbnail(
                                strokes = lastStrokes!!,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onOpenDrawing)
                            )
                            
                            IconButton(onClick = onClearDrawing) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }

                    Text(
                        text = stringResource(Res.string.dictionary_results_count_format).replace("%1\$d", results.size.toString()),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Results List ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { item ->
                    DictionaryItemRow(item = item, onClick = onItemClick)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun DrawingThumbnail(
    strokes: List<RecognitionStroke>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(4.dp)) {
        if (strokes.isEmpty()) return@Canvas

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (stroke in strokes) {
            for (point in stroke.points) {
                minX = min(minX, point.x)
                minY = min(minY, point.y)
                maxX = max(maxX, point.x)
                maxY = max(maxY, point.y)
            }
        }

        val drawingWidth = maxX - minX
        val drawingHeight = maxY - minY
        if (drawingWidth <= 0 || drawingHeight <= 0) return@Canvas

        val scale = min(size.width / drawingWidth, size.height / drawingHeight) * 0.8f
        val offsetX = (size.width - (drawingWidth * scale)) / 2f - (minX * scale)
        val offsetY = (size.height - (drawingHeight * scale)) / 2f - (minY * scale)

        strokes.forEach { stroke ->
            val path = Path()
            stroke.points.forEachIndexed { index, point ->
                val px = point.x * scale + offsetX
                val py = point.y * scale + offsetY
                if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun DictionaryItemRow(item: DictionaryItem, onClick: (DictionaryItem) -> Unit) {
    val levelTextList = item.displayLabelKeys.map { key ->
        val resource = ResourceUtils.resolveStringResource(key.lowercase())
        if (resource != null) stringResource(resource) else key
    }
    val levelText = levelTextList.joinToString(" • ")

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick(item) }.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.character, fontSize = 28.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            val onReadings = item.readings.filter { it.type == "on" }.map { hiraganaToKatakana(it.text) }
            val kunReadings = item.readings.filter { it.type == "kun" }.map { it.text }
            val readingText = buildString {
                if (onReadings.isNotEmpty()) append("On: " + onReadings.joinToString(", "))
                if (kunReadings.isNotEmpty()) { if (isNotEmpty()) append("  "); append("Kun: " + kunReadings.joinToString(", ")) }
            }
            Text(text = readingText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = item.meanings.joinToString(", "), fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (levelText.isNotEmpty()) Text(text = levelText, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
        }
        Text(text = "${item.strokeCount} traits", fontSize = 12.sp)
    }
}

private fun hiraganaToKatakana(s: String): String {
    return s.map { if (it in '\u3041'..'\u3096') (it + 0x60) else it }.joinToString("")
}
