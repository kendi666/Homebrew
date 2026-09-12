package com.brewmaster.presentation.screen.brew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewmaster.domain.model.BrewCalculation
import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.BrewSymptom
import com.brewmaster.domain.usecase.BrewCoachAdvice
import com.brewmaster.domain.usecase.BrewCoachUseCase
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
import javax.inject.Inject

data class BrewTimerUiState(
    val calculation: BrewCalculation? = null,
    val currentStepIndex: Int = 0,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val showStopAlert: Boolean = false,
    val showCoach: Boolean = false
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
    val isFinished: Boolean
)

@HiltViewModel
class BrewTimerViewModel @Inject constructor(
    private val brewCoachUseCase: BrewCoachUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrewTimerUiState())
    val uiState: StateFlow<BrewTimerUiState> = _uiState.asStateFlow()

    private val _stepTransitions = MutableSharedFlow<StepTransitionEvent>(extraBufferCapacity = 8)
    val stepTransitions: SharedFlow<StepTransitionEvent> = _stepTransitions.asSharedFlow()

    private var timerJob: Job? = null

    fun startBrew(calculation: BrewCalculation) {
        timerJob?.cancel()
        _uiState.update {
            BrewTimerUiState(
                calculation = calculation,
                isRunning = true
            )
        }
        startTimerLoop()
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false, isPaused = true) }
    }

    fun resumeTimer() {
        _uiState.update { it.copy(isRunning = true, isPaused = false) }
        startTimerLoop()
    }

    fun resetTimer() {
        timerJob?.cancel()
        val calc = _uiState.value.calculation
        _uiState.update {
            BrewTimerUiState(
                calculation = calc,
                isRunning = false,
                isPaused = false
            )
        }
    }

    /** Jump to the start of the next step (or finish if already on the last). */
    fun skipToNextStep() {
        val state = _uiState.value
        val calc = state.calculation ?: return
        if (calc.steps.isEmpty()) return

        if (state.currentStepIndex >= calc.steps.lastIndex) {
            timerJob?.cancel()
            _uiState.update {
                it.copy(
                    elapsedSeconds = calc.totalBrewTimeSec,
                    currentStepIndex = calc.steps.lastIndex,
                    isRunning = false,
                    isPaused = false,
                    isFinished = true,
                    showStopAlert = true
                )
            }
            _stepTransitions.tryEmit(
                StepTransitionEvent(newStepIndex = calc.steps.lastIndex, isFinished = true)
            )
            return
        }

        val nextIndex = state.currentStepIndex + 1
        val nextStep = calc.steps[nextIndex]
        _uiState.update {
            it.copy(
                currentStepIndex = nextIndex,
                elapsedSeconds = nextStep.startTimeSec,
                isFinished = false,
                showStopAlert = false,
                showCoach = false
            )
        }
        _stepTransitions.tryEmit(StepTransitionEvent(newStepIndex = nextIndex, isFinished = false))
    }

    /** Jump back: restart current step, or previous if already at its start. */
    fun goToPreviousStep() {
        val state = _uiState.value
        val calc = state.calculation ?: return
        if (calc.steps.isEmpty()) return

        val safeIndex = state.currentStepIndex.coerceIn(0, calc.steps.lastIndex)
        val currentStart = calc.steps[safeIndex].startTimeSec
        val targetIndex = if (!state.isFinished && state.elapsedSeconds > currentStart + 1) {
            safeIndex
        } else {
            (safeIndex - 1).coerceAtLeast(0)
        }
        val targetStep = calc.steps[targetIndex]
        val wasFinished = state.isFinished

        _uiState.update {
            it.copy(
                currentStepIndex = targetIndex,
                elapsedSeconds = targetStep.startTimeSec,
                isFinished = false,
                showStopAlert = false,
                showCoach = false,
                isPaused = if (wasFinished) true else it.isPaused,
                isRunning = if (wasFinished) false else it.isRunning
            )
        }
        _stepTransitions.tryEmit(StepTransitionEvent(newStepIndex = targetIndex, isFinished = false))
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

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                onTimerTick()
                if (_uiState.value.isFinished || !_uiState.value.isRunning) break
            }
        }
    }

    private fun onTimerTick() {
        val state = _uiState.value
        val calc = state.calculation ?: return
        val newElapsed = state.elapsedSeconds + 1

        if (newElapsed >= calc.totalBrewTimeSec) {
            val lastIndex = calc.steps.lastIndex.coerceAtLeast(0)
            _uiState.update {
                it.copy(
                    elapsedSeconds = calc.totalBrewTimeSec,
                    currentStepIndex = lastIndex,
                    isRunning = false,
                    isFinished = true,
                    showStopAlert = true
                )
            }
            _stepTransitions.tryEmit(
                StepTransitionEvent(newStepIndex = lastIndex, isFinished = true)
            )
            return
        }

        val currentStep = calc.steps.getOrNull(state.currentStepIndex)
        val newIndex = if (currentStep != null && newElapsed >= currentStep.endTimeSec) {
            (state.currentStepIndex + 1).coerceAtMost(calc.steps.lastIndex)
        } else {
            state.currentStepIndex
        }

        _uiState.update {
            it.copy(elapsedSeconds = newElapsed, currentStepIndex = newIndex)
        }
        if (newIndex != state.currentStepIndex) {
            _stepTransitions.tryEmit(
                StepTransitionEvent(newStepIndex = newIndex, isFinished = false)
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
