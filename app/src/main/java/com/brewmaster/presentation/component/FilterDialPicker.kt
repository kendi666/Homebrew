package com.brewmaster.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewmaster.domain.model.Grinder
import com.brewmaster.presentation.theme.DarkBackground
import com.brewmaster.presentation.theme.LimeGreen
import com.brewmaster.presentation.theme.TextMuted
import com.brewmaster.presentation.theme.TextPrimary
import com.brewmaster.presentation.theme.TextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.abs

/**
 * Height-picker style vertical ruler for filter dial settings.
 * Large value on the left, tick ruler on the right, accent center line.
 */
@Composable
fun FilterDialPicker(
    grinder: Grinder,
    selectedDial: String,
    onDialSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val labels = grinder.filterDialLabels()
    if (labels.isEmpty()) return

    val selectedIndex = labels.indexOf(selectedDial).let { if (it >= 0) it else labels.size / 2 }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val fling = rememberSnapFlingBehavior(lazyListState = listState)
    val tickHeight = 28.dp
    val pickerHeight = 220.dp
    val step = grinder.dialStep

    LaunchedEffect(grinder.id) {
        val idx = labels.indexOf(selectedDial).let { if (it >= 0) it else labels.size / 2 }
        listState.scrollToItem(idx)
    }

    LaunchedEffect(listState, labels) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { scrolling -> !scrolling }
            .map {
                val info = listState.layoutInfo
                val center = info.viewportStartOffset + info.viewportSize.height / 2
                info.visibleItemsInfo.minByOrNull { item ->
                    abs((item.offset + item.size / 2) - center)
                }?.index ?: listState.firstVisibleItemIndex
            }
            .distinctUntilChanged()
            .collect { index ->
                labels.getOrNull(index)?.let(onDialSelected)
            }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Filter dial · ${grinder.name}",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pickerHeight)
                .background(DarkBackground, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Big readout
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = selectedDial.ifBlank { labels[selectedIndex] },
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            lineHeight = 52.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "dial",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        text = "${grinder.filterDialMin}–${grinder.filterDialMax} filter only",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Ruler
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .fillMaxHeight()
                ) {
                    LazyColumn(
                        state = listState,
                        flingBehavior = fling,
                        contentPadding = PaddingValues(vertical = pickerHeight / 2 - tickHeight / 2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.End
                    ) {
                        itemsIndexed(labels, key = { _, label -> label }) { _, label ->
                            val value = label.toDoubleOrNull() ?: 0.0
                            val isMajor = isMajorTick(value, step)
                            val isHalf = isHalfTick(value, step)
                            RulerTick(
                                label = if (isMajor) label else null,
                                major = isMajor,
                                half = isHalf,
                                selected = label == selectedDial,
                                modifier = Modifier.height(tickHeight)
                            )
                        }
                    }

                    // Fade top/bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(DarkBackground, Color.Transparent)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, DarkBackground)
                                )
                            )
                    )
                }
            }

            // Center accent line across whole picker
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.Center)
            ) {
                drawLine(
                    color = LimeGreen,
                    start = Offset(24.dp.toPx(), size.height / 2),
                    end = Offset(size.width - 8.dp.toPx(), size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun RulerTick(
    label: String?,
    major: Boolean,
    half: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) LimeGreen else TextPrimary,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Canvas(
            modifier = Modifier
                .width(if (major) 48.dp else if (half) 32.dp else 18.dp)
                .height(2.dp)
        ) {
            drawLine(
                color = when {
                    selected -> LimeGreen
                    major -> Color.White.copy(alpha = 0.85f)
                    half -> Color.White.copy(alpha = 0.45f)
                    else -> Color.White.copy(alpha = 0.22f)
                },
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = if (major || selected) 2.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private fun isMajorTick(value: Double, step: Double): Boolean {
    if (step >= 1.0) return true
    val nearest = kotlin.math.round(value)
    return abs(value - nearest) < step * 0.01
}

private fun isHalfTick(value: Double, step: Double): Boolean {
    if (step >= 1.0) return false
    val half = kotlin.math.round(value * 2) / 2.0
    return abs(value - half) < step * 0.01 && !isMajorTick(value, step)
}
