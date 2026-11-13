package com.brewmaster.presentation.screen.brew

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brewmaster.domain.model.BrewCalculation
import com.brewmaster.domain.model.BrewStep
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
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    // 3, 2, 1 before a new brew. 0 means the countdown is not showing.
    var readyCount by remember {
        mutableStateOf(
            if (viewModel.uiState.value.calculation == null && BrewSession.currentCalculation != null) 3 else 0
        )
    }

    // Keep the screen awake while getting ready or while the brew is live.
    val keepAwake = readyCount > 0 || (uiState.calculation != null && !uiState.isFinished && !uiState.autoPaused)
    DisposableEffect(view, keepAwake) {
        view.keepScreenOn = keepAwake
        onDispose { view.keepScreenOn = false }
    }

    val timerPrefs = remember { context.getSharedPreferences(BrewTimerService.PREFS, Context.MODE_PRIVATE) }
    var soundOn by remember { mutableStateOf(timerPrefs.getBoolean(BrewTimerService.PREF_STEP_SOUND, false)) }
    // Only used for the on-screen clock; the service beeps on its own.
    val toneGenerator = remember {
        runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, BrewTimerService.TONE_VOLUME) }.getOrNull()
    }
    DisposableEffect(toneGenerator) {
        onDispose { toneGenerator?.release() }
    }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val start = viewModel.takePendingStart()
        val calc = viewModel.uiState.value.calculation
        if (granted && calc != null) {
            viewModel.markUsingService()
            startTimerService(context, calc, start)
        } else {
            viewModel.beginLocalClock(start)
        }
    }

    LaunchedEffect(Unit) {
        val session = BrewSession.currentCalculation
        // The brew clock starts after this, so the 3 seconds are not part of the recipe.
        if (viewModel.uiState.value.calculation == null && session != null) {
            for (n in 3 downTo 1) {
                readyCount = n
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, BrewTimerService.TONE_MS)
                delay(1000)
            }
            readyCount = 0
        }
        // Null on rotation (the clock is already going) and when there is no brew.
        val start = viewModel.takeBrew(session) ?: return@LaunchedEffect
        val calc = viewModel.uiState.value.calculation ?: return@LaunchedEffect
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val granted = !needsPermission || ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.markUsingService()
            startTimerService(context, calc, start)
        } else {
            viewModel.holdPendingStart(start)
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(context) {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as? BrewTimerService.BrewTimerBinder)?.getService() ?: return
                scope.launch {
                    service.timerState.collect { clock ->
                        viewModel.onClock(
                            clock.elapsedSeconds,
                            clock.isRunning,
                            clock.sessionActive,
                            clock.autoPaused
                        )
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }
        context.bindService(
            Intent(context, BrewTimerService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        onDispose { context.unbindService(connection) }
    }

    val leaveBrew = {
        if (viewModel.uiState.value.usesService) {
            sendTimerAction(context, BrewTimerService.ACTION_STOP)
        }
        onNavigateBack()
    }
    BackHandler(onBack = leaveBrew)

    LaunchedEffect(Unit) {
        viewModel.stepTransitions.collect { event ->
            val pattern = when {
                event.isFinished -> BrewTimerService.FINISH_PATTERN
                event.isTargetReached -> BrewTimerService.TARGET_PATTERN
                else -> BrewTimerService.STEP_PATTERN
            }
            if (soundOn && !event.isFinished) {
                toneGenerator?.startTone(
                    if (event.isTargetReached) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_BEEP,
                    BrewTimerService.TONE_MS
                )
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
        if (readyCount > 0) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Be Ready",
                    color = TextSecondary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = readyCount.toString(),
                    color = LimeGreen,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 96.sp
                )
            }
        } else if (calculation == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No brew to show. Start one from the dashboard.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = leaveBrew,
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = DarkBackground)
                ) {
                    Text("Back to dashboard")
                }
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
                    onBack = leaveBrew,
                    soundOn = soundOn,
                    onToggleSound = {
                        soundOn = !soundOn
                        timerPrefs.edit().putBoolean(BrewTimerService.PREF_STEP_SOUND, soundOn).apply()
                        if (uiState.usesService) {
                            context.startService(
                                Intent(context, BrewTimerService::class.java)
                                    .setAction(BrewTimerService.ACTION_SET_SOUND)
                                    .putExtra(BrewTimerService.EXTRA_SOUND, soundOn)
                            )
                        }
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
                        uiState.isFinished -> "done ${formatClock(uiState.elapsedSeconds)}"
                        uiState.autoPaused -> "paused ${BrewTimerService.OVERRUN_LIMIT_SEC / 60} min past target"
                        uiState.elapsedSeconds >= calculation.totalBrewTimeSec -> "past target"
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
                    canSkip = viewModel.peekNextStartSec() != null,
                    onPause = {
                        if (uiState.usesService) sendTimerAction(context, BrewTimerService.ACTION_PAUSE)
                        else viewModel.pauseLocal()
                    },
                    onResume = {
                        if (uiState.usesService) sendTimerAction(context, BrewTimerService.ACTION_RESUME)
                        else viewModel.resumeLocal()
                    },
                    onReset = {
                        val wasFinished = uiState.isFinished
                        viewModel.prepareForClock()
                        when {
                            !uiState.usesService -> viewModel.resetLocal()
                            // Drawdown stopped the service, so start a paused one.
                            wasFinished -> startTimerService(context, calculation, ClockStart(0, paused = true))
                            else -> sendTimerAction(context, BrewTimerService.ACTION_RESET)
                        }
                    },
                    onPrevious = {
                        val target = viewModel.peekPreviousStartSec() ?: return@BottomControls
                        val wasFinished = uiState.isFinished
                        viewModel.prepareForClock()
                        when {
                            !uiState.usesService -> viewModel.seekLocal(target)
                            wasFinished -> startTimerService(context, calculation, ClockStart(target, paused = true))
                            else -> seekTimer(context, target)
                        }
                    },
                    onSkip = {
                        val target = viewModel.peekNextStartSec() ?: return@BottomControls
                        if (uiState.usesService) seekTimer(context, target) else viewModel.seekLocal(target)
                    },
                    onDrawdown = {
                        // Stop the service so no paused notification is left behind.
                        if (uiState.usesService) sendTimerAction(context, BrewTimerService.ACTION_STOP)
                        else viewModel.pauseLocal()
                        viewModel.finishDrawdown()
                    }
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
                onDismiss = viewModel::dismissStopAlert
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
                onDone = { rating, symptom ->
                    scope.launch {
                        // Save before leaving, so popping the screen cannot cancel the write.
                        if (rating != null) viewModel.saveToJournal(rating, symptom)
                        viewModel.dismissCoach()
                        leaveBrew()
                    }
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    techniqueName: String,
    totalTimeSec: Int,
    onBack: () -> Unit,
    soundOn: Boolean,
    onToggleSound: () -> Unit
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

        IconButton(onClick = onToggleSound) {
            Icon(
                imageVector = if (soundOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                contentDescription = if (soundOn) "Step sound on" else "Step sound off",
                tint = if (soundOn) LimeGreen else TextMuted
            )
        }
    }
}

@Composable
private fun StepHeadline(step: BrewStep) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (step.isPour) {
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
    onSkip: () -> Unit,
    onDrawdown: () -> Unit
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

        if (!isFinished) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDrawdown,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkCard,
                    contentColor = LimeGreen
                )
            ) {
                Text(
                    text = "Drawdown done",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
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
    if (!step.isPour) return step.cumulativeWater
    val before = step.cumulativeWater - step.waterAmount
    return before + step.waterAmount * stepProgress.coerceIn(0f, 1f)
}

private fun targetRate(step: BrewStep): String {
    if (!step.isPour || step.durationSec <= 0) return "—"
    return "%.1f g/s".format(step.waterAmount / step.durationSec)
}

private fun formatClock(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)

/** Starts the foreground clock with the whole step schedule, so it can run without the screen. */
private fun startTimerService(context: Context, calculation: BrewCalculation, start: ClockStart) {
    val intent = Intent(context, BrewTimerService::class.java).apply {
        action = BrewTimerService.ACTION_START
        putExtra(BrewTimerService.EXTRA_STEP_NAMES, calculation.steps.map { it.name }.toTypedArray())
        putExtra(BrewTimerService.EXTRA_STEP_STARTS, calculation.steps.map { it.startTimeSec }.toIntArray())
        putExtra(BrewTimerService.EXTRA_TARGET_SEC, calculation.totalBrewTimeSec)
        putExtra(BrewTimerService.EXTRA_ELAPSED_SEC, start.elapsedSec)
        putExtra(BrewTimerService.EXTRA_START_PAUSED, start.paused)
        putExtra(
            BrewTimerService.EXTRA_SOUND,
            context.getSharedPreferences(BrewTimerService.PREFS, Context.MODE_PRIVATE)
                .getBoolean(BrewTimerService.PREF_STEP_SOUND, false)
        )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

private fun sendTimerAction(context: Context, action: String) {
    context.startService(Intent(context, BrewTimerService::class.java).setAction(action))
}

private fun seekTimer(context: Context, elapsedSec: Int) {
    val intent = Intent(context, BrewTimerService::class.java).apply {
        action = BrewTimerService.ACTION_SEEK
        putExtra(BrewTimerService.EXTRA_ELAPSED_SEC, elapsedSec)
    }
    context.startService(intent)
}

private fun formatWeight(weight: Double): String =
    if (weight == weight.toLong().toDouble()) {
        weight.toLong().toString()
    } else {
        "%.1f".format(weight)
    }
