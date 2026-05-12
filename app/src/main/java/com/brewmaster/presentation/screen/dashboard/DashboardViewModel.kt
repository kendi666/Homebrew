package com.brewmaster.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewmaster.domain.model.BrewCalculation
import com.brewmaster.domain.model.BrewMode
import com.brewmaster.domain.model.BrewTechnique
import com.brewmaster.domain.model.CoffeeBean
import com.brewmaster.domain.model.CoffeeProcess
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.domain.model.TargetProfile
import com.brewmaster.domain.usecase.CalculateBrewUseCase
import com.brewmaster.domain.usecase.GetBeansUseCase
import com.brewmaster.domain.usecase.GetProcessPresetsUseCase
import com.brewmaster.domain.usecase.GetTechniquesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val techniques: List<BrewTechnique> = emptyList(),
    val selectedTechnique: BrewTechnique? = null,
    val processes: List<CoffeeProcess> = emptyList(),
    val selectedProcess: CoffeeProcess? = null,
    val beans: List<CoffeeBean> = emptyList(),
    val selectedBean: CoffeeBean? = null,
    val targetProfile: TargetProfile = TargetProfile.BALANCED,
    val coffeeWeight: String = "15",
    val ratio: String = "16.67",
    val grindSize: GrindSize = GrindSize.MEDIUM_FINE,
    val brewMode: BrewMode = BrewMode.HOT,
    val iceWeight: String = "80",
    val calculation: BrewCalculation? = null,
    val showProcessPicker: Boolean = false,
    val showBeanPicker: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val calculateBrewUseCase: CalculateBrewUseCase,
    private val getTechniquesUseCase: GetTechniquesUseCase,
    private val getProcessPresetsUseCase: GetProcessPresetsUseCase,
    private val getBeansUseCase: GetBeansUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        val techniques = getTechniquesUseCase()
        val first = techniques.firstOrNull()
        _uiState.update { state ->
            state.copy(
                techniques = techniques,
                selectedTechnique = first,
                ratio = first?.defaultRatio?.toString() ?: state.ratio,
                grindSize = first?.defaultGrind ?: state.grindSize
            )
        }
        recalculate()

        viewModelScope.launch {
            getProcessPresetsUseCase().collect { processes ->
                _uiState.update { it.copy(processes = processes) }
            }
        }

        viewModelScope.launch {
            getBeansUseCase().collect { beans ->
                _uiState.update { it.copy(beans = beans) }
            }
        }
    }

    fun onTechniqueSelected(technique: BrewTechnique) {
        val targetProfile = _uiState.value.targetProfile
        _uiState.update {
            it.copy(
                selectedTechnique = technique,
                ratio = technique.defaultRatio.toString(),
                grindSize = applyGrindShift(technique.defaultGrind, targetProfile.grindShift)
            )
        }
        recalculate()
    }

    fun onCoffeeWeightChanged(weight: String) {
        _uiState.update { it.copy(coffeeWeight = weight) }
        recalculate()
    }

    fun onRatioChanged(ratio: String) {
        _uiState.update { it.copy(ratio = ratio) }
        recalculate()
    }

    fun onGrindSizeChanged(grindSize: GrindSize) {
        _uiState.update { it.copy(grindSize = grindSize) }
        recalculate()
    }

    fun onBrewModeToggled(mode: BrewMode) {
        _uiState.update { it.copy(brewMode = mode) }
        recalculate()
    }

    fun onIceWeightChanged(weight: String) {
        _uiState.update { it.copy(iceWeight = weight) }
        recalculate()
    }

    fun onProcessSelected(process: CoffeeProcess?) {
        _uiState.update { it.copy(selectedProcess = process, showProcessPicker = false) }
        recalculate()
    }

    fun onProcessPickerDismissed() {
        _uiState.update { it.copy(showProcessPicker = false) }
    }

    fun onProcessPickerOpen() {
        _uiState.update { it.copy(showProcessPicker = true) }
    }

    fun onBeanSelected(bean: CoffeeBean?) {
        if (bean != null) {
            val matchingProcess = _uiState.value.processes.find { it.id == bean.processId }
            _uiState.update {
                it.copy(
                    selectedBean = bean,
                    selectedProcess = matchingProcess ?: it.selectedProcess,
                    showBeanPicker = false
                )
            }
        } else {
            _uiState.update { it.copy(selectedBean = null, showBeanPicker = false) }
        }
        recalculate()
    }

    fun onBeanPickerDismissed() {
        _uiState.update { it.copy(showBeanPicker = false) }
    }

    fun onBeanPickerOpen() {
        _uiState.update { it.copy(showBeanPicker = true) }
    }

    fun onTargetProfileSelected(profile: TargetProfile) {
        val currentState = _uiState.value
        val baseGrind = currentState.selectedTechnique?.defaultGrind ?: currentState.grindSize
        val adjustedGrind = applyGrindShift(baseGrind, profile.grindShift)
        _uiState.update {
            it.copy(
                targetProfile = profile,
                grindSize = adjustedGrind
            )
        }
        recalculate()
    }

    private fun applyGrindShift(base: GrindSize, shift: Int): GrindSize {
        if (shift == 0) return base
        val values = GrindSize.entries
        val currentIndex = values.indexOf(base)
        val newIndex = (currentIndex + shift).coerceIn(0, values.lastIndex)
        return values[newIndex]
    }

    private fun recalculate() {
        val state = _uiState.value
        val technique = state.selectedTechnique ?: return
        val coffeeWeight = parseDecimal(state.coffeeWeight) ?: return
        val ratio = parseDecimal(state.ratio) ?: return
        if (coffeeWeight <= 0 || ratio <= 0) return

        val iceWeight = if (state.brewMode == BrewMode.ICE) {
            if (state.iceWeight.isBlank()) {
                coffeeWeight * ratio * DEFAULT_ICE_RATIO
            } else {
                parseDecimal(state.iceWeight) ?: 0.0
            }
        } else {
            0.0
        }

        val calculation = calculateBrewUseCase(
            technique = technique,
            coffeeWeight = coffeeWeight,
            ratio = ratio,
            grindSize = state.grindSize,
            brewMode = state.brewMode,
            iceWeight = iceWeight,
            process = state.selectedProcess,
            targetProfile = state.targetProfile,
            bean = state.selectedBean
        )
        _uiState.update { it.copy(calculation = calculation) }
    }

    private fun parseDecimal(value: String): Double? {
        return value.trim().replace(',', '.').toDoubleOrNull()
    }

    companion object {
        private const val DEFAULT_ICE_RATIO = 0.4
    }
}
