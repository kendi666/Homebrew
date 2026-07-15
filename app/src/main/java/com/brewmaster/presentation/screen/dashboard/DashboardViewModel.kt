package com.brewmaster.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewmaster.domain.model.BrewCalculation
import com.brewmaster.domain.model.BrewMode
import com.brewmaster.domain.model.BrewSuggestion
import com.brewmaster.domain.model.BrewTechnique
import com.brewmaster.domain.model.CoffeeBean
import com.brewmaster.domain.model.CoffeeProcess
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.domain.model.Grinder
import com.brewmaster.domain.model.PersonalRecipe
import com.brewmaster.domain.model.TargetProfile
import com.brewmaster.domain.usecase.CalculateBrewUseCase
import com.brewmaster.domain.usecase.GetBeansUseCase
import com.brewmaster.domain.usecase.GetGrindersUseCase
import com.brewmaster.domain.usecase.GetProcessPresetsUseCase
import com.brewmaster.domain.usecase.GetTechniquesUseCase
import com.brewmaster.domain.usecase.SaveRecipeUseCase
import com.brewmaster.domain.usecase.SuggestBrewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

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
    val iceWeight: String = "",
    val calculation: BrewCalculation? = null,
    val grinders: List<Grinder> = emptyList(),
    val selectedGrinder: Grinder? = null,
    val grinderClicks: String = "",
    val suggestions: List<BrewSuggestion> = emptyList(),
    val showBeanPicker: Boolean = false,
    val showSaveRecipeDialog: Boolean = false,
    val customTempMin: Int = 90,
    val customTempMax: Int = 94,
    val customSteps: List<com.brewmaster.domain.model.CustomStepConfig> = listOf(
        com.brewmaster.domain.model.CustomStepConfig(com.brewmaster.domain.model.StepAction.BLOOM, 45, 0.20),
        com.brewmaster.domain.model.CustomStepConfig(com.brewmaster.domain.model.StepAction.POUR, 135, 0.80)
    )
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val calculateBrewUseCase: CalculateBrewUseCase,
    private val getTechniquesUseCase: GetTechniquesUseCase,
    private val getProcessPresetsUseCase: GetProcessPresetsUseCase,
    private val getBeansUseCase: GetBeansUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val getGrindersUseCase: GetGrindersUseCase,
    private val suggestBrewUseCase: SuggestBrewUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private var pendingRecipe: PersonalRecipe? = null
    /** When set, CalculateBrewUseCase uses these temps (from a suggestion tap). */
    private var suggestionTemps: Pair<Int, Int>? = null

    init {
        val techniques = getTechniquesUseCase()
        val first = techniques.firstOrNull()
        val grinders = getGrindersUseCase()
        val defaultGrinder = grinders.firstOrNull { it.id == "1zpresso_k_ultra" } ?: grinders.firstOrNull()
        val defaultDial = defaultGrinder?.filterDialLabels()?.let { labels ->
            labels.getOrNull(labels.size / 2).orEmpty()
        }.orEmpty()
        val dialClicks = defaultGrinder?.let { g ->
            defaultDial.toDoubleOrNull()?.let { g.dialToClicks(it) }
        }
        _uiState.update { state ->
            state.copy(
                techniques = techniques,
                selectedTechnique = first,
                ratio = first?.defaultRatio?.toString() ?: state.ratio,
                grindSize = dialClicks?.let { defaultGrinder.nearestGrindSize(it) }
                    ?: first?.defaultGrind
                    ?: state.grindSize,
                grinders = grinders,
                selectedGrinder = defaultGrinder,
                grinderClicks = defaultDial
            )
        }
        recalculate()

        viewModelScope.launch {
            getProcessPresetsUseCase().collect { processes ->
                _uiState.update { it.copy(processes = processes) }
                pendingRecipe?.let(::applyRecipe)
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
        suggestionTemps = null
        _uiState.update {
            it.copy(
                selectedTechnique = technique,
                ratio = technique.defaultRatio.toString(),
                grindSize = applyGrindShift(technique.defaultGrind, targetProfile.grindShift)
            )
        }
        recalculate()
    }

    fun onSuggestionSelected(suggestion: BrewSuggestion) {
        suggestionTemps = suggestion.tempMin to suggestion.tempMax
        _uiState.update {
            it.copy(
                selectedTechnique = suggestion.technique,
                ratio = suggestion.technique.defaultRatio.toString(),
                grindSize = suggestion.inferredGrind,
                customTempMin = suggestion.tempMin,
                customTempMax = suggestion.tempMax
            )
        }
        recalculate()
    }

    fun onCoffeeWeightChanged(weight: String) {
        _uiState.update { it.copy(coffeeWeight = weight) }
        // Keep ice at 40% of total when user hasn't locked a custom ice weight
        syncDefaultIceIfNeeded()
        recalculate()
    }

    fun onRatioChanged(ratio: String) {
        _uiState.update { it.copy(ratio = ratio) }
        syncDefaultIceIfNeeded()
        recalculate()
    }

    fun onGrindSizeChanged(grindSize: GrindSize) {
        _uiState.update { it.copy(grindSize = grindSize, grinderClicks = "") }
        recalculate()
    }

    fun onGrinderSelected(grinder: Grinder) {
        val dials = grinder.filterDialLabels()
        val current = _uiState.value.grinderClicks
        val dial = when {
            current.isNotBlank() && current in dials -> current
            else -> dials.getOrNull(dials.size / 2).orEmpty()
        }
        _uiState.update { it.copy(selectedGrinder = grinder, grinderClicks = dial) }
        if (dial.isNotBlank()) {
            onGrinderClicksChanged(dial)
        } else {
            recalculate()
        }
    }

    fun onGrinderClicksChanged(clicks: String) {
        val parsed = parseClicks(clicks)
        _uiState.update { state ->
            val grinder = state.selectedGrinder
            val inferred = if (parsed != null && grinder != null) {
                grinder.nearestGrindSize(parsed)
            } else {
                state.grindSize
            }
            state.copy(grinderClicks = clicks, grindSize = inferred)
        }
        recalculate()
    }

    fun onBrewModeToggled(mode: BrewMode) {
        _uiState.update { state ->
            val ice = if (mode == BrewMode.ICE && state.iceWeight.isBlank()) {
                defaultIceString(state)
            } else {
                state.iceWeight
            }
            state.copy(brewMode = mode, iceWeight = ice)
        }
        recalculate()
    }

    fun onIceWeightChanged(weight: String) {
        _uiState.update { it.copy(iceWeight = weight) }
        recalculate()
    }

    fun onBeanSelected(bean: CoffeeBean?) {
        if (bean != null) {
            val matchingProcess = _uiState.value.processes.find { it.id == bean.processId }
            _uiState.update { state ->
                val profile = state.targetProfile
                val grind = matchingProcess?.let {
                    applyGrindShift(it.grindRecommendation, profile.grindShift)
                } ?: state.grindSize
                val ratio = matchingProcess?.ratioMin?.let { formatDecimal(it) } ?: state.ratio
                state.copy(
                    selectedBean = bean,
                    selectedProcess = matchingProcess ?: state.selectedProcess,
                    grindSize = grind,
                    ratio = ratio,
                    grinderClicks = "",
                    showBeanPicker = false
                )
            }
        } else {
            _uiState.update { it.copy(selectedBean = null, showBeanPicker = false) }
        }
        syncDefaultIceIfNeeded()
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
        val baseGrind = currentState.selectedProcess?.grindRecommendation
            ?: currentState.selectedTechnique?.defaultGrind
            ?: currentState.grindSize
        val adjustedGrind = applyGrindShift(baseGrind, profile.grindShift)
        _uiState.update {
            it.copy(
                targetProfile = profile,
                grindSize = adjustedGrind,
                grinderClicks = ""
            )
        }
        recalculate()
    }

    fun onSaveRecipeOpen() {
        _uiState.update { it.copy(showSaveRecipeDialog = true) }
    }

    fun onSaveRecipeDismissed() {
        _uiState.update { it.copy(showSaveRecipeDialog = false) }
    }

    fun onCustomTempMinChanged(temp: Int) {
        _uiState.update { it.copy(customTempMin = temp) }
        recalculate()
    }

    fun onCustomTempMaxChanged(temp: Int) {
        _uiState.update { it.copy(customTempMax = temp) }
        recalculate()
    }

    fun onAddCustomStep(step: com.brewmaster.domain.model.CustomStepConfig) {
        _uiState.update { it.copy(customSteps = it.customSteps + step) }
        recalculate()
    }

    fun onRemoveCustomStep(index: Int) {
        _uiState.update {
            val newSteps = it.customSteps.toMutableList().apply { removeAt(index) }
            it.copy(customSteps = newSteps)
        }
        recalculate()
    }

    fun onUpdateCustomStep(index: Int, step: com.brewmaster.domain.model.CustomStepConfig) {
        _uiState.update {
            val newSteps = it.customSteps.toMutableList().apply { this[index] = step }
            it.copy(customSteps = newSteps)
        }
        recalculate()
    }

    fun saveCurrentRecipe(beanName: String, notes: String?, tempMin: Int? = null, tempMax: Int? = null) {
        val state = _uiState.value
        val technique = state.selectedTechnique ?: return
        val coffeeWeight = parseDecimal(state.coffeeWeight) ?: return
        val ratio = parseDecimal(state.ratio) ?: return
        val iceWeight = if (state.brewMode == BrewMode.ICE) {
            parseDecimal(state.iceWeight)
        } else {
            null
        }

        viewModelScope.launch {
            saveRecipeUseCase(
                PersonalRecipe(
                    beanName = beanName,
                    techniqueId = technique.id,
                    processId = state.selectedProcess?.id ?: 0,
                    grindSize = state.grindSize,
                    ratio = ratio,
                    coffeeWeight = coffeeWeight,
                    isIce = state.brewMode == BrewMode.ICE,
                    iceWeight = iceWeight,
                    notes = notes,
                    createdAt = System.currentTimeMillis(),
                    grinderSetting = state.grinderClicks.ifBlank { null },
                    tempMin = tempMin ?: state.calculation?.tempMin,
                    tempMax = tempMax ?: state.calculation?.tempMax
                )
            )
            _uiState.update { it.copy(showSaveRecipeDialog = false) }
        }
    }

    fun loadRecipe(recipe: PersonalRecipe) {
        pendingRecipe = recipe
        applyRecipe(recipe)
    }

    private fun applyRecipe(recipe: PersonalRecipe) {
        val state = _uiState.value
        val technique = state.techniques.find { it.id == recipe.techniqueId }
        val process = state.processes.find { it.id == recipe.processId }
        val matchingBean = state.beans.find { it.processId == recipe.processId }

        _uiState.update {
            it.copy(
                selectedTechnique = technique ?: it.selectedTechnique,
                selectedProcess = process,
                selectedBean = matchingBean,
                coffeeWeight = formatDecimal(recipe.coffeeWeight),
                ratio = formatDecimal(recipe.ratio),
                grindSize = recipe.grindSize,
                brewMode = if (recipe.isIce) BrewMode.ICE else BrewMode.HOT,
                iceWeight = formatDecimal(recipe.iceWeight ?: 0.0),
                grinderClicks = recipe.grinderSetting.orEmpty(),
                customTempMin = recipe.tempMin ?: it.customTempMin,
                customTempMax = recipe.tempMax ?: it.customTempMax
            )
        }
        suggestionTemps = if (recipe.tempMin != null && recipe.tempMax != null) {
            recipe.tempMin to recipe.tempMax
        } else {
            null
        }
        pendingRecipe = null
        recalculate()
    }

    private fun applyGrindShift(base: GrindSize, shift: Int): GrindSize {
        if (shift == 0) return base
        val values = GrindSize.entries
        val currentIndex = values.indexOf(base)
        val newIndex = (currentIndex + shift).coerceIn(0, values.lastIndex)
        return values[newIndex]
    }

    private fun syncDefaultIceIfNeeded() {
        val state = _uiState.value
        if (state.brewMode != BrewMode.ICE) return
        // Only auto-fill when blank (user hasn't typed a custom ice weight yet)
        if (state.iceWeight.isNotBlank()) return
        _uiState.update { it.copy(iceWeight = defaultIceString(it)) }
    }

    private fun defaultIceString(state: DashboardUiState): String {
        val coffee = parseDecimal(state.coffeeWeight) ?: return ""
        val ratio = parseDecimal(state.ratio) ?: return ""
        val ice = (coffee * ratio * DEFAULT_ICE_RATIO).roundToInt()
        return ice.toString()
    }

    private fun recalculate() {
        val state = _uiState.value
        val technique = state.selectedTechnique ?: return
        val coffeeWeight = parseDecimal(state.coffeeWeight) ?: return
        val ratio = parseDecimal(state.ratio) ?: return
        if (coffeeWeight <= 0 || ratio <= 0) return

        val iceWeight = if (state.brewMode == BrewMode.ICE) {
            parseDecimal(state.iceWeight) ?: (coffeeWeight * ratio * DEFAULT_ICE_RATIO)
        } else {
            0.0
        }

        val temps = suggestionTemps
        val calculation = calculateBrewUseCase(
            technique = technique,
            coffeeWeight = coffeeWeight,
            ratio = ratio,
            grindSize = state.grindSize,
            brewMode = state.brewMode,
            iceWeight = iceWeight,
            process = state.selectedProcess,
            targetProfile = state.targetProfile,
            bean = state.selectedBean,
            customSteps = state.customSteps,
            customTempMin = if (technique.id == "custom") state.customTempMin else temps?.first,
            customTempMax = if (technique.id == "custom") state.customTempMax else temps?.second
        )

        val clicks = parseClicks(state.grinderClicks)
        val suggestions = suggestBrewUseCase(
            grinder = state.selectedGrinder,
            clicks = clicks,
            grindSize = state.grindSize,
            process = state.selectedProcess,
            targetProfile = state.targetProfile,
            brewMode = state.brewMode
        )

        _uiState.update { it.copy(calculation = calculation, suggestions = suggestions) }
    }

    private fun parseDecimal(value: String): Double? {
        return value.trim().replace(',', '.').toDoubleOrNull()
    }

    private fun parseClicks(value: String): Int? {
        val trimmed = value.trim().replace(',', '.')
        if (trimmed.isEmpty()) return null
        val asDouble = trimmed.toDoubleOrNull() ?: return null
        val grinder = _uiState.value.selectedGrinder ?: return asDouble.roundToInt()
        return grinder.dialToClicks(asDouble)
    }

    private fun formatDecimal(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
        }
    }

    companion object {
        private const val DEFAULT_ICE_RATIO = 0.4
    }
}
