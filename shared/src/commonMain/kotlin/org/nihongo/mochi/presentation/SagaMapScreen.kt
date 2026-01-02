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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.nihongo.mochi.domain.statistics.ResultsViewModel
import org.nihongo.mochi.domain.statistics.SagaNode
import org.nihongo.mochi.domain.statistics.SagaStep
import org.nihongo.mochi.domain.statistics.SagaTab
import org.nihongo.mochi.domain.statistics.StatisticsType
import org.nihongo.mochi.domain.statistics.UserSagaProgress
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.background_day
import org.nihongo.mochi.shared.generated.resources.background_night
import org.nihongo.mochi.shared.generated.resources.reading
import org.nihongo.mochi.shared.generated.resources.recognising
import org.nihongo.mochi.shared.generated.resources.writing
import kotlin.math.abs
import kotlin.math.sin

enum class SagaAction {
    SIGN_IN, ACHIEVEMENTS, BACKUP, RESTORE
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
                        onNodeClick = onNodeClick
                    )
                }
            }
        }
    }
}

// ... CloudActionsBar, SagaTabBar, etc. remain the same ...
@Composable
fun CloudActionsBar(
    isAuthenticated: Boolean,
    onAction: (SagaAction) -> Unit
) {
    FloatingCardBar {
        if (!isAuthenticated) {
            ActionButton(
                icon = Icons.Default.Login,
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
                indication = androidx.compose.material.ripple.rememberRipple(bounded = false),
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
    onNodeClick: (String, StatisticsType) -> Unit
) {
    val listState = rememberLazyListState()
    val pathColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val density = LocalDensity.current
    val context = LocalContext.current
    
    LaunchedEffect(steps) {
        if (steps.isNotEmpty()) {
             listState.scrollToItem(0)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthDp = maxWidth
        val widthPx = with(density) { widthDp.toPx() }
        val centerXPx = widthPx / 2f
        
        // Use a winding path (sine wave) for the "Candy Crush" feel
        // Amplitude determines how wide the snake is
        val amplitudeDp = (widthDp / 2) - 60.dp // Leave margins
        val amplitudePx = with(density) { amplitudeDp.toPx() }
        
        val nodeSpacing = 280.dp 
        val nodeSpacingPx = with(density) { nodeSpacing.toPx() }
        
        // Helper to resolve string resource dynamically for Node Titles
        fun getNodeTitle(key: String): String {
            val resId = context.resources.getIdentifier(key, "string", context.packageName)
            return if (resId != 0) context.getString(resId) else key
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 200.dp, top = 40.dp),
            reverseLayout = false 
        ) {
            itemsIndexed(steps) { index, step ->
                val nextStep = steps.getOrNull(index + 1)
                
                // Calculate base position on the winding curve
                val phase = index * 0.8f 
                val basePathX = centerXPx + (sin(phase) * amplitudePx)
                
                // Determine layout for THIS step (1 node vs 2 nodes)
                val nodePositionsX = if (step.nodes.size > 1) {
                    // Fork: Place nodes side-by-side centered around the path
                    val spread = with(density) { 160.dp.toPx() } // Wide enough to avoid overlap
                    val leftX = basePathX - (spread / 2)
                    val rightX = basePathX + (spread / 2)
                    
                    // Clamp to screen bounds
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
                    // Draw Connections to Next Step
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
                             
                             // If multiple nodes connect to multiple nodes (Graph Mesh Issue)
                             // Logic: Connect left to left, right to right (parallel) if possible
                             // Or simplify: connect everything to everything if ambiguous
                             
                             // Current issue: "maillage complet" -> every node in step N connects to every node in step N+1
                             
                             // FIX: Simple heuristic
                             // 1 -> 1 : Single line
                             // 2 -> 2 : Parallel lines (0->0, 1->1)
                             // 1 -> 2 : Fork (0->0, 0->1)
                             // 2 -> 1 : Merge (0->0, 1->0)
                             
                             val currentCount = nodePositionsX.size
                             val nextCount = nextPositionsX.size
                             
                             if (currentCount == nextCount) {
                                 // 1-to-1 or 2-to-2 (Parallel)
                                 for (i in 0 until currentCount) {
                                     val startX = nodePositionsX[i]
                                     val endX = nextPositionsX[i]
                                     drawCurvedPath(this, startX, startY, endX, endY, nodeSpacingPx, pathColor)
                                 }
                             } else {
                                 // Fork or Merge: Connect all to all (standard graph behavior)
                                 // OR try to be smart: if 1->2, connect 0->0 and 0->1
                                 nodePositionsX.forEach { startX ->
                                     nextPositionsX.forEach { endX ->
                                         drawCurvedPath(this, startX, startY, endX, endY, nodeSpacingPx, pathColor)
                                     }
                                 }
                             }
                        }
                        
                        // Place Billboards on the connections
                        step.nodes.forEachIndexed { nodeIndex, node ->
                            val progress = viewModel.getSagaProgress(node)
                            val startX = nodePositionsX[nodeIndex]
                            
                            // Determine target for billboard path interpolation
                            // If 2->2, target is same index. If others, average or first.
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
                                        resource = Res.drawable.writing // Placeholder
                                        color = MaterialTheme.colorScheme.error
                                        label = "Gram"
                                    }
                                    StatisticsType.GAMES -> {
                                        resource = Res.drawable.recognising // Placeholder
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
                                                else -> null
                                            }
                                            if (id != null) onNodeClick(id, spec.type)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Render Main Nodes for this Step
                    step.nodes.forEachIndexed { nodeIndex, node ->
                        val progress = viewModel.getSagaProgress(node)
                        val nodeX = nodePositionsX[nodeIndex]
                        
                        SagaNodeItem(
                            node = node,
                            // TRANSLATE THE TITLE HERE
                            title = getNodeTitle(node.title),
                            progress = progress,
                            isAuthenticated = isAuthenticated,
                            onNodeClick = onNodeClick,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset { IntOffset(nodeX.toInt() - 110, (nodeSpacingPx/2).toInt() - 110) }
                        )
                    }
                }
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
    ) {
        if (isLeftSide) {
            BillboardContent(drawable, description, progress, color)
            Icon(
                imageVector = Icons.Default.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp).offset(x = (-4).dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.ArrowLeft,
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

@Composable
fun SagaNodeItem(
    node: SagaNode,
    title: String = node.title, // Add title parameter with default fallback
    progress: UserSagaProgress,
    isAuthenticated: Boolean,
    onNodeClick: (String, StatisticsType) -> Unit,
    modifier: Modifier = Modifier
) {
    val validScores = mutableListOf<Int>()
    if (node.recognitionId != null) validScores.add(progress.recognitionIndex)
    if (node.readingId != null) validScores.add(progress.readingIndex)
    if (node.writingId != null) validScores.add(progress.writingIndex)
    
    val avgProgress = if (validScores.isNotEmpty()) {
        validScores.average().toInt()
    } else {
        0
    }
    
    val isCompleted = avgProgress >= 100
    
    val backgroundColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val borderColor = MaterialTheme.colorScheme.outline

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (isAuthenticated) {
             Surface(
                 shape = CircleShape,
                 color = MaterialTheme.colorScheme.surface,
                 border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                 modifier = Modifier
                     .size(36.dp)
                     .offset(y = 12.dp)
                     .zIndex(1f)
             ) {
                 Icon(
                     imageVector = Icons.Default.AccountCircle,
                     contentDescription = "User Avatar",
                     tint = MaterialTheme.colorScheme.onSurface,
                     modifier = Modifier.padding(2.dp)
                 )
             }
        }

        Surface(
            shape = CircleShape,
            color = backgroundColor,
            border = androidx.compose.foundation.BorderStroke(3.dp, borderColor),
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(80.dp)
                .clickable { 
                    onNodeClick(node.id, node.mainType)
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = title, // Use the passed title which might be translated
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${avgProgress}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor
                )
            }
        }
    }
}

// Helper function to draw curved path to keep code cleaner
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

fun Modifier.zIndex(zIndex: Float): Modifier = this
