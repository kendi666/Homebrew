package com.brewmaster.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brewmaster.domain.model.CustomStepConfig
import com.brewmaster.domain.model.StepAction
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.LimeGreen
import com.brewmaster.presentation.theme.DarkSurfaceVariant

@Composable
fun CustomTechniqueEditor(
    tempMin: Int,
    tempMax: Int,
    steps: List<CustomStepConfig>,
    onTempMinChanged: (Int) -> Unit,
    onTempMaxChanged: (Int) -> Unit,
    onAddStep: (CustomStepConfig) -> Unit,
    onUpdateStep: (Int, CustomStepConfig) -> Unit,
    onRemoveStep: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStepDialog by remember { mutableStateOf(false) }
    var editingStepIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier) {
        // Temperature Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TempControlCard(
                label = "Temp Min",
                value = tempMin,
                onValueChange = onTempMinChanged,
                modifier = Modifier.weight(1f)
            )
            TempControlCard(
                label = "Temp Max",
                value = tempMax,
                onValueChange = onTempMaxChanged,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Steps List
        Text(
            text = "CUSTOM STEPS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        steps.forEachIndexed { index, step ->
            CustomStepItem(
                step = step,
                onEdit = {
                    editingStepIndex = index
                    showStepDialog = true
                },
                onDelete = { onRemoveStep(index) }
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = {
                editingStepIndex = null
                showStepDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = LimeGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Step")
            Spacer(Modifier.width(8.dp))
            Text("ADD STEP")
        }
    }

    if (showStepDialog) {
        val initialStep = editingStepIndex?.let { steps[it] } ?: CustomStepConfig(StepAction.POUR, 30, 0.20)
        CustomStepDialog(
            initialStep = initialStep,
            onDismiss = { showStepDialog = false },
            onSave = { step ->
                if (editingStepIndex != null) {
                    onUpdateStep(editingStepIndex!!, step)
                } else {
                    onAddStep(step)
                }
                showStepDialog = false
            }
        )
    }
}

@Composable
private fun TempControlCard(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = DarkCard,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onValueChange((value - 1).coerceAtLeast(80)) }) {
                    Text("-", style = MaterialTheme.typography.titleLarge, color = LimeGreen)
                }
                Text(
                    text = "$value°C",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { onValueChange((value + 1).coerceAtMost(100)) }) {
                    Text("+", style = MaterialTheme.typography.titleLarge, color = LimeGreen)
                }
            }
        }
    }
}

@Composable
private fun CustomStepItem(
    step: CustomStepConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.action.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LimeGreen
                )
                Text(
                    text = "${step.durationSec}s • ${(step.waterPercentage * 100).toInt()}% water",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomStepDialog(
    initialStep: CustomStepConfig,
    onDismiss: () -> Unit,
    onSave: (CustomStepConfig) -> Unit
) {
    var action by remember { mutableStateOf(initialStep.action) }
    var duration by remember { mutableStateOf(initialStep.durationSec.toString()) }
    var percentage by remember { mutableStateOf((initialStep.waterPercentage * 100).toInt().toString()) }
    var actionDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Step") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = actionDropdownExpanded,
                    onExpandedChange = { actionDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = action.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Action") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = actionDropdownExpanded,
                        onDismissRequest = { actionDropdownExpanded = false }
                    ) {
                        StepAction.entries.forEach { stepAction ->
                            DropdownMenuItem(
                                text = { Text(stepAction.label) },
                                onClick = {
                                    action = stepAction
                                    actionDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (seconds)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = percentage,
                    onValueChange = { percentage = it },
                    label = { Text("Water Percentage (0-100)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val dur = duration.toIntOrNull() ?: 30
                val perc = (percentage.toDoubleOrNull() ?: 20.0) / 100.0
                onSave(CustomStepConfig(action, dur, perc))
            }) {
                Text("Save", color = LimeGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
