package org.nihongo.mochi.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*

enum class TooltipDirection { TOP, BOTTOM, LEFT, RIGHT }

data class TutorialStep(
    val text: String,
    val targetAnchor: Alignment = Alignment.Center,
    val tooltipDirection: TooltipDirection = TooltipDirection.TOP,
    val targetRect: IntRect? = null
)

@Composable
fun TutorialOverlay(
    steps: List<TutorialStep>,
    isVisible: Boolean,
    onFinished: () -> Unit
) {
    var currentStepIndex by remember { mutableStateOf(0) }

    if (!isVisible || steps.isEmpty() || currentStepIndex >= steps.size) return

    val currentStep = steps[currentStepIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (currentStepIndex < steps.size - 1) {
                    currentStepIndex++
                } else {
                    onFinished()
                }
            }
    ) {
        // Tooltip Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            TooltipBubble(
                step = currentStep,
                isLastStep = currentStepIndex == steps.size - 1,
                onNext = {
                    if (currentStepIndex < steps.size - 1) {
                        currentStepIndex++
                    } else {
                        onFinished()
                    }
                },
                onSkip = onFinished,
                modifier = Modifier.align(currentStep.targetAnchor)
            )
        }
    }
}

@Composable
private fun TooltipBubble(
    step: TutorialStep,
    isLastStep: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = step.text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSkip) {
                Text(
                    stringResource(Res.string.tutorial_skip),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isLastStep) stringResource(Res.string.tutorial_finish) else stringResource(Res.string.tutorial_next)
                )
            }
        }
    }
}
