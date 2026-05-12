package com.brewmaster.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.brewmaster.presentation.theme.DarkSurfaceVariant
import com.brewmaster.presentation.theme.LimeGreen
import com.brewmaster.presentation.theme.LimeGreenDark
import com.brewmaster.presentation.theme.TextMuted
import com.brewmaster.presentation.theme.TextSecondary

@Composable
fun DynamicPhaseIndicator(
    steps: List<BrewStep>,
    currentStepIndex: Int,
    elapsedSeconds: Int,
    modifier: Modifier = Modifier
) {
    if (steps.isEmpty()) return

    val totalDuration = steps.sumOf { it.durationSec }.coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            steps.forEachIndexed { index, step ->
                val weight = step.durationSec.toFloat() / totalDuration
                val color = when {
                    index < currentStepIndex -> LimeGreenDark
                    index == currentStepIndex -> LimeGreen
                    else -> DarkSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .weight(weight.coerceAtLeast(0.05f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            steps.forEachIndexed { index, step ->
                val weight = step.durationSec.toFloat() / totalDuration
                val labelColor = when {
                    index < currentStepIndex -> TextSecondary
                    index == currentStepIndex -> LimeGreen
                    else -> TextMuted
                }

                Box(
                    modifier = Modifier.weight(weight.coerceAtLeast(0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.name,
                        style = MaterialTheme.typography.labelMedium,
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
