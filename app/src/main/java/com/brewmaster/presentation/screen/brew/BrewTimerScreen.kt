package com.brewmaster.presentation.screen.brew

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brewmaster.domain.model.StepAction
import com.brewmaster.presentation.component.CircularProgressTimer
import com.brewmaster.presentation.component.DynamicPhaseIndicator
import com.brewmaster.presentation.component.StopAlertOverlay
import com.brewmaster.presentation.theme.DarkBackground
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.LimeGreen
import com.brewmaster.presentation.theme.TextMuted
import com.brewmaster.presentation.theme.TextPrimary
import com.brewmaster.presentation.theme.TextSecondary

@Composable
fun BrewTimerScreen(
    onNavigateBack: () -> Unit,
    viewModel: BrewTimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    LaunchedEffect(Unit) {
        val calculation = BrewSession.currentCalculation ?: return@LaunchedEffect
        viewModel.startBrew(calculation)
    }

    LaunchedEffect(Unit) {
        viewModel.stepTransitions.collect { event ->
            val pattern = if (event.isFinished) {
                longArrayOf(0, 300, 100, 300, 100, 500)
            } else {
                longArrayOf(0, 150, 50, 150)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    val calculation = uiState.calculation

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (calculation == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No brew data available",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopBar(
                    techniqueName = calculation.technique.name,
                    totalTimeSec = calculation.totalBrewTimeSec,
                    onBack = onNavigateBack
                )

                Spacer(modifier = Modifier.height(24.dp))

                CircularProgressTimer(
                    elapsedSeconds = uiState.elapsedSeconds,
                    totalSeconds = calculation.totalBrewTimeSec,
                    currentStepName = uiState.currentStep?.name ?: ""
                )

                Spacer(modifier = Modifier.height(24.dp))

                DynamicPhaseIndicator(
                    steps = calculation.steps,
                    currentStepIndex = uiState.currentStepIndex,
                    elapsedSeconds = uiState.elapsedSeconds,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                uiState.currentStep?.let { step ->
                    InstructionCard(step = step)
                }

                Spacer(modifier = Modifier.weight(1f))

                BottomControls(
                    isRunning = uiState.isRunning,
                    isPaused = uiState.isPaused,
                    isFinished = uiState.isFinished,
                    onPause = viewModel::pauseTimer,
                    onResume = viewModel::resumeTimer,
                    onReset = viewModel::resetTimer
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.showStopAlert,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            StopAlertOverlay(
                totalVolume = calculation?.totalVolume ?: 0.0,
                onDismiss = {
                    viewModel.dismissStopAlert()
                    onNavigateBack()
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    techniqueName: String,
    totalTimeSec: Int,
    onBack: () -> Unit
) {
    val totalMinutes = totalTimeSec / 60
    val totalSeconds = totalTimeSec % 60
    val totalTimeText = "%d:%02d".format(totalMinutes, totalSeconds)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = techniqueName,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        Text(
            text = totalTimeText,
            style = MaterialTheme.typography.titleMedium,
            color = TextMuted
        )
    }
}

@Composable
private fun InstructionCard(step: com.brewmaster.domain.model.BrewStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = actionIcon(step.action),
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (step.isWaterStep()) {
                Text(
                    text = "ADD ${formatWeight(step.waterAmount)}g",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = LimeGreen,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Target total: ${formatWeight(step.cumulativeWater)}g",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text(
                    text = "Current total: ${formatWeight(step.cumulativeWater)}g",
                    style = MaterialTheme.typography.titleMedium,
                    color = LimeGreen,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = step.instruction,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            step.tip?.let { tip ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BottomControls(
    isRunning: Boolean,
    isPaused: Boolean,
    isFinished: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        FilledIconButton(
            onClick = onReset,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = DarkCard,
                contentColor = TextSecondary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset",
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        FilledIconButton(
            onClick = {
                if (isRunning) onPause() else onResume()
            },
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            enabled = !isFinished,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = LimeGreen,
                contentColor = DarkBackground
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Pause" else "Resume",
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        Spacer(modifier = Modifier.size(48.dp))
    }
}

private fun actionIcon(action: StepAction): String = when (action) {
    StepAction.BLOOM -> "💧"
    StepAction.POUR -> "🌊"
    StepAction.STIR -> "🔄"
    StepAction.SWIRL -> "🌀"
    StepAction.WAIT -> "⏳"
    StepAction.EXCAVATE -> "⛏️"
    StepAction.PULSE -> "📊"
    StepAction.OSMOTIC -> "💦"
}

private fun com.brewmaster.domain.model.BrewStep.isWaterStep(): Boolean {
    return waterAmount > 0.0 && action in setOf(
        StepAction.BLOOM,
        StepAction.POUR,
        StepAction.PULSE,
        StepAction.OSMOTIC
    )
}

private fun formatWeight(weight: Double): String =
    if (weight == weight.toLong().toDouble()) {
        weight.toLong().toString()
    } else {
        "%.1f".format(weight)
    }
