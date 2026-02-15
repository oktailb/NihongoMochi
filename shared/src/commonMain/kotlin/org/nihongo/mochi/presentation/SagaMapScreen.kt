package org.nihongo.mochi.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.nihongo.mochi.domain.services.PlayerInfo
import org.nihongo.mochi.domain.statistics.ResultsViewModel
import org.nihongo.mochi.domain.statistics.SagaNode
import org.nihongo.mochi.domain.statistics.SagaStep
import org.nihongo.mochi.domain.statistics.SagaTab
import org.nihongo.mochi.domain.statistics.StatisticsType
import org.nihongo.mochi.domain.statistics.UserSagaProgress
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.background_day
import org.nihongo.mochi.shared.generated.resources.background_night
import org.nihongo.mochi.shared.generated.resources.level_dark
import org.nihongo.mochi.shared.generated.resources.level_light
import org.nihongo.mochi.shared.generated.resources.reading
import org.nihongo.mochi.shared.generated.resources.recognising
import org.nihongo.mochi.shared.generated.resources.writing
import org.nihongo.mochi.shared.generated.resources.grammar
import kotlin.math.abs
import kotlin.math.sin

enum class SagaAction {
    SIGN_IN, ACHIEVEMENTS, BACKUP, RESTORE, LEADERBOARDS
}

private data class BillboardSpec(
    val type: StatisticsType,
    val progress: Int,
    val t: Float = 0f,
    val horizontalOffset: Float = 0f
)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SagaMapScreen(
    viewModel: ResultsViewModel,
    onNodeClick: (String, StatisticsType) -> Unit = { _, _ -> },
    onAction: (SagaAction) -> Unit = {}
) {
    val steps by viewModel.sagaSteps.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val playerInfo by viewModel.playerInfo.collectAsState()
    
    val isDark = isSystemInDarkTheme()
    val backgroundRes = if (isDark) Res.drawable.background_night else Res.drawable.background_day

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Scaffold(
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    CloudActionsBar(
                        isAuthenticated = isAuthenticated,
                        onAction = onAction
                    )
                    
                    SagaTabBar(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            containerColor = Color.Transparent 
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (steps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    SagaMapContent(
                        steps = steps, 
                        viewModel = viewModel, 
                        isAuthenticated = isAuthenticated,
                        playerInfo = playerInfo,
                        onNodeClick = onNodeClick
                    )
                }
            }
        }
    }
}

@Composable
fun CloudActionsBar(
    isAuthenticated: Boolean,
    onAction: (SagaAction) -> Unit
) {
    FloatingCardBar {
        if (!isAuthenticated) {
            ActionButton(
                icon = Icons.AutoMirrored.Filled.Login,
                label = "Sign In",
                onClick = { onAction(SagaAction.SIGN_IN) }
            )
        } else {
            ActionButton(
                icon = Icons.Default.EmojiEvents,
                label = "Trophies",
                onClick = { onAction(SagaAction.ACHIEVEMENTS) }
            )
            ActionButton(
                icon = Icons.Default.Visibility,
                label = "Rankings",
                onClick = { onAction(SagaAction.LEADERBOARDS) }
            )
            ActionButton(
                icon = Icons.Default.CloudUpload,
                label = "Backup",
                onClick = { onAction(SagaAction.BACKUP) }
            )
            ActionButton(
                icon = Icons.Default.CloudDownload,
                label = "Restore",
                onClick = { onAction(SagaAction.RESTORE) }
            )
        }
    }
}

