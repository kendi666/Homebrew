package com.brewmaster.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.LimeGreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh

@Composable
fun StepPreviewList(
    steps: List<BrewStep>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        steps.forEachIndexed { index, step ->
            val isLast = index == steps.lastIndex
            StepTimelineItem(
                step = step,
                isLast = isLast
            )
        }
    }
}

@Composable
private fun StepTimelineItem(
    step: BrewStep,
    isLast: Boolean
) {
    val lineColor = LimeGreen

    Row(modifier = Modifier.height(82.dp)) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 32f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = LimeGreen,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${step.order}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkCard,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = stepActionIcon(step.action),
                    contentDescription = step.action.label,
                    tint = LimeGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stepVolumeText(step),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun stepActionIcon(action: StepAction) = when (action) {
    StepAction.BLOOM -> Icons.Default.ArrowDropDown
    StepAction.POUR -> Icons.Default.ArrowDropDown
    StepAction.STIR -> Icons.Default.Refresh
    StepAction.SWIRL -> Icons.Default.Refresh
    StepAction.WAIT -> Icons.Default.ArrowDropDown
    StepAction.EXCAVATE -> Icons.Default.ArrowDropDown
    StepAction.PULSE -> Icons.Default.ArrowDropDown
    StepAction.OSMOTIC -> Icons.Default.ArrowDropDown
    StepAction.IMMERSE -> Icons.Default.ArrowDropDown
    StepAction.RELEASE -> Icons.Default.Refresh
}

private fun formatWeight(weight: Double): String =
    if (weight == weight.toLong().toDouble()) {
        weight.toLong().toString()
    } else {
        "%.1f".format(weight)
    }

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

private fun stepVolumeText(step: BrewStep): String {
    val timeRange = "${formatTime(step.startTimeSec)}–${formatTime(step.endTimeSec)}"
    return if (step.waterAmount > 0.0) {
        "+${formatWeight(step.waterAmount)}g -> total ${formatWeight(step.cumulativeWater)}g  ·  $timeRange"
    } else {
        "No water added -> total ${formatWeight(step.cumulativeWater)}g  ·  $timeRange"
    }
}
