package org.nihongo.mochi.ui.games.taquin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaquinGameScreen(
    viewModel: TaquinViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()

    MochiBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = stringResource(Res.string.game_taquin_title),
                            color = MaterialTheme.colorScheme.onBackground
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack, 
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            state?.let { gameState ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Stats Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = stringResource(Res.string.game_taquin_moves_label), 
                            value = gameState.moves.toString()
                        )
                        StatItem(
                            label = stringResource(Res.string.game_taquin_time_label), 
                            value = stringResource(Res.string.game_memorize_time_format, gameState.timeSeconds)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // The Puzzle Grid
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.widthIn(max = 600.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (r in 0 until gameState.rows) {
                                Row(
                                    modifier = Modifier.wrapContentHeight(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (c in 0 until gameState.cols) {
                                        val index = r * gameState.cols + c
                                        val piece = gameState.pieces.getOrNull(index)
                                        
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                            if (piece != null) {
                                                TaquinPieceItem(
                                                    modifier = Modifier.fillMaxSize(),
                                                    piece = piece,
                                                    onClick = { viewModel.onPieceClicked(index) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (gameState.isSolved) {
                        Text(
                            text = stringResource(Res.string.game_taquin_congrats),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(Res.string.game_taquin_back))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaquinPieceItem(
    modifier: Modifier = Modifier,
    piece: TaquinPiece,
    onClick: () -> Unit
) {
    val backgroundColor = if (piece.isBlank) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(enabled = !piece.isBlank) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!piece.isBlank) {
            Text(
                text = piece.character,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
