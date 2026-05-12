package com.brewmaster.presentation.screen.cheatsheet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brewmaster.domain.model.CoffeeBean
import com.brewmaster.domain.model.CoffeeProcess
import com.brewmaster.presentation.theme.LimeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBeanDialog(
    processes: List<CoffeeProcess>,
    onDismiss: () -> Unit,
    onSave: (CoffeeBean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var selectedProcess by remember { mutableStateOf(processes.firstOrNull()) }
    var roastLevel by remember { mutableStateOf("Medium") }
    var notes by remember { mutableStateOf("") }
    var processExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Coffee Bean",
                fontWeight = FontWeight.Bold,
                color = LimeGreen
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bean Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Origin") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = processExpanded,
                    onExpandedChange = { processExpanded = !processExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProcess?.processName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Process") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = processExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = processExpanded,
                        onDismissRequest = { processExpanded = false }
                    ) {
                        processes.forEach { process ->
                            DropdownMenuItem(
                                text = { Text(process.processName) },
                                onClick = {
                                    selectedProcess = process
                                    processExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    "Roast Level",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Light", "Medium", "Dark").forEach { level ->
                        FilterChip(
                            selected = roastLevel == level,
                            onClick = { roastLevel = level },
                            label = { Text(level) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LimeGreen.copy(alpha = 0.2f),
                                selectedLabelColor = LimeGreen
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedProcess != null) {
                        onSave(
                            CoffeeBean(
                                name = name.trim(),
                                origin = origin.trim(),
                                processId = selectedProcess!!.id,
                                processName = selectedProcess!!.processName,
                                roastLevel = roastLevel,
                                notes = notes.trim().ifBlank { null }
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && selectedProcess != null,
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
