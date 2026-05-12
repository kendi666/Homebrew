package com.brewmaster.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServiceTimerState(
    val elapsedSeconds: Int = 0,
    val currentStepName: String = "",
    val isRunning: Boolean = false
)

class BrewTimerService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    private val _timerState = MutableStateFlow(ServiceTimerState())
    val timerState: StateFlow<ServiceTimerState> = _timerState.asStateFlow()

    private val binder = BrewTimerBinder()

    inner class BrewTimerBinder : Binder() {
        fun getService(): BrewTimerService = this@BrewTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val stepName = intent.getStringExtra(EXTRA_STEP_NAME) ?: ""
                startTimer(stepName)
            }
            ACTION_UPDATE_STEP -> {
                val stepName = intent.getStringExtra(EXTRA_STEP_NAME) ?: ""
                _timerState.update { it.copy(currentStepName = stepName) }
            }
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    private fun startTimer(initialStepName: String) {
        _timerState.update {
            ServiceTimerState(
                elapsedSeconds = 0,
                currentStepName = initialStepName,
                isRunning = true
            )
        }
        startForegroundNotification()
        startTimerLoop()
    }

    fun stopTimer() {
        timerJob?.cancel()
        _timerState.update { it.copy(isRunning = false) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000L)
                _timerState.update { state ->
                    val newElapsed = state.elapsedSeconds + 1
                    state.copy(elapsedSeconds = newElapsed)
                }
                updateNotification()
            }
        }
    }

    private fun startForegroundNotification() {
        val notification = buildNotification(
            stepName = _timerState.value.currentStepName,
            elapsedSeconds = _timerState.value.elapsedSeconds
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        val state = _timerState.value
        val notification = buildNotification(
            stepName = state.currentStepName,
            elapsedSeconds = state.elapsedSeconds
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(stepName: String, elapsedSeconds: Int): android.app.Notification {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val timeText = "%02d:%02d".format(minutes, seconds)

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, BrewMasterApp.TIMER_CHANNEL_ID)
            .setContentTitle("BrewMaster - Brewing")
            .setContentText("Step: $stepName | $timeText")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        scope.cancel()
    }

    companion object {
        const val ACTION_START = "com.brewmaster.action.START_TIMER"
        const val ACTION_UPDATE_STEP = "com.brewmaster.action.UPDATE_STEP"
        const val ACTION_STOP = "com.brewmaster.action.STOP_TIMER"
        const val EXTRA_STEP_NAME = "extra_step_name"
        private const val NOTIFICATION_ID = 1001
    }
}
