package com.brewmaster.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.brewmaster.BrewMasterApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ServiceTimerState(
    val elapsedSeconds: Int = 0,
    val currentStepName: String = "",
    val isRunning: Boolean = false,
    /** False until START, so a bind does not look like a paused brew. */
    val sessionActive: Boolean = false,
    /** True when the clock paused itself [BrewTimerService.OVERRUN_LIMIT_SEC] after the target. */
    val autoPaused: Boolean = false
)

/**
 * The brew clock. Elapsed time comes from [SystemClock.elapsedRealtime],
 * not from counting one-second delays.
 *
 * The service holds the step schedule, so the notification text, the step
 * vibration and the target vibration keep working when no screen is bound.
 */
class BrewTimerService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    private var accumulatedMs = 0L
    private var segmentStartElapsed = 0L
    private var running = false
    private var foreground = false

    private var stepNames: Array<String> = emptyArray()
    private var stepStarts: IntArray = IntArray(0)
    private var targetSec = 0
    private var lastStepIndex = -1
    private var targetAlerted = false
    private var soundOn = false
    private var toneGenerator: ToneGenerator? = null

    private val _timerState = MutableStateFlow(ServiceTimerState())
    val timerState: StateFlow<ServiceTimerState> = _timerState.asStateFlow()

    private val binder = BrewTimerBinder()

    inner class BrewTimerBinder : Binder() {
        fun getService(): BrewTimerService = this@BrewTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startSession(intent)
            ACTION_PAUSE -> pause(auto = false)
            ACTION_RESUME -> resume()
            ACTION_RESET -> reset()
            ACTION_SEEK -> seek(intent.getIntExtra(EXTRA_ELAPSED_SEC, 0))
            ACTION_STOP -> stopTimer()
            ACTION_SET_SOUND -> soundOn = intent.getBooleanExtra(EXTRA_SOUND, false)
            // A restart after process death has no schedule. Do not linger.
            null -> if (!_timerState.value.sessionActive) stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    private fun elapsedMs(): Long {
        val live = if (running) SystemClock.elapsedRealtime() - segmentStartElapsed else 0L
        return accumulatedMs + live.coerceAtLeast(0L)
    }

    private fun stepIndexAt(seconds: Int): Int {
        if (stepStarts.isEmpty()) return -1
        var index = 0
        for (i in stepStarts.indices) if (seconds >= stepStarts[i]) index = i
        return index
    }

    private fun publish() {
        val seconds = (elapsedMs() / 1000L).toInt()
        val index = stepIndexAt(seconds)
        if (running) {
            if (index != lastStepIndex && lastStepIndex >= 0) {
                vibrate(STEP_PATTERN)
                beep(ToneGenerator.TONE_PROP_BEEP)
            }
            if (!targetAlerted && targetSec > 0 && seconds >= targetSec) {
                targetAlerted = true
                vibrate(TARGET_PATTERN)
                beep(ToneGenerator.TONE_PROP_BEEP2)
            }
        }
        lastStepIndex = index
        _timerState.value = ServiceTimerState(
            elapsedSeconds = seconds,
            currentStepName = stepNames.getOrNull(index).orEmpty(),
            isRunning = running,
            sessionActive = true,
            autoPaused = _timerState.value.autoPaused && !running
        )
        updateNotification()
        if (running && targetSec > 0 && seconds >= targetSec + OVERRUN_LIMIT_SEC) {
            pause(auto = true)
        }
    }

    private fun startSession(intent: Intent) {
        stepNames = intent.getStringArrayExtra(EXTRA_STEP_NAMES) ?: emptyArray()
        stepStarts = intent.getIntArrayExtra(EXTRA_STEP_STARTS) ?: IntArray(0)
        targetSec = intent.getIntExtra(EXTRA_TARGET_SEC, 0)
        val startSec = intent.getIntExtra(EXTRA_ELAPSED_SEC, 0).coerceAtLeast(0)
        val startPaused = intent.getBooleanExtra(EXTRA_START_PAUSED, false)
        soundOn = intent.getBooleanExtra(EXTRA_SOUND, false)

        timerJob?.cancel()
        accumulatedMs = startSec * 1000L
        running = !startPaused
        segmentStartElapsed = SystemClock.elapsedRealtime()
        targetAlerted = targetSec in 1..startSec
        lastStepIndex = stepIndexAt(startSec)
        _timerState.value = ServiceTimerState(sessionActive = true)
        startForegroundNotification()
        if (running) startTimerLoop() else publish()
    }

    private fun pause(auto: Boolean) {
        if (!running) return
        accumulatedMs = elapsedMs()
        running = false
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(autoPaused = auto)
        publish()
    }

    private fun resume() {
        if (running || !_timerState.value.sessionActive) return
        running = true
        segmentStartElapsed = SystemClock.elapsedRealtime()
        _timerState.value = _timerState.value.copy(autoPaused = false)
        startTimerLoop()
    }

    private fun reset() {
        timerJob?.cancel()
        accumulatedMs = 0L
        running = false
        targetAlerted = false
        lastStepIndex = stepIndexAt(0)
        _timerState.value = _timerState.value.copy(autoPaused = false)
        publish()
    }

    private fun seek(seconds: Int) {
        if (!_timerState.value.sessionActive) return
        val target = seconds.coerceAtLeast(0)
        accumulatedMs = target * 1000L
        if (running) segmentStartElapsed = SystemClock.elapsedRealtime()
        if (target < targetSec) targetAlerted = false
        // A manual jump should not buzz like an automatic step change.
        lastStepIndex = stepIndexAt(target)
        publish()
    }

    fun stopTimer() {
        timerJob?.cancel()
        running = false
        accumulatedMs = 0L
        _timerState.value = ServiceTimerState()
        if (foreground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            foreground = false
        }
        stopSelf()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && running) {
                publish()
                delay(TICK_MS)
            }
        }
    }

    private fun vibrate(pattern: LongArray) {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator?.hasVibrator() != true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun beep(tone: Int) {
        if (!soundOn) return
        val generator = toneGenerator ?: runCatching {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, TONE_VOLUME)
        }.getOrNull()?.also { toneGenerator = it } ?: return
        generator.startTone(tone, TONE_MS)
    }

    private fun startForegroundNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        foreground = true
    }

    private fun updateNotification() {
        if (!foreground) return
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): android.app.Notification {
        val state = _timerState.value
        val timeText = "%02d:%02d".format(state.elapsedSeconds / 60, state.elapsedSeconds % 60)
        val pastTarget = targetSec > 0 && state.elapsedSeconds >= targetSec
        val text = when {
            state.autoPaused -> "Paused ${OVERRUN_LIMIT_SEC / 60} min after target | $timeText"
            !state.isRunning -> "Paused: ${state.currentStepName} | $timeText"
            pastTarget -> "Past target, tap Drawdown done | $timeText"
            else -> "Step: ${state.currentStepName} | $timeText"
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, BrewMasterApp.TIMER_CHANNEL_ID)
            .setContentTitle("Homebrew - Brewing")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(state.isRunning)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        toneGenerator?.release()
        toneGenerator = null
        timerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.brewmaster.action.START_TIMER"
        const val ACTION_PAUSE = "com.brewmaster.action.PAUSE_TIMER"
        const val ACTION_RESUME = "com.brewmaster.action.RESUME_TIMER"
        const val ACTION_RESET = "com.brewmaster.action.RESET_TIMER"
        const val ACTION_SEEK = "com.brewmaster.action.SEEK_TIMER"
        const val ACTION_STOP = "com.brewmaster.action.STOP_TIMER"
        const val EXTRA_ELAPSED_SEC = "extra_elapsed_sec"
        const val EXTRA_START_PAUSED = "extra_start_paused"
        const val EXTRA_STEP_NAMES = "extra_step_names"
        const val EXTRA_STEP_STARTS = "extra_step_starts"
        const val EXTRA_TARGET_SEC = "extra_target_sec"
        const val ACTION_SET_SOUND = "com.brewmaster.action.SET_SOUND"
        const val EXTRA_SOUND = "extra_sound"

        /** Saved choice for the step beep, shared with the timer screen. */
        const val PREFS = "timer_prefs"
        const val PREF_STEP_SOUND = "step_sound"
        const val TONE_VOLUME = 80
        const val TONE_MS = 250

        /** The clock pauses itself this long after the target if drawdown is never tapped. */
        const val OVERRUN_LIMIT_SEC = 300

        val STEP_PATTERN = longArrayOf(0, 150, 50, 150)
        val TARGET_PATTERN = longArrayOf(0, 300, 100, 300)
        val FINISH_PATTERN = longArrayOf(0, 300, 100, 300, 100, 500)

        private const val NOTIFICATION_ID = 1001
        private const val TICK_MS = 200L
    }
}
