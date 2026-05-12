package com.brewmaster.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brewmaster.domain.model.BrewMode
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.HotOrange
import com.brewmaster.presentation.theme.IceBlue

@Composable
fun BrewModeToggle(
    mode: BrewMode,
    onToggle: (BrewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = { onToggle(BrewMode.HOT) },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (mode == BrewMode.HOT) HotOrange else DarkCard,
                contentColor = if (mode == BrewMode.HOT) {
                    MaterialTheme.colorScheme.onTertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        ) {
            Text(
                text = "HOT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = { onToggle(BrewMode.ICE) },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (mode == BrewMode.ICE) IceBlue else DarkCard,
                contentColor = if (mode == BrewMode.ICE) {
                    MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        ) {
            Text(
                text = "ICE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
