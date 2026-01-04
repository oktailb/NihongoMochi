package org.nihongo.mochi.ui.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.presentation.dictionary.KanjiDetailViewModel
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KanjiDetailScreen(
    viewModel: KanjiDetailViewModel,
    onKanjiClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val kanjiStrokeOrderFamily = FontFamily(Font(Res.font.KanjiStrokeOrders))
    
    MochiBackground {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Kanji Card
                Card(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(bottom = 24.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.kanjiCharacter ?: "",
                            fontSize = 200.sp,
                            //fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = kanjiStrokeOrderFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Meanings
                SectionHeader(text = "MEANINGS")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.kanjiMeanings.joinToString(", "),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "JLPT", value = uiState.jlptLevel ?: "-")
                    StatItem(label = "GRADE", value = uiState.schoolGrade ?: "-")
                    StatItem(label = "STROKES", value = uiState.kanjiStrokes.toString())
                }

                // Components Graph Section (Tree View)
                if (uiState.componentTree != null && uiState.componentTree!!.children.isNotEmpty()) {
                    SectionHeader(text = stringResource(Res.string.kanji_detail_components))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .padding(bottom = 24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        // Background structure text
                        if (!uiState.kanjiStructure.isNullOrEmpty()) {
                            Text(
                                text = uiState.kanjiStructure ?: "",
                                fontSize = 60.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), // Increased visibility
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                            )
                        }
                        
                        KanjiGraphComponent(
                            rootNode = uiState.componentTree!!,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else if (uiState.components.isNotEmpty()) {
                    // Fallback to simple list if tree not built or empty
                     // Components Section (Old List style as fallback)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SectionHeader(text = stringResource(Res.string.kanji_detail_components))
                        
                        Row(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.kanjiStructure ?: "",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            
                            Row {
                                uiState.components.forEach { component ->
                                    val currentKanjiRef = component.kanjiRef
                                    
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .let {
                                                if (!currentKanjiRef.isNullOrEmpty()) {
                                                    it.clickable { onKanjiClick(currentKanjiRef) }
                                                } else it
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = component.character,
                                            fontSize = 32.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = kanjiStrokeOrderFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Readings Header
                SectionHeader(text = "READINGS", modifier = Modifier.padding(bottom = 16.dp))

                // Readings Columns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    // ON Readings
                    ReadingColumn(
                        title = "ON (Chinese)",
                        readings = uiState.onReadings,
                        isOn = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    // KUN Readings
                    ReadingColumn(
                        title = "KUN (Japanese)",
                        readings = uiState.kunReadings,
                        isOn = false,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }

                // Examples
                SectionHeader(text = "EXAMPLES", modifier = Modifier.padding(bottom = 16.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.examples.forEach { example ->
                        ExampleItem(example)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        letterSpacing = 0.1.sp,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ReadingColumn(
    title: String, 
    readings: List<KanjiDetailViewModel.ReadingItem>, 
    isOn: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        readings.forEach { reading ->
            val text = if (isOn) hiraganaToKatakana(reading.reading) else reading.reading
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ExampleItem(item: KanjiDetailViewModel.ExampleItem) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .widthIn(min = 60.dp)
    ) {
        Text(
            text = item.word,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = item.reading,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun hiraganaToKatakana(s: String): String {
    return s.map { c ->
        if (c in '\u3041'..'\u3096') {
            (c + 0x60)
        } else {
            c
        }
    }.joinToString("")
}
