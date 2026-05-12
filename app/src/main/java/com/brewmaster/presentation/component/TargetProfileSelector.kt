package com.brewmaster.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brewmaster.domain.model.TargetProfile
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.LimeGreen

@Composable
fun TargetProfileSelector(
    selected: TargetProfile,
    onSelect: (TargetProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val profiles = TargetProfile.entries
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileCard(
                profile = profiles[0],
                icon = Icons.Outlined.Tune,
                isSelected = selected == profiles[0],
                onClick = { onSelect(profiles[0]) },
                modifier = Modifier.weight(1f)
            )
            ProfileCard(
                profile = profiles[1],
                icon = Icons.Outlined.FavoriteBorder,
                isSelected = selected == profiles[1],
                onClick = { onSelect(profiles[1]) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileCard(
                profile = profiles[2],
                icon = Icons.Outlined.FlashOn,
                isSelected = selected == profiles[2],
                onClick = { onSelect(profiles[2]) },
                modifier = Modifier.weight(1f)
            )
            ProfileCard(
                profile = profiles[3],
                icon = Icons.Outlined.FitnessCenter,
                isSelected = selected == profiles[3],
                onClick = { onSelect(profiles[3]) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProfileCard(
    profile: TargetProfile,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) LimeGreen else DarkCard
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = profile.label,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = profile.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
