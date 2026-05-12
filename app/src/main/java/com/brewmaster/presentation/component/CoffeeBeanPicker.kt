package com.brewmaster.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brewmaster.domain.model.CoffeeBean
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.LimeGreen

@Composable
fun CoffeeBeanPickerCard(
    selectedBean: CoffeeBean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = DarkCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (selectedBean != null) {
                    Text(
                        text = selectedBean.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!selectedBean.notes.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = selectedBean.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "Pilih Jenis Kopi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CoffeeBeanPickerSheet(
    beans: List<CoffeeBean>,
    selectedBean: CoffeeBean?,
    onSelect: (CoffeeBean?) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            text = "Jenis Kopi",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(8.dp))

        Surface(
            onClick = { onSelect(null) },
            shape = RoundedCornerShape(12.dp),
            color = if (selectedBean == null) {
                LimeGreen.copy(alpha = 0.12f)
            } else {
                DarkCard
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = "None (use technique defaults)",
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedBean == null) LimeGreen else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )
        }

        beans.forEach { bean ->
            val isSelected = bean.id == selectedBean?.id
            Surface(
                onClick = { onSelect(bean) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) LimeGreen.copy(alpha = 0.12f) else DarkCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = bean.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) LimeGreen else MaterialTheme.colorScheme.onSurface
                    )
                    if (!bean.notes.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = bean.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
