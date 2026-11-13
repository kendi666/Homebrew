package com.brewmaster.presentation.screen.brew

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewmaster.domain.model.BrewCalculation
import com.brewmaster.domain.model.BrewLog
import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.BrewSymptom
import com.brewmaster.domain.usecase.BrewCoachAdvice
import com.brewmaster.domain.usecase.BrewCoachUseCase
import com.brewmaster.domain.usecase.SaveBrewLogUseCase
import com.brewmaster.service.BrewTimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class BrewTimerUiState(
    val calculation: BrewCalculation? = null,
    val currentStepIndex: Int = 0,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val showStopAlert: Boolean = false,
    val showCoach: Boolean = false,
    val usesService: Boolean = false,
    /** The clock paused itself because drawdown was never tapped. */
    val autoPaused: Boolean = false
) {
    val currentStep: BrewStep?
        get() = calculation?.steps?.getOrNull(currentStepIndex)

    val progress: Float
        get() {
            val total = calculation?.totalBrewTimeSec ?: return 0f
            if (total <= 0) return 0f
            return (elapsedSeconds.toFloat() / total).coerceIn(0f, 1f)
        }

    val stepProgress: Float
        get() {
            val step = currentStep ?: return 0f
            val duration = step.durationSec
            if (duration <= 0) return 0f
            val elapsed = elapsedSeconds - step.startTimeSec
            return (elapsed.toFloat() / duration).coerceIn(0f, 1f)
        }
}

data class StepTransitionEvent(
    val newStepIndex: Int,
    val isFinished: Boolean,
    val isTargetReached: Boolean = false
)

/** Where a clock should begin: at [elapsedSec], running or paused. */
data class ClockStart(val elapsedSec: Int, val paused: Boolean)

