package org.nihongo.mochi.ui.games.kanadrop

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.nihongo.mochi.presentation.MochiBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanaDropGameScreen(
    viewModel: KanaDropViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    MochiBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Kana Link") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.isGameOver) {
                GameOverView(state, onBackClick)
            } else {
                val backgroundColor by animateColorAsState(
                    if (state.errorFlash) Color.Red.copy(alpha = 0.3f) else Color.Transparent,
                    label = "errorFlash"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(backgroundColor)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Score", style = MaterialTheme.typography.labelMedium)
                            Text(
                                state.score.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Optimized Timer
                        TimerDisplay(viewModel.state)
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Words", style = MaterialTheme.typography.labelMedium)
                            Text(
                                state.wordsFound.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(
                            state.currentWord.ifEmpty { " " },
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Game Grid
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        val gridHeight = state.grid.size
                        val gridWidth = state.grid.firstOrNull()?.size ?: 1
                        val cellSizeWidth = maxWidth / gridWidth
                        val cellSizeHeight = maxHeight / gridHeight
                        val cellSize = minOf(cellSizeWidth, cellSizeHeight)
                        val boardWidth = cellSize * gridWidth
                        val boardHeight = cellSize * gridHeight

                        Box(
                            modifier = Modifier
                                .size(boardWidth, boardHeight)
                                .align(Alignment.Center)
                                .pointerInput(state.grid) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val col = (offset.x / cellSize.toPx()).toInt()
                                            val row = (offset.y / cellSize.toPx()).toInt()
                                            viewModel.onCellTouched(row, col)
                                        },
                                        onDrag = { change, _ ->
                                            val col = (change.position.x / cellSize.toPx()).toInt()
                                            val row = (change.position.y / cellSize.toPx()).toInt()
                                            viewModel.onCellTouched(row, col)
                                        },
                                        onDragEnd = {
                                            viewModel.onReleaseSelection()
                                        }
                                    )
                                }
                        ) {
                            state.grid.forEach { row ->
                                row.forEach { cell ->
                                    val isSelected = state.selectedCells.any { it.id == cell.id }
                                    KanaCellItem(cell = cell, size = cellSize, isSelected = isSelected)
                                }
                            }
                        }
                    }
                    
                    // Last Word Meaning
                    state.lastValidWord?.let { word ->
                        Card(
                            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(word.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(word.phonetics, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerDisplay(stateFlow: StateFlow<KanaDropGameState>) {
    // We only collect the timeRemaining to minimize recomposition
    val timeRemaining by stateFlow.map { it.timeRemaining }.collectAsState(initial = 0)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Time", style = MaterialTheme.typography.labelMedium)
        Text(
            timeRemaining.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (timeRemaining < 10) Color.Red else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun GameOverView(state: KanaDropGameState, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("GAME OVER", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = Color.Red)
        Spacer(modifier = Modifier.height(24.dp))
        
        ScoreStat("Final Score", state.score.toString())
        ScoreStat("Words Found", state.wordsFound.toString())
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("RETURN TO MENU", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ScoreStat(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun KanaCellItem(
    cell: KanaDropCell,
    size: androidx.compose.ui.unit.Dp,
    isSelected: Boolean
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        label = "cellColor"
    )
    val textColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "textColor"
    )
    val scale by animateFloatAsState(if (isSelected) 1.2f else 1.0f, label = "cellScale")

    Box(
        modifier = Modifier
            .offset(x = size * cell.col, y = size * cell.row)
            .size(size)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cell.char,
            fontSize = (size.value * 0.5f).sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
        )
    }
}
