package org.nihongo.mochi.ui.games.crossword

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*

@Composable
fun CrosswordGameScreen(
    viewModel: CrosswordViewModel,
    onBackClick: () -> Unit
) {
    val cells by viewModel.cells.collectAsState()
    val placedWords by viewModel.placedWords.collectAsState()
    val selectedCell by viewModel.selectedCell.collectAsState()
    val keyboardKeys by viewModel.keyboardKeys.collectAsState()
    val gameTimeSeconds by viewModel.gameTimeSeconds.collectAsState()
    val selectedHintType by viewModel.selectedHintType.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    
    val cellsMap = remember(cells) {
        cells.associateBy { it.r to it.c }
    }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val gridSize = 16
    val cellSize = 40.dp
    val gridDimension = cellSize * gridSize

    val currentWord = remember(selectedCell, placedWords) {
        selectedCell?.let { (r, c) ->
            placedWords.find { word ->
                if (word.isHorizontal) {
                    word.row == r && c >= word.col && c < word.col + word.word.length
                } else {
                    word.col == c && r >= word.row && r < word.row + word.word.length
                }
            }
        }
    }

    MochiBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale *= zoom
                            scale = scale.coerceIn(0.5f, 3f)
                            offset += pan
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.wrapContentSize(unbounded = true)) {
                    Column(modifier = Modifier.requiredSize(gridDimension)) {
                        for (r in 0 until gridSize) {
                            Row(modifier = Modifier.wrapContentWidth(unbounded = true)) {
                                for (c in 0 until gridSize) {
                                    val cell = cellsMap[r to c]
                                    CrosswordCellView(
                                        cell = cell,
                                        isSelected = selectedCell?.first == r && selectedCell?.second == c,
                                        onClick = { 
                                            if (cell != null && !cell.isBlack) {
                                                viewModel.onCellSelected(r, c)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.game_crosswords_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTime(gameTimeSeconds),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = onBackClick) {
                        Text("X")
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            ) {
                if (currentWord != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            if (selectedMode == CrosswordMode.KANJIS) {
                                Text(
                                    text = currentWord.phonetics,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (currentWord.meaning.isNotEmpty()) {
                                    Text(
                                        text = currentWord.meaning,
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            } else {
                                val clueText = if (selectedHintType == CrosswordHintType.KANJI) {
                                    currentWord.kanji
                                } else {
                                    currentWord.meaning
                                }
                                Text(
                                    text = clueText,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (selectedCell != null && !isFinished) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shadowElevation = 16.dp,
                        tonalElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp).height(200.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(keyboardKeys) { key ->
                                    KeyButton(text = key, onClick = { viewModel.onKeyTyped(key) })
                                }
                            }
                            
                            Button(
                                onClick = { viewModel.onDelete() },
                                modifier = Modifier.width(60.dp).fillMaxHeight().padding(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("⌫", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
            
            if (isFinished) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(Res.string.game_crossword_congrats), 
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(Res.string.game_crossword_completed_in, formatTime(gameTimeSeconds))
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onBackClick) {
                                Text(stringResource(Res.string.game_crossword_back_menu))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.aspectRatio(1f).clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CrosswordCellView(
    cell: CrosswordCell?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cellSize = 40.dp
    
    // Conteneur fixe pour garder la grille alignée
    Box(
        modifier = Modifier.size(cellSize),
        contentAlignment = Alignment.Center
    ) {
        if (cell != null && !cell.isBlack) {
            val isInputCorrect = cell.userInput.isNotEmpty() && cell.userInput == cell.solution
            val isInputWrong = cell.userInput.isNotEmpty() && cell.userInput != cell.solution

            val backgroundColor = when {
                cell.isCorrect || isInputCorrect -> Color(0xFFC8E6C9)
                isInputWrong -> Color(0xFFFFCDD2)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.White
            }

            // Surface gère l'élévation et l'ombre correctement
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp) // Important: laisse l'ombre visible sans chevaucher les voisins
                    .clickable { onClick() },
                shape = RoundedCornerShape(2.dp),
                color = backgroundColor,
                shadowElevation = if (isSelected) 4.dp else 2.dp,
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 0.5.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (cell.number != null) {
                        Text(
                            text = cell.number.toString(),
                            fontSize = 9.sp,
                            modifier = Modifier.align(Alignment.TopStart).padding(2.dp),
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = cell.userInput,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            cell.isCorrect || isInputCorrect -> Color(0xFF2E7D32)
                            isInputWrong -> Color(0xFFD32F2F)
                            else -> Color.Black
                        }
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
