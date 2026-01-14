package org.nihongo.mochi.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
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
    onMemorizeClick: () -> Unit,
    onParticlesClick: () -> Unit,
    onForgeClick: () -> Unit,
    onShiritoriClick: () -> Unit,
    onShadowClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoonMessage = stringResource(Res.string.about_coming_soon)

    val showComingSoon = {
        scope.launch {
            snackbarHostState.showSnackbar(comingSoonMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        MochiBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(padding)
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
                    title = stringResource(Res.string.game_kana_link_title),
                    subtitle = stringResource(Res.string.game_kana_link_subtitle),
                    kanjiTitle = "リンク",
                    onClick = onTetrisClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                BigModeCard(
                    title = stringResource(Res.string.game_crosswords_title),
                    subtitle = stringResource(Res.string.game_crosswords_subtitle),
                    kanjiTitle = "十字語",
                    onClick = { showComingSoon() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                BigModeCard(
                    title = stringResource(Res.string.game_particles_title),
                    subtitle = stringResource(Res.string.game_particles_subtitle),
                    kanjiTitle = "助詞",
                    onClick = { showComingSoon() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                BigModeCard(
                    title = stringResource(Res.string.game_forge_title),
                    subtitle = stringResource(Res.string.game_forge_subtitle),
                    kanjiTitle = "鍛冶",
                    onClick = { showComingSoon() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                BigModeCard(
                    title = stringResource(Res.string.game_shiritori_title),
                    subtitle = stringResource(Res.string.game_shiritori_subtitle),
                    kanjiTitle = "しりとり",
                    onClick = { showComingSoon() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                BigModeCard(
                    title = stringResource(Res.string.game_shadow_title),
                    subtitle = stringResource(Res.string.game_shadow_subtitle),
                    kanjiTitle = "影",
                    onClick = { showComingSoon() }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
