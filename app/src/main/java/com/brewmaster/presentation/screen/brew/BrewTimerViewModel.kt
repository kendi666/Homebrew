package com.brewmaster.presentation.screen.brew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewmaster.domain.model.BrewCalculation
import com.brewmaster.domain.model.BrewStep
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
    val showStopAlert: Boolean = false
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
class BrewTimerViewModel @Inject constructor() : ViewModel() {

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

    fun dismissStopAlert() {
        _uiState.update { it.copy(showStopAlert = false) }
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
        _uiState.update { state ->
            val calc = state.calculation ?: return@update state
            val newElapsed = state.elapsedSeconds + 1

            if (newElapsed >= calc.totalBrewTimeSec) {
                viewModelScope.launch {
                    _stepTransitions.emit(
                        StepTransitionEvent(
                            newStepIndex = calc.steps.lastIndex,
                            isFinished = true
                        )
                    )
                }
                return@update state.copy(
                    elapsedSeconds = calc.totalBrewTimeSec,
                    isRunning = false,
                    isFinished = true,
                    showStopAlert = true
                )
            }

            val currentStep = calc.steps.getOrNull(state.currentStepIndex)
            var newIndex = state.currentStepIndex

            if (currentStep != null && newElapsed >= currentStep.endTimeSec) {
                newIndex = (state.currentStepIndex + 1).coerceAtMost(calc.steps.lastIndex)
                viewModelScope.launch {
                    _stepTransitions.emit(
                        StepTransitionEvent(newStepIndex = newIndex, isFinished = false)
                    )
                }
            }

            state.copy(
                elapsedSeconds = newElapsed,
                currentStepIndex = newIndex
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
