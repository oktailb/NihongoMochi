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
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    availableLevels: List<LevelDefinition>,
    selectedLevelId: String,
    isRecognitionEnabled: Boolean,
    isReadingEnabled: Boolean,
    isWritingEnabled: Boolean,
    onLevelSelected: (String) -> Unit,
    onRecognitionClick: () -> Unit,
    onReadingClick: () -> Unit,
    onWritingClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onResultsClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val scrollState = rememberScrollState()

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

            BigModeCard(
                title = stringResource(Res.string.menu_recognition),
                subtitle = stringResource(Res.string.home_recognition_subtitle),
                kanjiTitle = stringResource(Res.string.recognition_title),
                enabled = isRecognitionEnabled,
                onClick = onRecognitionClick
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            BigModeCard(
                title = stringResource(Res.string.menu_reading),
                subtitle = stringResource(Res.string.home_reading_subtitle),
                kanjiTitle = stringResource(Res.string.reading_title),
                enabled = isReadingEnabled,
                onClick = onReadingClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            BigModeCard(
                title = stringResource(Res.string.menu_writing),
                subtitle = stringResource(Res.string.home_writing_subtitle),
                kanjiTitle = stringResource(Res.string.writing_title),
                enabled = isWritingEnabled,
                onClick = onWritingClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                SmallUtilityCard(
                    title = stringResource(Res.string.menu_dictionary),
                    icon = Icons.Default.Search,
                    onClick = onDictionaryClick,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                
                SmallUtilityCard(
                    title = stringResource(Res.string.menu_results),
                    icon = Icons.Default.Star, 
                    onClick = onResultsClick,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                SmallUtilityCard(
                    title = stringResource(Res.string.settings_title),
                    icon = Icons.Default.Settings, 
                    onClick = onOptionsClick,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                
                SmallUtilityCard(
                    title = stringResource(Res.string.menu_about),
                    icon = Icons.Default.Info, 
                    onClick = onAboutClick,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f
    
    Card(
        modifier = Modifier
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
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                modifier = Modifier.padding(start = 8.dp)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp).padding(bottom = 8.dp)
            )
            
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
