package org.nihongo.mochi.ui.grammar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.presentation.ScorePresentationUtils
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.stonepath
import org.nihongo.mochi.shared.generated.resources.toori
import org.nihongo.mochi.ui.ResourceUtils
import org.nihongo.mochi.ui.components.MochiWebView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarScreen(
    viewModel: GrammarViewModel,
    onBackClick: () -> Unit,
    quizViewModelFactory: @Composable (tags: List<String>, sessionKey: String) -> GrammarQuizViewModel? = { _, _ -> null }
) {
    val nodes by viewModel.nodes.collectAsState()
    val separators by viewModel.separators.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val totalSlots by viewModel.totalLayoutSlots.collectAsState()
    val currentLevelId by viewModel.currentLevelId.collectAsState()
    
    val availableCategories by viewModel.availableCategories.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val selectedLessonHtml by viewModel.selectedLessonHtml.collectAsState()
    val selectedLessonTitle by viewModel.selectedLessonTitle.collectAsState()
    val selectedQuizTags by viewModel.selectedQuizTags.collectAsState()
    
    var showFilterDialog by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val stonePathPainter = painterResource(Res.drawable.stonepath)

    var detectedLevelName by remember { mutableStateOf("") }
    var initialScrollDone by remember { mutableStateOf(false) }

    val isQuizOpen = selectedQuizTags != null
    val isLessonOpen = selectedLessonHtml != null

    BackHandler(enabled = isQuizOpen || isLessonOpen) {
        if (isQuizOpen) {
            viewModel.closeQuiz()
        } else if (isLessonOpen) {
            viewModel.closeLesson()
        }
    }

    Scaffold { paddingValues ->
        MochiBackground {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val viewportHeight = maxHeight
                    val viewportHeightPx = with(density) { viewportHeight.toPx() }
                    val viewportWidth = maxWidth
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        val totalSlotsVal = totalSlots
                        val calculatedHeight = (totalSlotsVal * 70f).dp + 200.dp
                        
                        val minCanvasHeight = viewportHeight
                        val canvasHeight = if (calculatedHeight > minCanvasHeight) calculatedHeight else minCanvasHeight
                        val canvasHeightPx = with(density) { canvasHeight.toPx() }
                        
                        LaunchedEffect(canvasHeightPx, separators, currentLevelId) {
                            if (!initialScrollDone && separators.isNotEmpty() && canvasHeightPx > 0) {
                                val targetIndex = separators.indexOfFirst { it.levelId == currentLevelId }
                                if (targetIndex != -1) {
                                    val targetY = if (targetIndex > 0) {
                                        separators[targetIndex - 1].y * canvasHeightPx
                                    } else {
                                        0f
                                    }
                                    val finalScroll = max(0f, targetY - 100f)
                                    scrollState.scrollTo(finalScroll.toInt())
                                    initialScrollDone = true
                                }
                            }
                        }
                        
                        LaunchedEffect(separators, canvasHeightPx, viewportHeightPx) {
                            snapshotFlow { scrollState.value }
                                .collect { scrollY ->
                                    if (separators.isNotEmpty()) {
                                        val centerViewY = scrollY.toFloat() + (viewportHeightPx / 3f)
                                        val currentSep = separators.firstOrNull { (it.y * canvasHeightPx) > centerViewY } 
                                                         ?: separators.lastOrNull()
                                        if (currentSep != null) {
                                            detectedLevelName = currentSep.levelId
                                        }
                                    }
                                }
                        }
                        
                        val nodesById = remember(nodes) { nodes.associateBy { it.rule.id } }
                        val nodesByLevel = remember(nodes) { nodes.groupBy { it.rule.level } }
                        
                        val mapping = remember(nodes, nodesById) {
                            val tracksLeft = mutableListOf<Float>()
                            val tracksRight = mutableListOf<Float>()
                            val mapping = mutableMapOf<Pair<String, String>, Int>()
                            val bufferY = 0.02f 
                            val connections = mutableListOf<InternalConnInfo>()
                            nodes.forEach { child ->
                                child.rule.dependencies.forEach { parentId ->
                                    val parent = nodesById[parentId]
                                    if (parent != null && parent.rule.level == child.rule.level) {
                                        val isLeft = child.x < 0.5f
                                        val isParentLeft = parent.x < 0.5f
                                        if (isLeft == isParentLeft) {
                                            val minY = min(parent.y, child.y)
                                            val maxY = max(parent.y, child.y)
                                            connections.add(InternalConnInfo(parentId to child.rule.id, minY, maxY, maxY - minY, isLeft))
                                        }
                                    }
                                }
                            }
                            connections.sortBy { it.length }
                            connections.forEach { info ->
                                val tracks = if (info.isLeft) tracksLeft else tracksRight
                                var allocatedTrack = -1
                                for (i in tracks.indices) {
                                    if (tracks[i] + bufferY < info.minY) {
                                        tracks[i] = info.maxY
                                        allocatedTrack = i
                                        break
                                    }
                                }
                                if (allocatedTrack == -1) {
                                    tracks.add(info.maxY)
                                    allocatedTrack = tracks.lastIndex
                                }
                                mapping[info.key] = allocatedTrack
                            }
                            mapping
                        }
                        
                        val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        val nodeHalfWidthPx = with(density) { 50.dp.toPx() }
                        val cornerRadius = with(density) { 16.dp.toPx() }
                        val trackSpacingPx = with(density) { 4.dp.toPx() }

                        Box(
                            modifier = Modifier.fillMaxWidth().height(canvasHeight)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasW = size.width
                                val canvasH = size.height
                                val centerX = canvasW / 2f

                                separators.forEach { separator ->
                                    val gateY = separator.y * canvasH
                                    val levelNodes = nodesByLevel[separator.levelId] ?: emptyList()
                                    levelNodes.forEach { node ->
                                        val childAnchorX = if (node.x < 0.5f) (node.x * canvasW) + nodeHalfWidthPx else (node.x * canvasW) - nodeHalfWidthPx
                                        val childCenterY = node.y * canvasH
                                        val childChannelX = centerX
                                        val path = Path()
                                        path.moveTo(childAnchorX, childCenterY)
                                        val distY = gateY - childCenterY
                                        val dir = sign(distY)
                                        if (abs(distY) > cornerRadius * 1.5f) {
                                            val turnY = childCenterY + (dir * cornerRadius)
                                            path.quadraticBezierTo(childChannelX, childCenterY, childChannelX, turnY)
                                            path.lineTo(childChannelX, gateY)
                                        } else {
                                            path.cubicTo(childChannelX, childCenterY, childChannelX, gateY, childChannelX, gateY)
                                        }
                                        drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
                                    }
                                }

                                nodes.forEach { node ->
                                    node.rule.dependencies.forEach { dependencyId ->
                                        val parentNode = nodesById[dependencyId]
                                        if (parentNode != null) {
                                            val parentCenterY = parentNode.y * canvasH
                                            val childCenterY = node.y * canvasH
                                            val isInterLevel = parentNode.rule.level != node.rule.level
                                            val path = Path()
                                            if (isInterLevel) {
                                                val gate = separators.filter { it.y < node.y }.maxByOrNull { it.y }
                                                if (gate != null) {
                                                    val gateY = gate.y * canvasH
                                                    val parentAnchorX = if (parentNode.x < 0.5f) (parentNode.x * canvasW) + nodeHalfWidthPx else (parentNode.x * canvasW) - nodeHalfWidthPx
                                                    val parentChannelX = centerX
                                                    val pathParentToGate = Path()
                                                    pathParentToGate.moveTo(parentAnchorX, parentCenterY)
                                                    val distToGate = gateY - parentCenterY
                                                    if (abs(distToGate) > cornerRadius * 1.5f) {
                                                        val turnY = if(distToGate > 0) parentCenterY + cornerRadius else parentCenterY - cornerRadius
                                                        pathParentToGate.quadraticBezierTo(parentChannelX, parentCenterY, parentChannelX, turnY)
                                                        pathParentToGate.lineTo(parentChannelX, gateY)
                                                    } else {
                                                        pathParentToGate.cubicTo(parentChannelX, parentCenterY, centerX, parentCenterY + distToGate * 0.5f, centerX, gateY)
                                                    }
                                                    drawPath(pathParentToGate, lineColor, style = Stroke(width = 2.dp.toPx()))
                                                    val childAnchorX = if (node.x < 0.5f) (node.x * canvasW) + nodeHalfWidthPx else (node.x * canvasW) - nodeHalfWidthPx
                                                    val childChannelX = centerX
                                                    path.moveTo(childChannelX, gateY)
                                                    val distFromGate = childCenterY - gateY
                                                    if (abs(distFromGate) > cornerRadius * 1.5f) {
                                                         val turnY = childCenterY - cornerRadius 
                                                         path.lineTo(childChannelX, turnY)
                                                         path.quadraticBezierTo(childChannelX, childCenterY, childAnchorX, childCenterY)
                                                    } else {
                                                        path.cubicTo(childChannelX, gateY + distFromGate * 0.5f, childChannelX, childCenterY, childAnchorX, childCenterY)
                                                    }
                                                }
                                            } else {
                                                val isCrossing = (parentNode.x < 0.5f) != (node.x < 0.5f)
                                                val parentAnchorX = if (parentNode.x < 0.5f) (parentNode.x * canvasW) + nodeHalfWidthPx else (parentNode.x * canvasW) - nodeHalfWidthPx
                                                if (isCrossing) {
                                                    val childAnchorX = if (node.x < 0.5f) (node.x * canvasW) + nodeHalfWidthPx else (node.x * canvasW) - nodeHalfWidthPx
                                                    val parentChannelX = centerX
                                                    val childChannelX = centerX
                                                    path.moveTo(parentAnchorX, parentCenterY)
                                                    val distY = childCenterY - parentCenterY
                                                    val dir = sign(distY)
                                                    if (abs(distY) > cornerRadius * 2.5f) {
                                                        val turn1Y = parentCenterY + (dir * cornerRadius)
                                                        val midY = parentCenterY + (distY / 2f)
                                                        val turn2Y = childCenterY - (dir * cornerRadius)
                                                        path.quadraticBezierTo(parentChannelX, parentCenterY, parentChannelX, turn1Y)
                                                        path.lineTo(parentChannelX, midY - (dir * cornerRadius))
                                                        path.cubicTo(parentChannelX, midY, childChannelX, midY, childChannelX, midY + (dir * cornerRadius))
                                                        path.lineTo(childChannelX, turn2Y)
                                                        path.quadraticBezierTo(childChannelX, childCenterY, childAnchorX, childCenterY)
                                                    } else {
                                                        path.cubicTo(parentChannelX, parentCenterY, childChannelX, childCenterY, childAnchorX, childCenterY)
                                                    }
                                                } else {
                                                    val parentAnchorXOuter = if (parentNode.x < 0.5f) (parentNode.x * canvasW) - nodeHalfWidthPx else (parentNode.x * canvasW) + nodeHalfWidthPx
                                                    val childAnchorXOuter = if (node.x < 0.5f) (node.x * canvasW) - nodeHalfWidthPx else (node.x * canvasW) + nodeHalfWidthPx
                                                    val trackIndex = mapping[dependencyId to node.rule.id] ?: 0
                                                    val totalOffset = 40f + (trackIndex.toFloat() * trackSpacingPx)
                                                    val controlOffset = if (parentNode.x < 0.5f) -totalOffset else totalOffset
                                                    val outerChannelX = parentAnchorXOuter + controlOffset
                                                    path.moveTo(parentAnchorXOuter, parentCenterY)
                                                    val distY = childCenterY - parentCenterY
                                                    val dir = sign(distY)
                                                    if (abs(distY) > cornerRadius * 2f) {
                                                        val turn1Y = parentCenterY + (dir * cornerRadius)
                                                        val turn2Y = childCenterY - (dir * cornerRadius)
                                                        path.quadraticBezierTo(outerChannelX, parentCenterY, outerChannelX, turn1Y)
                                                        path.lineTo(outerChannelX, turn2Y)
                                                        path.quadraticBezierTo(outerChannelX, childCenterY, childAnchorXOuter, childCenterY)
                                                    } else {
                                                        path.cubicTo(outerChannelX, parentCenterY, outerChannelX, childCenterY, childAnchorXOuter, childCenterY)
                                                    }
                                                }
                                            }
                                            if (!path.isEmpty) drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
                                        }
                                    }
                                }
                            }

                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                 val stoneWidth = 48.dp
                                 Column(modifier = Modifier.width(stoneWidth)) {
                                     val repeatCount = (canvasHeight.value / 40f).toInt() + 10
                                     repeat(repeatCount) {
                                         Image(painter = stonePathPainter, contentDescription = null, contentScale = ContentScale.FillWidth, modifier = Modifier.fillMaxWidth(), alpha = 0.9f)
                                     }
                                 }
                            }

                            separators.forEach { separator ->
                                val yPosPx = separator.y * canvasHeight.value
                                val levelName = ResourceUtils.resolveStringResource("level_${separator.levelId}")?.let { 
                                    stringResource(it) 
                                } ?: separator.levelId.uppercase()

                                Box(
                                    modifier = Modifier.fillMaxWidth().offset(y = yPosPx.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.offset(y = (-180).dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.startQuiz(separator.ruleIds) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.offset(y = (-20).dp).padding(bottom = 16.dp)
                                        ) {
                                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Passer l'examen", style = MaterialTheme.typography.labelLarge)
                                        }

                                        Image(
                                            painter = painterResource(Res.drawable.toori),
                                            contentDescription = null,
                                            modifier = Modifier.size(280.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        
                                        val labelText = "$levelName (${separator.completionPercentage}%)"
                                        Text(
                                            text = labelText,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.offset(y = (-100).dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(4.dp)).padding(8.dp, 2.dp)
                                        )
                                    }
                                }
                            }

                            nodes.forEach { node ->
                                val xPos = viewportWidth.value * node.x
                                val yPos = canvasHeight.value * node.y
                                GrammarNodeItem(
                                    node = node,
                                    onLessonClick = { viewModel.openLesson(node) },
                                    onNodeClick = { viewModel.startQuiz(listOf(node.rule.id)) },
                                    modifier = Modifier.offset(x = (xPos - 50f).dp, y = (yPos - 30f).dp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (detectedLevelName.isNotEmpty()) {
                            val displayLevelName = ResourceUtils.resolveStringResource("level_${detectedLevelName}")?.let { 
                                stringResource(it) 
                            } ?: detectedLevelName.uppercase()
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f), CircleShape)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = displayLevelName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                             IconButton(
                                 onClick = { showFilterDialog = true },
                                 modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                             ) {
                                 Icon(Icons.Filled.FilterList, "Filter", tint = MaterialTheme.colorScheme.primary)
                             }
                        }
                    }
                    
                    if (selectedLessonHtml != null) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(enabled = false) {},
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .fillMaxSize(0.8f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val title = selectedLessonTitle?.let { key -> 
                                            ResourceUtils.resolveStringResource(key)?.let { stringResource(it) } ?: key 
                                        } ?: "Lesson"
                                        
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        IconButton(onClick = { viewModel.closeLesson() }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                    
                                    Box(modifier = Modifier.weight(1f)) {
                                        MochiWebView(
                                            html = selectedLessonHtml ?: "",
                                            isDarkMode = MaterialTheme.colorScheme.surface.toArgb() == Color.Black.toArgb() // Détection basique du dark mode
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val tags = selectedQuizTags
                    if (tags != null) {
                        val sessionKey = remember(tags) { "${tags.joinToString(",")}_${Clock.System.now().toEpochMilliseconds()}" }
                        val quizViewModel = quizViewModelFactory(tags, sessionKey)
                        if (quizViewModel != null) {
                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                GrammarQuizScreen(
                                    viewModel = quizViewModel,
                                    onBackClick = { viewModel.closeQuiz() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Categories") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (availableCategories.isEmpty()) {
                        Text("No categories found.")
                    } else {
                        availableCategories.forEach { category ->
                            val isSelected = selectedCategories.isEmpty() || selectedCategories.contains(category)
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleCategory(category) }.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = isSelected, onCheckedChange = { viewModel.toggleCategory(category) })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showFilterDialog = false }) { Text("Done") } },
            dismissButton = { TextButton(onClick = { viewModel.setCategories(emptySet()); showFilterDialog = false }) { Text("Clear All") } }
        )
    }
}

@Composable
fun GrammarNodeItem(node: GrammarNode, onLessonClick: () -> Unit, onNodeClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = ResourceUtils.resolveStringResource(node.rule.description)?.let { stringResource(it) } ?: node.rule.id
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val colorInt = ScorePresentationUtils.getScoreColor(node.score, baseColor.toArgb())
    val backgroundColor = Color(colorInt)

    Box(
        modifier = modifier
            .width(100.dp)
            .height(60.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable(onClick = onNodeClick)
            .padding(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 3, lineHeight = 12.sp)
        }
        
        if (node.hasLesson) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = -12.dp, y = (-12.dp))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onLessonClick)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Open Lesson",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private data class InternalConnInfo(
    val key: Pair<String, String>,
    val minY: Float,
    val maxY: Float,
    val length: Float,
    val isLeft: Boolean
)
