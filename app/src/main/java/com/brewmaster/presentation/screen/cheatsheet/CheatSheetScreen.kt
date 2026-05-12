package com.brewmaster.presentation.screen.cheatsheet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brewmaster.domain.model.CoffeeBean
import com.brewmaster.domain.model.CoffeeProcess
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.LimeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheatSheetScreen(
    onNavigateBack: () -> Unit,
    viewModel: CheatSheetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Brewing Cheat Sheet",
                        fontWeight = FontWeight.Bold,
                        color = LimeGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onAddBeanOpen,
                containerColor = LimeGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bean")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "PROCESS REFERENCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 1.5f
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LimeGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Process", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LimeGreen, modifier = Modifier.weight(1.5f))
                        Text("Rest", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LimeGreen, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
                        Text("Temp", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LimeGreen, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
                        Text("Grind", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LimeGreen, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text("Ratio", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LimeGreen, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
                    }
                }
            }

            items(state.processes) { process ->
                ProcessRow(process)
            }

            item {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Special Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = LimeGreen
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Untuk kopi dengan densitas tinggi seperti Ethiopia dan Kenya, bisa buat lebih coarse daripada kopi lainnya.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Data resting diatas adalah subjective menurut dari pengalaman kami selama ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "MY COFFEE BEANS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 1.5f
                )
            }

            if (state.beans.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkCard
                    ) {
                        Text(
                            text = "No beans yet. Tap + to add your coffee beans.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(state.beans) { bean ->
                    BeanCard(bean = bean, onDelete = { viewModel.onDeleteBean(bean.id) })
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (state.showAddBeanDialog) {
        AddBeanDialog(
            processes = state.processes,
            onDismiss = viewModel::onAddBeanDismissed,
            onSave = viewModel::onSaveBean
        )
    }
}

@Composable
private fun ProcessRow(process: CoffeeProcess) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = process.processName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1.5f)
            )
            Text(
                text = "${process.restingDays}d",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.8f)
            )
            Text(
                text = "${process.tempMin}-${process.tempMax}",
                style = MaterialTheme.typography.bodySmall,
                color = LimeGreen,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.8f)
            )
            Text(
                text = process.grindRecommendation.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "1:${process.ratioMin.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.8f)
            )
        }
    }
}

@Composable
private fun BeanCard(bean: CoffeeBean, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bean.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${bean.origin} \u2022 ${bean.roastLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LimeGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = bean.processName,
                            style = MaterialTheme.typography.labelSmall,
                            color = LimeGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (bean.restingDays != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "Rest ${bean.restingDays}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                if (!bean.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = bean.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
