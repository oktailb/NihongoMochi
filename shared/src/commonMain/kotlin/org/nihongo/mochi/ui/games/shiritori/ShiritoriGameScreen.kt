package org.nihongo.mochi.ui.games.shiritori

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.components.GameHUD
import org.nihongo.mochi.ui.components.GameResultOverlay

@Composable
fun ShiritoriGameScreen(
    viewModel: ShiritoriViewModel,
    onBackClick: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val playedWords by viewModel.playedWords.collectAsState()
    val lastKana by viewModel.lastKana.collectAsState()
    val score by viewModel.score.collectAsState()
    val bestScore by viewModel.bestScore.collectAsState()
    val error by viewModel.error.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isVictory by viewModel.isVictory.collectAsState()
    val gameTimeSeconds by viewModel.gameTimeSeconds.collectAsState()

    val listState = rememberLazyListState()
    val audioPlayer: org.nihongo.mochi.domain.services.AudioPlayer = koinInject()

    // Lancement automatique du jeu dès l'entrée sur l'écran
    LaunchedEffect(Unit) {
        if (gameState == ShiritoriGameState.IDLE) {
            viewModel.startGame()
        }
    }

    LaunchedEffect(playedWords.size) {
        if (playedWords.isNotEmpty()) {
            listState.animateScrollToItem(playedWords.size - 1)
        }
    }

    MochiBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- Header avec GameHUD uniquement ---
                GameHUD(
                    primaryStat = stringResource(Res.string.game_shiritori_title) to "",
                    secondaryStat = "Score" to score.toString(),
                    timerFlow = viewModel.gameTimeSeconds,
                    initialTimerValue = gameTimeSeconds,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // --- Indicateur de Kana Cible ---
                if (gameState != ShiritoriGameState.GAME_OVER && gameState != ShiritoriGameState.IDLE) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Suivant : ", 
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                lastKana.ifEmpty { "?" },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // --- Liste des mots ---
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playedWords) { shiritoriWord ->
                        WordBubble(shiritoriWord)
                    }

                    if (gameState == ShiritoriGameState.AI_TURN) {
                        item {
                            TypingIndicator()
                        }
                    }
                    
                    if (gameState == ShiritoriGameState.LOADING) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                // --- Message d'erreur ---
                if (error != ShiritoriError.None) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = when (error) {
                                ShiritoriError.InvalidStart -> "Doit commencer par $lastKana"
                                ShiritoriError.EndsInN -> "Finit par 'ん' !"
                                ShiritoriError.AlreadyUsed -> "Déjà utilisé !"
                                ShiritoriError.WordNotFound -> "Mot inconnu"
                                else -> ""
                            },
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // --- Zone de saisie ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                            .imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { viewModel.onInputChanged(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { 
                                Text(
                                    text = "Tapez en Romaji...",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                ) 
                            },
                            enabled = gameState == ShiritoriGameState.PLAYER_TURN,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password, 
                                imeAction = ImeAction.Send,
                                autoCorrect = false
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank()) {
                                        viewModel.onPlayerSubmit()
                                    }
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.onPlayerSubmit()
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }

            // --- Overlay de résultat (Stabilité renforcée) ---
            if (gameState == ShiritoriGameState.GAME_OVER) {
                GameResultOverlay(
                    isVictory = isVictory,
                    score = score.toString(),
                    bestScore = bestScore.toString(),
                    stats = listOf(
                        "Mots trouvés" to score.toString(),
                        "Temps" to formatGameTimeHUD(gameTimeSeconds)
                    ),
                    onReplayClick = { viewModel.startGame() },
                    onMenuClick = { 
                        viewModel.resetToIdle()
                        onBackClick() 
                    },
                    audioPlayer = audioPlayer
                )
            }
        }
    }
}

private fun formatGameTimeHUD(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}

@Composable
fun WordBubble(shiritoriWord: ShiritoriWord) {
    val isPlayer = shiritoriWord.isPlayer
    val alignment = if (isPlayer) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isPlayer) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isPlayer) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isPlayer) 16.dp else 0.dp,
                bottomEnd = if (isPlayer) 0.dp else 16.dp
            ),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = shiritoriWord.phonetics,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
                Text(
                    text = shiritoriWord.word,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Text(
        text = "L'IA réfléchit...",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
    )
}
