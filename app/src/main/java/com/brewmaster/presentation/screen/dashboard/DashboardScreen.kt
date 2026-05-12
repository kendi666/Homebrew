package com.brewmaster.presentation.screen.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brewmaster.domain.model.BrewCalculation
import com.brewmaster.domain.model.BrewMode
import com.brewmaster.domain.model.CoffeeProcess
import com.brewmaster.presentation.component.BrewModeToggle
import com.brewmaster.presentation.component.CoffeeBeanPickerCard
import com.brewmaster.presentation.component.CoffeeBeanPickerSheet
import com.brewmaster.presentation.component.GrindSizeSelector
import com.brewmaster.presentation.component.ParameterCard
import com.brewmaster.presentation.component.StepPreviewList
import com.brewmaster.presentation.component.TargetProfileSelector
import com.brewmaster.presentation.component.TechniqueSelector
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.IceBlue
import com.brewmaster.presentation.theme.LimeGreen
import com.brewmaster.presentation.theme.DarkSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onStartBrewing: (BrewCalculation) -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToCheatSheet: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BrewMaster",
                style = MaterialTheme.typography.displayMedium,
                color = LimeGreen,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = onNavigateToCheatSheet,
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant
                ) {
                    Text(
                        text = "Cheat Sheet",
                        style = MaterialTheme.typography.labelMedium,
                        color = LimeGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Surface(
                    onClick = onNavigateToRecipes,
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant
                ) {
                    Text(
                        text = "Recipes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
        Text(
            text = "Craft your perfect cup",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 20.dp)
        )

        // --- Technique Selector ---
        SectionLabel("TECHNIQUE")
        Spacer(Modifier.height(8.dp))
        TechniqueSelector(
            techniques = state.techniques,
            selectedTechnique = state.selectedTechnique,
            onSelect = viewModel::onTechniqueSelected,
            modifier = Modifier.fillMaxWidth()
        )

        // --- Target Profile ---
        Spacer(Modifier.height(24.dp))
        SectionLabel("TARGET PROFILE")
        Spacer(Modifier.height(8.dp))
        TargetProfileSelector(
            selected = state.targetProfile,
            onSelect = viewModel::onTargetProfileSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // --- Jenis Kopi ---
        Spacer(Modifier.height(24.dp))
        SectionLabel("JENIS KOPI")
        Spacer(Modifier.height(8.dp))
        CoffeeBeanPickerCard(
            selectedBean = state.selectedBean,
            onClick = viewModel::onBeanPickerOpen,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // --- Parameters ---
        SectionLabel("PARAMETERS")
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ParameterCard(
                label = "Coffee Weight",
                value = state.coffeeWeight,
                onValueChange = viewModel::onCoffeeWeightChanged,
                suffix = "g",
                modifier = Modifier.weight(1f)
            )
            ParameterCard(
                label = "Brew Ratio",
                value = state.ratio,
                onValueChange = viewModel::onRatioChanged,
                prefix = "1:",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- Grind Size ---
        SectionLabel("GRIND SIZE")
        Spacer(Modifier.height(8.dp))
        GrindSizeSelector(
            selected = state.grindSize,
            onSelect = viewModel::onGrindSizeChanged,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // --- Coffee Process ---
        if (state.processes.isNotEmpty()) {
            SectionLabel("COFFEE PROCESS")
            Spacer(Modifier.height(8.dp))
            ProcessPickerCard(
                selectedProcess = state.selectedProcess,
                onClick = viewModel::onProcessPickerOpen,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        // --- Brew Mode ---
        SectionLabel("BREW MODE")
        Spacer(Modifier.height(8.dp))
        BrewModeToggle(
            mode = state.brewMode,
            onToggle = viewModel::onBrewModeToggled,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        AnimatedVisibility(
            visible = state.brewMode == BrewMode.ICE,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ParameterCard(
                label = "Ice Weight",
                value = state.iceWeight,
                onValueChange = viewModel::onIceWeightChanged,
                suffix = "g",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        // --- Calculation Preview ---
        val calc = state.calculation
        if (calc != null) {
            SectionLabel("BREW PREVIEW")
            Spacer(Modifier.height(8.dp))
            CalculationSummaryCard(
                calculation = calc,
                isIce = state.brewMode == BrewMode.ICE,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            if (calc.steps.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                SectionLabel("BREW STEPS")
                Spacer(Modifier.height(8.dp))
                StepPreviewList(
                    steps = calc.steps,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- Start Brewing Button ---
            Button(
                onClick = { onStartBrewing(calc) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "START BREWING",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    // --- Process Bottom Sheet ---
    if (state.showProcessPicker) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onProcessPickerDismissed,
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ProcessPickerSheet(
                processes = state.processes,
                selectedProcess = state.selectedProcess,
                onSelect = viewModel::onProcessSelected
            )
        }
    }

    // --- Bean Bottom Sheet ---
    if (state.showBeanPicker) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onBeanPickerDismissed,
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            CoffeeBeanPickerSheet(
                beans = state.beans,
                selectedBean = state.selectedBean,
                onSelect = viewModel::onBeanSelected
            )
        }
    }
}

// ─── Private composables ────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp),
        letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 1.5f
    )
}

@Composable
private fun ProcessPickerCard(
    selectedProcess: CoffeeProcess?,
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
                Text(
                    text = selectedProcess?.processName ?: "Select Process",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedProcess != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (selectedProcess != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = selectedProcess.extractionNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LimeGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${selectedProcess.tempMin}–${selectedProcess.tempMax}°C",
                            style = MaterialTheme.typography.labelMedium,
                            color = LimeGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select process",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProcessPickerSheet(
    processes: List<CoffeeProcess>,
    selectedProcess: CoffeeProcess?,
    onSelect: (CoffeeProcess?) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            text = "Coffee Process",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(8.dp))

        Surface(
            onClick = { onSelect(null) },
            shape = RoundedCornerShape(12.dp),
            color = if (selectedProcess == null) {
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
                color = if (selectedProcess == null) LimeGreen else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )
        }

        processes.forEach { process ->
            val isSelected = process.id == selectedProcess?.id
            Surface(
                onClick = { onSelect(process) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) LimeGreen.copy(alpha = 0.12f) else DarkCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = process.processName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) LimeGreen else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = LimeGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${process.tempMin}–${process.tempMax}°C",
                                style = MaterialTheme.typography.labelSmall,
                                color = LimeGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = process.extractionNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculationSummaryCard(
    calculation: BrewCalculation,
    isIce: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = DarkCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Total Water",
                    value = "${formatWeight(calculation.totalVolume)}g",
                    modifier = Modifier.weight(1f)
                )
                if (isIce) {
                    SummaryItem(
                        label = "Hot Water",
                        value = "${formatWeight(calculation.hotWaterVolume)}g",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryItem(
                        label = "Ice",
                        value = "${formatWeight(calculation.iceWeight)}g",
                        accent = IceBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Temperature",
                    value = "${calculation.tempMin}–${calculation.tempMax}°C",
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    label = "Grind",
                    value = calculation.grindSize.label,
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    label = "Time",
                    value = formatTotalTime(calculation.totalBrewTimeSec),
                    modifier = Modifier.weight(1f)
                )
            }
            if (calculation.processNote != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LimeGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = calculation.processNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = LimeGreen,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: androidx.compose.ui.graphics.Color = LimeGreen
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatWeight(weight: Double): String =
    if (weight == weight.toLong().toDouble()) {
        weight.toLong().toString()
    } else {
        "%.1f".format(weight)
    }

private fun formatTotalTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (s == 0) "${m}m" else "${m}m ${s}s"
}
