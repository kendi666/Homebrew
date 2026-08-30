package com.brewmaster.presentation.screen.convert

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brewmaster.domain.model.ConvertedGrindDial
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.LimeGreen
import com.brewmaster.presentation.theme.TextMuted
import com.brewmaster.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrindConvertScreen(
    onNavigateBack: () -> Unit,
    viewModel: GrindConvertViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val filteredTargets = state.result?.targets.orEmpty().let { targets ->
        val brand = state.brandFilter
        if (brand == null) targets else targets.filter { it.profile.brand == brand }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Grind Convert",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "FM 120 micron bridge: K-Ultra / Guerrero 64 Pro / Sculptor are calibrated. C3, C5, and JX-S share the same µm estimate — dial to taste.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Text(
                    text = "FROM GRINDER",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.profiles, key = { it.id }) { profile ->
                        val selected = profile.id == state.sourceId
                        Surface(
                            onClick = { viewModel.onSourceSelected(profile) },
                            shape = RoundedCornerShape(12.dp),
                            color = DarkCard,
                            border = if (selected) BorderStroke(2.dp, LimeGreen) else null
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = profile.brand,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) LimeGreen else TextMuted
                                )
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) LimeGreen else MaterialTheme.colorScheme.onSurface
                                )
                                if (profile.calibrated) {
                                    Text(
                                        text = "CALIBRATED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LimeGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                val source = state.source
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "DIAL / ${source?.unitLabel?.uppercase() ?: "SETTING"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledIconButton(
                            onClick = { viewModel.nudgeDial(-1) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = DarkCard,
                                contentColor = LimeGreen
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        OutlinedTextField(
                            value = state.dialText,
                            onValueChange = viewModel::onDialChanged,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LimeGreen,
                                cursorColor = LimeGreen,
                                focusedLabelColor = LimeGreen
                            ),
                            supportingText = {
                                if (source != null) {
                                    Text("${source.dialMin}–${source.dialMax} · step ${source.dialStep}")
                                }
                            }
                        )
                        FilledIconButton(
                            onClick = { viewModel.nudgeDial(1) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = DarkCard,
                                contentColor = LimeGreen
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                    source?.note?.let { note ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            item {
                state.result?.let { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Estimated particle size",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted
                            )
                            Text(
                                text = "${formatMicron(result.estimatedMicron)} µm",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = LimeGreen
                            )
                            Text(
                                text = "${result.source.brand} ${result.source.name} @ ${result.source.formatDial(result.sourceDial)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "EQUIVALENT ON OTHER GRINDERS",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BrandChip(
                        label = "All",
                        selected = state.brandFilter == null,
                        onClick = { viewModel.onBrandFilterSelected(null) }
                    )
                    state.brands.forEach { brand ->
                        BrandChip(
                            label = brand,
                            selected = state.brandFilter == brand,
                            onClick = { viewModel.onBrandFilterSelected(brand) }
                        )
                    }
                }
            }

            items(filteredTargets, key = { it.profile.id }) { converted ->
                ConversionRow(
                    converted = converted,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun BrandChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) LimeGreen.copy(alpha = 0.2f) else DarkCard,
        border = if (selected) BorderStroke(1.dp, LimeGreen) else null
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) LimeGreen else TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ConversionRow(
    converted: ConvertedGrindDial,
    modifier: Modifier = Modifier
) {
    val profile = converted.profile
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.brand,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (profile.calibrated) "calibrated" else "approx",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (profile.calibrated) LimeGreen else TextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = profile.formatDial(converted.dial),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LimeGreen
                )
                Text(
                    text = profile.unitLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

private fun formatMicron(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", value)
    }
}
