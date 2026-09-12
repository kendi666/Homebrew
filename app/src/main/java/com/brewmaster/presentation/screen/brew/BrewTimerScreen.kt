package com.brewmaster.presentation.screen.brew

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import com.brewmaster.presentation.component.CircularProgressTimer
import com.brewmaster.presentation.component.DynamicPhaseIndicator
import com.brewmaster.presentation.component.PostBrewCoachOverlay
import com.brewmaster.presentation.component.StopAlertOverlay
import com.brewmaster.service.BrewTimerService
import com.brewmaster.presentation.theme.DarkBackground
import com.brewmaster.presentation.theme.DarkBorder
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
        startTimerService(context, calculation.steps.firstOrNull()?.name.orEmpty())
    }

    LaunchedEffect(uiState.currentStepIndex) {
        uiState.currentStep?.let { step ->
            updateTimerServiceStep(context, step.name)
        }
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            stopTimerService(context)
        }
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
                    onBack = {
                        stopTimerService(context)
                        onNavigateBack()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                val step = uiState.currentStep
                val nextStep = calculation.steps.getOrNull(uiState.currentStepIndex + 1)
                CircularProgressTimer(
                    elapsedSeconds = uiState.elapsedSeconds,
                    stepProgress = uiState.stepProgress,
                    currentStepName = step?.name ?: "",
                    footer = when {
                        uiState.isFinished -> "done"
                        nextStep != null -> "next: ${nextStep.name} at ${formatClock(step?.endTimeSec ?: 0)}"
                        else -> "until ${formatClock(calculation.totalBrewTimeSec)}"
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (step != null) {
                    StepHeadline(step = step)
                    Spacer(modifier = Modifier.height(16.dp))
                    StatRow(
                        poured = pouredSoFar(calculation.steps, uiState.currentStepIndex, uiState.stepProgress),
                        target = step.cumulativeWater,
                        rate = targetRate(step)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DynamicPhaseIndicator(
                    steps = calculation.steps,
                    currentStepIndex = uiState.currentStepIndex,
                    elapsedSeconds = uiState.elapsedSeconds,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                BottomControls(
                    isRunning = uiState.isRunning,
                    isFinished = uiState.isFinished,
                    canGoBack = uiState.currentStepIndex > 0 || uiState.elapsedSeconds > 0,
                    canSkip = !uiState.isFinished,
                    onPause = viewModel::pauseTimer,
                    onResume = viewModel::resumeTimer,
                    onReset = viewModel::resetTimer,
                    onPrevious = viewModel::goToPreviousStep,
                    onSkip = viewModel::skipToNextStep
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
                    stopTimerService(context)
                    viewModel.dismissStopAlert()
                }
            )
        }

        AnimatedVisibility(
            visible = uiState.showCoach,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PostBrewCoachOverlay(
                elapsedSec = uiState.elapsedSeconds,
                targetSec = calculation?.totalBrewTimeSec ?: 0,
                adviceFor = viewModel::coachAdvice,
                onDone = {
                    viewModel.dismissCoach()
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
private fun StepHeadline(step: BrewStep) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (step.isWaterStep()) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "ADD ${formatWeight(step.waterAmount)} g",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "→ ${formatWeight(step.cumulativeWater)} g",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = step.instruction,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = step.instruction,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
        step.tip?.let { tip ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tip,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatRow(poured: Double, target: Double, rate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        StatCell(label = "POURED", value = "${formatWeight(poured)} g", modifier = Modifier.weight(1f))
        StatCell(label = "TARGET", value = "${formatWeight(target)} g", modifier = Modifier.weight(1f))
        StatCell(label = "RATE", value = rate, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary
        )
    }
}

@Composable
private fun BottomControls(
    isRunning: Boolean,
    isFinished: Boolean,
    canGoBack: Boolean,
    canSkip: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { if (isRunning) onPause() else onResume() },
                enabled = !isFinished,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = CircleShape,
                border = BorderStroke(1.5.dp, if (isFinished) DarkBorder else LimeGreen),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = LimeGreen,
                    disabledContentColor = TextMuted
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRunning) "Pause" else "Resume",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Button(
                onClick = onSkip,
                enabled = canSkip,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeGreen,
                    contentColor = DarkBackground,
                    disabledContainerColor = DarkCard,
                    disabledContentColor = TextMuted
                )
            ) {
                Text(
                    text = "Next step",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onPrevious, enabled = canGoBack) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = null,
                    tint = if (canGoBack) TextSecondary else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Prev step",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (canGoBack) TextSecondary else TextMuted
                )
            }
            Text(text = "·", color = TextMuted)
            TextButton(onClick = onReset) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reset",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

/** Water already poured, interpolating linearly inside a water step. */
private fun pouredSoFar(steps: List<BrewStep>, index: Int, stepProgress: Float): Double {
    val step = steps.getOrNull(index) ?: return 0.0
    if (!step.isWaterStep()) return step.cumulativeWater
    val before = step.cumulativeWater - step.waterAmount
    return before + step.waterAmount * stepProgress.coerceIn(0f, 1f)
}

private fun targetRate(step: BrewStep): String {
    if (!step.isWaterStep() || step.durationSec <= 0) return "—"
    return "%.1f g/s".format(step.waterAmount / step.durationSec)
}

private fun formatClock(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)

private fun startTimerService(context: Context, initialStepName: String) {
    val intent = Intent(context, BrewTimerService::class.java).apply {
        action = BrewTimerService.ACTION_START
        putExtra(BrewTimerService.EXTRA_STEP_NAME, initialStepName)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

private fun updateTimerServiceStep(context: Context, stepName: String) {
    val intent = Intent(context, BrewTimerService::class.java).apply {
        action = BrewTimerService.ACTION_UPDATE_STEP
        putExtra(BrewTimerService.EXTRA_STEP_NAME, stepName)
    }
    context.startService(intent)
}

private fun stopTimerService(context: Context) {
    val intent = Intent(context, BrewTimerService::class.java).apply {
        action = BrewTimerService.ACTION_STOP
    }
    context.startService(intent)
}

private fun BrewStep.isWaterStep(): Boolean {
    return waterAmount > 0.0 && action in setOf(
        StepAction.BLOOM,
        StepAction.POUR,
        StepAction.PULSE,
        StepAction.OSMOTIC,
        StepAction.IMMERSE
    )
}

private fun formatWeight(weight: Double): String =
    if (weight == weight.toLong().toDouble()) {
        weight.toLong().toString()
    } else {
        "%.1f".format(weight)
    }
