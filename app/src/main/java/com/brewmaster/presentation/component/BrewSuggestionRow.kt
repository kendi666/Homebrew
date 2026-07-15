package com.brewmaster.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brewmaster.domain.model.BrewSuggestion
import com.brewmaster.presentation.theme.DarkBackground
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.LimeGreen

@Composable
fun BrewSuggestionRow(
    suggestions: List<BrewSuggestion>,
    selectedTechniqueId: String?,
    onSelect: (BrewSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Suggested from grind + profile",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(suggestions, key = { _, s -> s.technique.id }) { index, suggestion ->
                val isBest = index == 0
                val selected = suggestion.technique.id == selectedTechniqueId
                val highlighted = isBest || selected
                Surface(
                    onClick = { onSelect(suggestion) },
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        selected -> LimeGreen.copy(alpha = 0.22f)
                        isBest -> LimeGreen.copy(alpha = 0.12f)
                        else -> DarkCard
                    },
                    border = BorderStroke(
                        width = if (highlighted) 2.dp else 1.dp,
                        color = when {
                            selected -> LimeGreen
                            isBest -> LimeGreen.copy(alpha = 0.75f)
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isBest) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = LimeGreen,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "BEST",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                            Text(
                                text = suggestion.technique.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (highlighted) LimeGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${suggestion.tempMin}–${suggestion.tempMax}°C · ${suggestion.reason}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (highlighted) {
                                LimeGreen.copy(alpha = 0.85f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
