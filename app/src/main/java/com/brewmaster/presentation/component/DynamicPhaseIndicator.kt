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
import com.brewmaster.presentation.theme.HotOrange
import com.brewmaster.presentation.theme.IceBlue
import com.brewmaster.presentation.theme.TextMuted
import com.brewmaster.presentation.theme.TextSecondary

/**
 * Blue blocks are pours. Orange blocks are the switch valve opening.
 * Gaps are closed waits and drawdown.
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
                    val marked = step.isPour || step.isValveOpen
                    val base = if (step.isValveOpen) HotOrange else IceBlue
                    val color = when {
                        !marked -> androidx.compose.ui.graphics.Color.Transparent
                        index < currentStepIndex -> base.copy(alpha = 0.45f)
                        index == currentStepIndex -> base
                        else -> base.copy(alpha = 0.28f)
                    }
                    Box(
                        modifier = Modifier
                            .weight(step.durationSec.toFloat().coerceAtLeast(0.001f))
                            .height(if (marked) 22.dp else 28.dp)
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
            var index = 0
            while (index < steps.size) {
                val step = steps[index]
                val marked = step.isPour || step.isValveOpen
                if (!marked) {
                    Box(modifier = Modifier.weight(step.durationSec.toFloat().coerceAtLeast(0.001f)))
                    index++
                    continue
                }
                var span = step.durationSec
                var end = index + 1
                while (end < steps.size && !steps[end].isPour && !steps[end].isValveOpen) {
                    span += steps[end].durationSec
                    end++
                }
                val inPhase = currentStepIndex in index until end
                val active = if (step.isValveOpen) HotOrange else IceBlue
                val labelColor = when {
                    inPhase -> active
                    index < currentStepIndex -> TextSecondary
                    else -> TextMuted
                }
                Box(
                    modifier = Modifier.weight(span.toFloat().coerceAtLeast(0.001f)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (step.isValveOpen) "open" else "${step.cumulativeWater.toInt()}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
                index = end
            }
        }
    }
}
