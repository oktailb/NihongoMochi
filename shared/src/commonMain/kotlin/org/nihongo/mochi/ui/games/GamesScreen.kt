package org.nihongo.mochi.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.home.BigModeCard

@Composable
fun GamesScreen(
    onBackClick: () -> Unit,
    onTaquinClick: () -> Unit,
    onSimonClick: () -> Unit,
    onTetrisClick: () -> Unit,
    onCrosswordsClick: () -> Unit,
    onMemorizeClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.games_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )

            BigModeCard(
                title = stringResource(Res.string.game_taquin_title),
                subtitle = stringResource(Res.string.game_taquin_subtitle),
                kanjiTitle = "パズル",
                onClick = onTaquinClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            BigModeCard(
                title = stringResource(Res.string.game_simon_title),
                subtitle = stringResource(Res.string.game_simon_subtitle),
                kanjiTitle = "記憶",
                onClick = onSimonClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            BigModeCard(
                title = stringResource(Res.string.game_memorize_title),
                subtitle = stringResource(Res.string.game_memorize_subtitle),
                kanjiTitle = "神経衰弱",
                onClick = onMemorizeClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            BigModeCard(
                title = stringResource(Res.string.game_tetris_title),
                subtitle = stringResource(Res.string.game_tetris_subtitle),
                kanjiTitle = "テトリス",
                onClick = onTetrisClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            BigModeCard(
                title = stringResource(Res.string.game_crosswords_title),
                subtitle = stringResource(Res.string.game_crosswords_subtitle),
                kanjiTitle = "十字語",
                onClick = onCrosswordsClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
