package com.brewmaster.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brewmaster.domain.model.Grinder
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.LimeGreen

/**
 * Horizontal chips to pick a grinder, plus a hint showing the approximate click /
 * step setting for the currently selected grind size.
 */
@Composable
fun GrinderSelector(
    grinders: List<Grinder>,
    selectedGrinder: Grinder?,
    grindSize: GrindSize,
    onSelect: (Grinder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(grinders, key = { it.id }) { grinder ->
                val isSelected = grinder.id == selectedGrinder?.id
                Surface(
                    onClick = { onSelect(grinder) },
                    shape = RoundedCornerShape(12.dp),
                    color = DarkCard,
                    border = if (isSelected) BorderStroke(2.dp, LimeGreen) else null
                ) {
                    Text(
                        text = grinder.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) LimeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        if (selectedGrinder != null) {
            Text(
                text = "${selectedGrinder.name}: ${selectedGrinder.settingLabel(grindSize.microns)} " +
                    "(${grindSize.label}) — approx, dial to taste",
                style = MaterialTheme.typography.bodySmall,
                color = LimeGreen,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
            )
        }
    }
}
