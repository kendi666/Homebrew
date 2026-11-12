package com.brewmaster.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brewmaster.domain.model.BrewStep
import com.brewmaster.presentation.theme.DarkBorder
import com.brewmaster.presentation.theme.IceBlue
import com.brewmaster.presentation.theme.TextMuted
import com.brewmaster.presentation.theme.TextSecondary

/**
 * Pour blocks sit on a timeline. Gaps are waits and drawdown, same idea as a
 * recipe chart that only paints the seconds you are actually pouring.
 */
@Composable
fun DynamicPhaseIndicator(
    steps: List<BrewStep>,
    currentStepIndex: Int,
    elapsedSeconds: Int,
    modifier: Modifier = Modifier
) {
    if (steps.isEmpty()) return

    val totalDuration = steps.sumOf { it.durationSec }.coerceAtLeast(1)
    val playhead = (elapsedSeconds.toFloat() / totalDuration).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DarkBorder)
            )
            Row(
                modifier = Modifier.fillMaxWidth().height(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    val weight = step.durationSec.toFloat() / totalDuration
                    val pour = step.isPour
                    val color = when {
                        !pour -> androidx.compose.ui.graphics.Color.Transparent
                        index < currentStepIndex -> IceBlue.copy(alpha = 0.45f)
                        index == currentStepIndex -> IceBlue
                        else -> IceBlue.copy(alpha = 0.28f)
                    }
                    Box(
                        modifier = Modifier
                            .weight(weight.coerceAtLeast(0.04f))
                            .height(if (pour) 22.dp else 28.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(playhead.coerceAtLeast(0.001f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = 1.dp)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(TextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            steps.forEachIndexed { index, step ->
                val weight = step.durationSec.toFloat() / totalDuration
                val labelColor = when {
                    index < currentStepIndex -> TextSecondary
                    index == currentStepIndex -> IceBlue
                    else -> TextMuted
                }
                Box(
                    modifier = Modifier.weight(weight.coerceAtLeast(0.04f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            step.isPour -> "${step.cumulativeWater.toInt()}g"
                            index == currentStepIndex -> "wait"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )
                }
            }
        }
    }
}
