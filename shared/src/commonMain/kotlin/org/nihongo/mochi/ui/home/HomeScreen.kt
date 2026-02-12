package org.nihongo.mochi.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.domain.levels.LevelDefinition
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.ResourceUtils
import org.nihongo.mochi.presentation.OnboardingViewModel
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    availableLevels: List<LevelDefinition>,
    selectedLevelId: String,
    isRecognitionEnabled: Boolean,
    isReadingEnabled: Boolean,
    isWritingEnabled: Boolean,
    isGrammarEnabled: Boolean,
    onLevelSelected: (String) -> Unit,
    onRecognitionClick: () -> Unit,
    onReadingClick: () -> Unit,
    onWritingClick: () -> Unit,
    onGrammarClick: (String) -> Unit,
    onGamesClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onResultsClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val onboardingViewModel: OnboardingViewModel = koinInject()

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.nihongomochi),
                    contentDescription = stringResource(Res.string.app_name),
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Level Selector Slider
            if (availableLevels.isNotEmpty()) {
                LevelSelectorCard(
                    availableLevels = availableLevels,
                    selectedLevelId = selectedLevelId,
                    onLevelSelected = onLevelSelected
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Vocabulary Section (Recognition, Reading, Writing)
            VocabularySplitSection(
                isRecognitionEnabled = isRecognitionEnabled,
                onRecognitionClick = onRecognitionClick,
                isReadingEnabled = isReadingEnabled,
                onReadingClick = onReadingClick,
                isWritingEnabled = isWritingEnabled,
                onWritingClick = onWritingClick
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Grammar Section
            GrammarSplitSection(
                enabled = isGrammarEnabled,
                onGrammarClick = onGrammarClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Games and Dictionary on the same line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallUtilityCard(
                    title = stringResource(Res.string.games_title),
                    icon = Icons.Default.Scoreboard,
                    onClick = onGamesClick,
                    modifier = Modifier.weight(1f)
                )
                SmallUtilityCard(
                    title = stringResource(Res.string.menu_dictionary),
                    icon = Icons.Default.Search,
                    onClick = onDictionaryClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Results, Settings, and About on the same line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallUtilityCard(
                    title = stringResource(Res.string.menu_results),
                    icon = Icons.Default.Star,
                    onClick = onResultsClick,
                    modifier = Modifier.weight(1f)
                )
                
                SmallUtilityCard(
                    title = stringResource(Res.string.settings_title),
                    icon = Icons.Default.Settings,
                    onClick = onOptionsClick,
                    modifier = Modifier.weight(1f)
                )

                SmallUtilityCard(
                    title = stringResource(Res.string.menu_about),
                    icon = Icons.Default.Info,
                    onClick = onAboutClick,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Display onboarding if needed
        OnboardingPopup(viewModel = onboardingViewModel)
    }
}

@Composable
fun VocabularySplitSection(
    isRecognitionEnabled: Boolean,
    onRecognitionClick: () -> Unit,
    isReadingEnabled: Boolean,
    onReadingClick: () -> Unit,
    isWritingEnabled: Boolean,
    onWritingClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.activity_type_vocabulary),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeBlockCard(
                    title = stringResource(Res.string.menu_recognition),
                    kanji = stringResource(Res.string.recognition_title),
                    modifier = Modifier.weight(1f),
                    enabled = isRecognitionEnabled,
                    onClick = onRecognitionClick
                )
                HomeBlockCard(
                    title = stringResource(Res.string.menu_reading),
                    kanji = stringResource(Res.string.reading_title),
                    modifier = Modifier.weight(1f),
                    enabled = isReadingEnabled,
                    onClick = onReadingClick
                )
                HomeBlockCard(
                    title = stringResource(Res.string.menu_writing),
                    kanji = stringResource(Res.string.writing_title),
                    modifier = Modifier.weight(1f),
                    enabled = isWritingEnabled,
                    onClick = onWritingClick
                )
            }
        }
    }
}

@Composable
fun GrammarSplitSection(
    enabled: Boolean,
    onGrammarClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.activity_type_grammar),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeBlockCard(
                    title = stringResource(Res.string.activity_type_grammar_bases),
                    kanji = "基本",
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = { onGrammarClick("dependencies_basics") }
                )
                HomeBlockCard(
                    title = stringResource(Res.string.activity_type_grammar_verbs),
                    kanji = "活用",
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = { onGrammarClick("conjugaison") }
                )
                HomeBlockCard(
                    title = stringResource(Res.string.activity_type_grammar_syntax),
                    kanji = "文法",
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = { onGrammarClick("rules") }
                )
            }
        }
    }
}

@Composable
fun HomeBlockCard(
    title: String,
    kanji: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f
    
    // Auto-scale font size based on title length
    val kanjiFontSize = when {
        kanji.length >= 3 -> 24.sp
        kanji.length >= 4 -> 20.sp
        else -> 28.sp
    }

    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (enabled) 2.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (enabled) 0.9f else 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = kanji,
                fontSize = kanjiFontSize,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LevelSelectorCard(
    availableLevels: List<LevelDefinition>,
    selectedLevelId: String,
    onLevelSelected: (String) -> Unit
) {
    val currentIndex = availableLevels.indexOfFirst { it.id == selectedLevelId }.coerceAtLeast(0)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val currentLevel = availableLevels.getOrNull(currentIndex)
            
            val levelName = currentLevel?.name?.let { 
                ResourceUtils.resolveStringResource(it)?.let { res -> stringResource(res) } 
            } ?: currentLevel?.name ?: ""

            Text(
                text = levelName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            if (currentLevel?.description?.isNotEmpty() == true) {
                val levelDescription = currentLevel.description.let {
                    ResourceUtils.resolveStringResource(it)?.let { res -> stringResource(res) }
                } ?: currentLevel.description

                 Text(
                    text = levelDescription,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Slider(
                value = currentIndex.toFloat(),
                onValueChange = { 
                    val newIndex = it.roundToInt()
                    if (newIndex in availableLevels.indices) {
                        onLevelSelected(availableLevels[newIndex].id)
                    }
                },
                valueRange = 0f..(availableLevels.size - 1).toFloat(),
                steps = (availableLevels.size - 2).coerceAtLeast(0),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun BigModeCard(
    title: String,
    subtitle: String,
    kanjiTitle: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (enabled) 2.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (enabled) 0.9f else 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            
            Text(
                text = kanjiTitle,
                fontSize = 40.sp, // Slightly smaller to fit in row
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun SmallUtilityCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp).padding(bottom = 4.dp)
            )
            
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