@Composable
fun SagaTabBar(
    currentTab: SagaTab,
    onTabSelected: (SagaTab) -> Unit
) {
    FloatingCardBar {
        SagaTab.values().forEach { tab ->
            TabButton(
                tab = tab,
                isSelected = currentTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
fun FloatingCardBar(
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp), 
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TabButton(
    tab: SagaTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when(tab) {
        SagaTab.JLPT -> Icons.Default.Star
        SagaTab.SCHOOL -> Icons.Default.Edit
        SagaTab.CHALLENGES -> Icons.Default.Lock
    }
    
    val targetContainerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val targetContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    
    val animatedBgColor by animateColorAsState(targetContainerColor, label = "bgColor")
    val animatedContentColor by animateColorAsState(targetContentColor, label = "contentColor")
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tab.name,
            tint = animatedContentColor
        )
        Text(
            text = tab.name,
            style = MaterialTheme.typography.labelSmall,
            color = animatedContentColor,
            fontWeight = fontWeight
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SagaMapContent(
    steps: List<SagaStep>, 
    viewModel: ResultsViewModel,
    isAuthenticated: Boolean,
    playerInfo: PlayerInfo?,
    onNodeClick: (String, StatisticsType) -> Unit
) {
    val listState = rememberLazyListState()
    val pathColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val density = LocalDensity.current
    
    LaunchedEffect(steps) {
        if (steps.isNotEmpty()) {
             listState.scrollToItem(0)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthDp = maxWidth
        val widthPx = with(density) { widthDp.toPx() }
        val centerXPx = widthPx / 2f
        
        val amplitudeDp = (widthDp / 2) - 60.dp 
        val amplitudePx = with(density) { amplitudeDp.toPx() }
        
        val nodeSpacing = 280.dp 
        val nodeSpacingPx = with(density) { nodeSpacing.toPx() }
        
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 200.dp, top = 40.dp),
            reverseLayout = false 
        ) {
            itemsIndexed(steps) { index, step ->
                val nextStep = steps.getOrNull(index + 1)
                
                val phase = index * 0.8f 
                val basePathX = centerXPx + (sin(phase) * amplitudePx)
                
                val nodePositionsX = if (step.nodes.size > 1) {
                    val spread = with(density) { 160.dp.toPx() } 
                    val leftX = basePathX - (spread / 2)
                    val rightX = basePathX + (spread / 2)
                    
                    listOf(
                        leftX.coerceIn(50f, widthPx - 50f),
                        rightX.coerceIn(50f, widthPx - 50f)
                    )
                } else {
                    listOf(basePathX)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(nodeSpacing), 
                    contentAlignment = Alignment.Center
                ) {
                    if (nextStep != null) {
                        val nextPhase = (index + 1) * 0.8f
                        val nextBasePathX = centerXPx + (sin(nextPhase) * amplitudePx)
                        
                        val nextPositionsX = if (nextStep.nodes.size > 1) {
                            val spread = with(density) { 160.dp.toPx() }
                            val leftX = nextBasePathX - (spread / 2)
                            val rightX = nextBasePathX + (spread / 2)
                            listOf(
                                leftX.coerceIn(50f, widthPx - 50f),
                                rightX.coerceIn(50f, widthPx - 50f)
                            )
                        } else {
                            listOf(nextBasePathX)
                        }
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                             val startY = nodeSpacingPx / 2
                             val endY = nodeSpacingPx * 1.5f 
                             
                             val currentCount = nodePositionsX.size
                             val nextCount = nextPositionsX.size
                             
                             if (currentCount == nextCount) {
                                 for (i in 0 until currentCount) {
                                     val startX = nodePositionsX[i]
                                     val endX = nextPositionsX[i]
                                     drawCurvedPath(this, startX, startY, endX, endY, nodeSpacingPx, pathColor)
                                 }
                             } else {
                                 nodePositionsX.forEach { startX ->
                                     nextPositionsX.forEach { endX ->
                                         drawCurvedPath(this, startX, startY, endX, endY, nodeSpacingPx, pathColor)
                                     }
                                 }
                             }
                        }
                        
                        step.nodes.forEachIndexed { nodeIndex, node ->
                            val progress = viewModel.getSagaProgress(node)
                            val startX = nodePositionsX[nodeIndex]
                            
                            val targetX = if (nodePositionsX.size == nextPositionsX.size) {
                                nextPositionsX[nodeIndex]
                            } else {
                                nextPositionsX.average().toFloat()
                            }
                            
                            val p0 = Offset(startX, nodeSpacingPx / 2)
                            val p3 = Offset(targetX, nodeSpacingPx * 1.5f)
                            val p1 = Offset(p0.x, p0.y + nodeSpacingPx * 0.5f)
                            val p2 = Offset(p3.x, p3.y - nodeSpacingPx * 0.5f)
                            
                            val billboards = mutableListOf<BillboardSpec>()
                            if (node.recognitionId != null) 
                                billboards.add(BillboardSpec(StatisticsType.RECOGNITION, progress.recognitionIndex))
                            if (node.readingId != null) 
                                billboards.add(BillboardSpec(StatisticsType.READING, progress.readingIndex))
                            if (node.writingId != null) 
                                billboards.add(BillboardSpec(StatisticsType.WRITING, progress.writingIndex))
                            if (node.grammarId != null) 
                                billboards.add(BillboardSpec(StatisticsType.GRAMMAR, progress.grammarIndex))
                            
                            val placedBillboards = billboards.map { spec ->
                                val t = 0.2f + (spec.progress / 100f) * 0.6f
                                spec.copy(t = t)
                            }.sortedBy { it.t }
                            
                            val finalBillboards = mutableListOf<BillboardSpec>()
                            var i = 0
                            while (i < placedBillboards.size) {
                                val current = placedBillboards[i]
                                val cluster = mutableListOf(current)
                                var j = i + 1
                                
                                while (j < placedBillboards.size && (placedBillboards[j].t - current.t) < 0.1f) {
                                    cluster.add(placedBillboards[j])
                                    j++
                                }
                                
                                cluster.forEachIndexed { clusterIdx, item ->
                                    val offset = if (clusterIdx % 2 == 0) -200f else 50f
                                    finalBillboards.add(item.copy(horizontalOffset = offset))
                                }
                                i = j
                            }
                            
                            finalBillboards.forEach { spec ->
                                val pos = getBezierPoint(spec.t, p0, p1, p2, p3)
                                
                                val resource: DrawableResource
                                val color: Color
                                val label: String
                                
                                when(spec.type) {
                                    StatisticsType.RECOGNITION -> {
                                        resource = Res.drawable.recognising
                                        color = MaterialTheme.colorScheme.secondary
                                        label = "Recog"
                                    }
                                    StatisticsType.READING -> {
                                        resource = Res.drawable.reading
                                        color = MaterialTheme.colorScheme.primary
                                        label = "Read"
                                    }
                                    StatisticsType.WRITING -> {
                                        resource = Res.drawable.writing
                                        color = MaterialTheme.colorScheme.tertiary
                                        label = "Write"
                                    }
                                    StatisticsType.GRAMMAR -> {
                                        resource = Res.drawable.grammar 
                                        color = MaterialTheme.colorScheme.error
                                        label = "Gram"
                                    }
                                    StatisticsType.GAMES -> {
                                        resource = Res.drawable.recognising 
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                        label = "Game"
                                    }
                                }
                                
                                val isLeftSide = spec.horizontalOffset < 0
                                
                                Box(modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset { IntOffset(pos.x.toInt() + spec.horizontalOffset.toInt() - 50, pos.y.toInt() - 50) }
                                ) {
                                    BillboardItem(
                                        drawable = resource,
                                        description = label,
                                        color = color,
                                        progress = spec.progress,
                                        isLeftSide = isLeftSide,
                                        onClick = {
                                            val id = when(spec.type) {
                                                StatisticsType.RECOGNITION -> node.recognitionId
                                                StatisticsType.READING -> node.readingId
                                                StatisticsType.WRITING -> node.writingId
                                                StatisticsType.GRAMMAR -> node.grammarId
                                                else -> null
                                            }
                                            if (id != null) onNodeClick(id, spec.type)
                                        }
                                    )
                                }
                            }

                            // Avatar placement on the path
                            if (isAuthenticated) {
                                val validScores = mutableListOf<Int>()
                                if (node.recognitionId != null) validScores.add(progress.recognitionIndex)
                                if (node.readingId != null) validScores.add(progress.readingIndex)
                                if (node.writingId != null) validScores.add(progress.writingIndex)
                                if (node.grammarId != null) validScores.add(progress.grammarIndex)
                                
                                val avgProgress = if (validScores.isNotEmpty()) validScores.average().toFloat() else 0f
                                
                                if (avgProgress > 0f && avgProgress < 100f) {
                                    val avatarT = (avgProgress / 100f).coerceIn(0.05f, 0.95f)
                                    val avatarPos = getBezierPoint(avatarT, p0, p1, p2, p3)
                                    
                                    Box(modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset { IntOffset(avatarPos.x.toInt() - 40, avatarPos.y.toInt() - 60) }
                                        .zIndex(10000f) 
                                    ) {
                                        PlayerAvatar(playerInfo)
                                    }
                                }
                            }
                        }
                    }
                    
                    step.nodes.forEachIndexed { nodeIndex, node ->
                        val progress = viewModel.getSagaProgress(node)
                        val nodeX = nodePositionsX[nodeIndex]
                        
                        SagaNodeItem(
                            node = node,
                            title = viewModel.getString(node.title),
                            progress = progress,
                            onNodeClick = onNodeClick,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset { IntOffset(nodeX.toInt() - 110, (nodeSpacingPx/2).toInt() - 110) }
                                .zIndex(5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerAvatar(playerInfo: PlayerInfo?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary,
            border = androidx.compose.foundation.BorderStroke(3.dp, Color.White),
            shadowElevation = 10.dp,
            modifier = Modifier.size(64.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Prioritize avatarBytes (extracted via ImageManager)
                if (playerInfo?.avatarBytes != null) {
                    AsyncImage(
                        model = playerInfo.avatarBytes,
                        contentDescription = "Player Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (playerInfo?.iconUri != null) {
                    // Fallback to Uri if bytes are missing (though bytes are more reliable)
                    AsyncImage(
                        model = playerInfo.iconUri,
                        contentDescription = "Player Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Visible if both image methods fail or are loading
                if (playerInfo?.avatarBytes == null && playerInfo?.iconUri == null) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
        
        if (playerInfo != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 4.dp).widthIn(max = 100.dp)
            ) {
                Text(
                    text = playerInfo.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

fun getBezierPoint(t: Float, p0: Offset, p1: Offset, p2: Offset, p3: Offset): Offset {
    val u = 1 - t
    val tt = t * t
    val uu = u * u
    val uuu = uu * u
    val ttt = tt * t
    
    val x = uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x
    val y = uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y
    return Offset(x, y)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BillboardItem(
    drawable: DrawableResource,
    description: String,
    color: Color,
    progress: Int,
    isLeftSide: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isLeftSide) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .zIndex(10f)
    ) {
        if (isLeftSide) {
            BillboardContent(drawable, description, progress, color)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp).offset(x = (-4).dp)
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp).offset(x = 4.dp)
            )
            BillboardContent(drawable, description, progress, color)
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BillboardContent(
    drawable: DrawableResource,
    description: String,
    progress: Int,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp, 
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(drawable),
                contentDescription = description,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "$progress%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SagaNodeItem(
    node: SagaNode,
    title: String = node.title, 
    progress: UserSagaProgress,
    onNodeClick: (String, StatisticsType) -> Unit,
    modifier: Modifier = Modifier
) {
    val validScores = mutableListOf<Int>()
    if (node.recognitionId != null) validScores.add(progress.recognitionIndex)
    if (node.readingId != null) validScores.add(progress.readingIndex)
    if (node.writingId != null) validScores.add(progress.writingIndex)
    if (node.grammarId != null) validScores.add(progress.grammarIndex)
    
    val avgProgress = if (validScores.isNotEmpty()) {
        validScores.average().toInt()
    } else {
        0
    }
    
    val isCompleted = avgProgress >= 100
    val isDark = isSystemInDarkTheme()
    val levelBgRes = if (isDark) Res.drawable.level_dark else Res.drawable.level_light
    
    val contentColor = if (isCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(110.dp) 
                .clickable { 
                    onNodeClick(node.id, node.mainType)
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(levelBgRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${avgProgress}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCurvedPath(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    startX: Float, startY: Float, endX: Float, endY: Float, 
    spacingPx: Float, color: Color
) {
    val path = Path().apply {
         moveTo(startX, startY)
         cubicTo(
             startX, startY + spacingPx * 0.5f,
             endX, endY - spacingPx * 0.5f,
             endX, endY
         )
     }
     
     drawPath(
         path = path,
         color = color,
         style = Stroke(
             width = 6.dp.toPx(),
             cap = StrokeCap.Round,
             join = StrokeJoin.Round
         )
     )
}