@HiltViewModel
class BrewTimerViewModel @Inject constructor(
    private val brewCoachUseCase: BrewCoachUseCase,
    private val saveBrewLogUseCase: SaveBrewLogUseCase,
    private val savedState: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrewTimerUiState())
    val uiState: StateFlow<BrewTimerUiState> = _uiState.asStateFlow()

    private val _stepTransitions = MutableSharedFlow<StepTransitionEvent>(extraBufferCapacity = 8)
    val stepTransitions: SharedFlow<StepTransitionEvent> = _stepTransitions.asSharedFlow()

    private var timerJob: Job? = null
    private var localAccumulatedMs = 0L
    private var localSegmentStart = 0L
    private var localRunning = false
    private var localTargetAlerted = false

    /** Set when the brew came back from saved state after process death. */
    private var restoredStart: ClockStart? = null

    /** Kept while the notification permission dialog is open. */
    private var pendingStart: ClockStart? = null

    init {
        restoreFromSavedState()
    }

    /**
     * Takes the brew once per ViewModel. Returns where the clock should start,
     * or null when nothing needs starting (a rotation, or no brew at all).
     */
    fun takeBrew(sessionCalculation: BrewCalculation?): ClockStart? {
        if (_uiState.value.calculation != null) {
            return restoredStart.also { restoredStart = null }
        }
        val calculation = sessionCalculation ?: return null
        _uiState.value = BrewTimerUiState(calculation = calculation, isRunning = true)
        savedState[KEY_CALCULATION] = json.encodeToString(BrewCalculation.serializer(), calculation)
        savedState[KEY_FINISHED] = false
        persistClock(0, running = true)
        return ClockStart(elapsedSec = 0, paused = false)
    }

    fun holdPendingStart(start: ClockStart) {
        pendingStart = start
    }

    fun takePendingStart(): ClockStart =
        pendingStart.also { pendingStart = null } ?: ClockStart(_uiState.value.elapsedSeconds, paused = false)

    fun markUsingService() {
        localRunning = false
        timerJob?.cancel()
        _uiState.update { it.copy(usesService = true) }
    }

    fun onClock(elapsedSeconds: Int, running: Boolean, active: Boolean, autoPaused: Boolean = false) {
        if (!active) return
        val state = _uiState.value
        if (state.isFinished) return
        val calc = state.calculation ?: return
        if (calc.steps.isEmpty()) return
        val index = calc.steps.indexOfLast { elapsedSeconds >= it.startTimeSec }
            .coerceIn(0, calc.steps.lastIndex)
        val stepChanged = index != state.currentStepIndex
        _uiState.update {
            it.copy(
                elapsedSeconds = elapsedSeconds,
                currentStepIndex = index,
                isRunning = running,
                isPaused = !running,
                autoPaused = autoPaused && !running
            )
        }
        persistClock(elapsedSeconds, running)

        // With the service, it buzzes on its own so the phone works with the screen away.
        if (state.usesService) return
        if (stepChanged && running) {
            _stepTransitions.tryEmit(StepTransitionEvent(newStepIndex = index, isFinished = false))
        }
        val target = calc.totalBrewTimeSec
        if (elapsedSeconds < target) localTargetAlerted = false
        if (running && !localTargetAlerted && target > 0 && elapsedSeconds >= target) {
            localTargetAlerted = true
            _stepTransitions.tryEmit(
                StepTransitionEvent(newStepIndex = index, isFinished = false, isTargetReached = true)
            )
        }
    }

    fun peekNextStartSec(): Int? {
        val state = _uiState.value
        val calc = state.calculation ?: return null
        if (calc.steps.isEmpty()) return null
        if (state.currentStepIndex >= calc.steps.lastIndex) return null
        return calc.steps[state.currentStepIndex + 1].startTimeSec
    }

    fun peekPreviousStartSec(): Int? {
        val state = _uiState.value
        val calc = state.calculation ?: return null
        if (calc.steps.isEmpty()) return null
        val safeIndex = state.currentStepIndex.coerceIn(0, calc.steps.lastIndex)
        val currentStart = calc.steps[safeIndex].startTimeSec
        val targetIndex = if (!state.isFinished && state.elapsedSeconds > currentStart + 1) {
            safeIndex
        } else {
            (safeIndex - 1).coerceAtLeast(0)
        }
        return calc.steps[targetIndex].startTimeSec
    }

    /** Lets a later clock event move the screen again after drawdown. */
    fun prepareForClock() {
        if (!_uiState.value.isFinished) return
        savedState[KEY_FINISHED] = false
        _uiState.update {
            it.copy(isFinished = false, showStopAlert = false, showCoach = false)
        }
    }

    /** Records the clock as it stands. The brew is allowed to run past the target. */
    fun finishDrawdown() {
        timerJob?.cancel()
        localRunning = false
        val state = _uiState.value
        val lastIndex = state.calculation?.steps?.lastIndex?.coerceAtLeast(0) ?: 0
        _uiState.update {
            it.copy(
                currentStepIndex = lastIndex,
                isRunning = false,
                isPaused = false,
                isFinished = true,
                autoPaused = false,
                showStopAlert = true
            )
        }
        persistClock(state.elapsedSeconds, running = false)
        savedState[KEY_FINISHED] = true
        _stepTransitions.tryEmit(StepTransitionEvent(newStepIndex = lastIndex, isFinished = true))
    }

    fun beginLocalClock(start: ClockStart) {
        _uiState.update { it.copy(usesService = false) }
        localAccumulatedMs = start.elapsedSec * 1000L
        localTargetAlerted = (_uiState.value.calculation?.totalBrewTimeSec ?: 0).let { it in 1..start.elapsedSec }
        if (start.paused) {
            localRunning = false
            onClock(start.elapsedSec, running = false, active = true)
        } else {
            localRunning = true
            localSegmentStart = SystemClock.elapsedRealtime()
            startLocalLoop()
        }
    }

    fun pauseLocal(auto: Boolean = false) {
        if (!localRunning) return
        localAccumulatedMs = localElapsedMs()
        localRunning = false
        timerJob?.cancel()
        onClock(localElapsedSec(), running = false, active = true, autoPaused = auto)
    }

    fun resumeLocal() {
        if (localRunning || _uiState.value.isFinished) return
        localRunning = true
        localSegmentStart = SystemClock.elapsedRealtime()
        startLocalLoop()
    }

    fun resetLocal() {
        timerJob?.cancel()
        localRunning = false
        localAccumulatedMs = 0L
        localTargetAlerted = false
        _uiState.update {
            it.copy(
                elapsedSeconds = 0,
                currentStepIndex = 0,
                isRunning = false,
                isPaused = true,
                isFinished = false,
                autoPaused = false,
                showStopAlert = false,
                showCoach = false
            )
        }
        persistClock(0, running = false)
    }

    fun seekLocal(seconds: Int) {
        localAccumulatedMs = seconds.coerceAtLeast(0) * 1000L
        if (localRunning) localSegmentStart = SystemClock.elapsedRealtime()
        onClock(localElapsedSec(), localRunning, active = true)
    }

    fun dismissStopAlert() {
        _uiState.update { it.copy(showStopAlert = false, showCoach = true) }
    }

    fun dismissCoach() {
        _uiState.update { it.copy(showCoach = false) }
    }

    fun coachAdvice(symptom: BrewSymptom?): BrewCoachAdvice {
        val state = _uiState.value
        return brewCoachUseCase(
            elapsedSec = state.elapsedSeconds,
            targetSec = state.calculation?.totalBrewTimeSec ?: 0,
            symptom = symptom
        )
    }

    /** Writes this brew to the journal with the time Drawdown done recorded. */
    suspend fun saveToJournal(rating: Int, symptom: BrewSymptom?) {
        val state = _uiState.value
        val calc = state.calculation ?: return
        val tasteNote = symptom?.let { "${it.label}. ${it.diagnosis}" }
        saveBrewLogUseCase(
            BrewLog(
                beanName = calc.beanName?.ifBlank { null } ?: "Untitled",
                techniqueId = calc.technique.id,
                processId = calc.processId,
                grindSize = calc.grindSize,
                ratio = if (calc.coffeeWeight > 0) calc.totalVolume / calc.coffeeWeight else 0.0,
                coffeeWeight = calc.coffeeWeight,
                isIce = calc.iceWeight > 0,
                tempUsed = (calc.tempMin + calc.tempMax) / 2,
                rating = rating.coerceIn(1, 5),
                notes = tasteNote,
                createdAt = System.currentTimeMillis(),
                grinderName = calc.grinderName,
                grinderSetting = calc.grinderSetting,
                brewTimeSec = state.elapsedSeconds
            )
        )
    }

    private fun localElapsedMs(): Long {
        val live = if (localRunning) SystemClock.elapsedRealtime() - localSegmentStart else 0L
        return localAccumulatedMs + live.coerceAtLeast(0L)
    }

    private fun localElapsedSec(): Int = (localElapsedMs() / 1000L).toInt()

    private fun startLocalLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (localRunning && !_uiState.value.isFinished) {
                val seconds = localElapsedSec()
                val target = _uiState.value.calculation?.totalBrewTimeSec ?: 0
                if (target > 0 && seconds >= target + BrewTimerService.OVERRUN_LIMIT_SEC) {
                    pauseLocal(auto = true)
                    break
                }
                onClock(seconds, running = true, active = true)
                delay(200L)
            }
        }
    }

    /** Stores the clock against elapsedRealtime, which keeps counting while the process is dead. */
    private fun persistClock(elapsedSeconds: Int, running: Boolean) {
        savedState[KEY_ACCUMULATED_MS] = elapsedSeconds * 1000L
        savedState[KEY_ANCHOR_REALTIME] = SystemClock.elapsedRealtime()
        savedState[KEY_RUNNING] = running
    }

    private fun restoreFromSavedState() {
        val encoded = savedState.get<String>(KEY_CALCULATION) ?: return
        val calculation = runCatching {
            json.decodeFromString(BrewCalculation.serializer(), encoded)
        }.getOrNull() ?: return
        val accumulated = savedState.get<Long>(KEY_ACCUMULATED_MS) ?: 0L
        val anchor = savedState.get<Long>(KEY_ANCHOR_REALTIME) ?: 0L
        val wasRunning = savedState.get<Boolean>(KEY_RUNNING) ?: false
        val finished = savedState.get<Boolean>(KEY_FINISHED) ?: false
        val now = SystemClock.elapsedRealtime()
        // A reboot resets elapsedRealtime. Then the gap is unknown, so the clock stays paused.
        val gap = if (wasRunning && now >= anchor) now - anchor else 0L
        val running = wasRunning && now >= anchor && !finished
        val elapsedSec = ((accumulated + gap) / 1000L).toInt()
        val index = calculation.steps.indexOfLast { elapsedSec >= it.startTimeSec }.coerceAtLeast(0)

        _uiState.value = BrewTimerUiState(
            calculation = calculation,
            currentStepIndex = index,
            elapsedSeconds = elapsedSec,
            isRunning = running,
            isPaused = !running && !finished,
            isFinished = finished,
            showCoach = finished
        )
        restoredStart = if (finished) null else ClockStart(elapsedSec, paused = !running)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
        const val KEY_CALCULATION = "brew_calculation"
        const val KEY_ACCUMULATED_MS = "brew_accumulated_ms"
        const val KEY_ANCHOR_REALTIME = "brew_anchor_realtime"
        const val KEY_RUNNING = "brew_running"
        const val KEY_FINISHED = "brew_finished"
    }
}
