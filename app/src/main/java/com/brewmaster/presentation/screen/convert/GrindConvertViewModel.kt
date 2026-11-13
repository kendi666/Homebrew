package com.brewmaster.presentation.screen.convert

import androidx.lifecycle.ViewModel
import com.brewmaster.domain.model.ConvertedGrindDial
import com.brewmaster.domain.model.GrindConversionProfile
import com.brewmaster.domain.model.GrindConversionResult
import com.brewmaster.domain.usecase.ConvertGrindUseCase
import com.brewmaster.domain.usecase.GetGrindConversionProfilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class ConvertMode { GRINDER, MICRON }

data class GrindConvertUiState(
    val profiles: List<GrindConversionProfile> = emptyList(),
    val mode: ConvertMode = ConvertMode.GRINDER,
    val sourceId: String = "fm",
    val dialText: String = "8.0",
    val micronText: String = "850",
    val targetId: String = "1zpresso_k_ultra",
    val result: GrindConversionResult? = null,
    val micronDials: List<ConvertedGrindDial> = emptyList(),
    val brandFilter: String? = null
) {
    val source: GrindConversionProfile?
        get() = profiles.find { it.id == sourceId }

    val brands: List<String>
        get() = profiles.map { it.brand }.distinct().sorted()
}

@HiltViewModel
class GrindConvertViewModel @Inject constructor(
    private val getProfiles: GetGrindConversionProfilesUseCase,
    private val convertGrind: ConvertGrindUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrindConvertUiState())
    val uiState: StateFlow<GrindConvertUiState> = _uiState.asStateFlow()

    init {
        val profiles = getProfiles()
        val defaultSource = profiles.find { it.id == "fm" } ?: profiles.first()
        val defaultDial = defaultSource.formatDial(
            8.0.coerceIn(defaultSource.dialMin, defaultSource.dialMax)
        )
        _uiState.update {
            it.copy(
                profiles = profiles,
                sourceId = defaultSource.id,
                dialText = defaultDial
            )
        }
        recalculate()
    }

    fun onModeSelected(mode: ConvertMode) {
        _uiState.update { it.copy(mode = mode) }
        recalculate()
    }

    fun onTargetSelected(profile: GrindConversionProfile) {
        _uiState.update { it.copy(targetId = profile.id) }
    }

    fun onSourceSelected(profile: GrindConversionProfile) {
        // Keep the same grind: start the new grinder at the dial that matches the current result.
        val carried = _uiState.value.result?.estimatedMicron?.let { profile.micronToDial(it) }
        val start = carried ?: profile.dialOptions().let { opts ->
            opts.getOrNull(opts.size / 2) ?: ((profile.dialMin + profile.dialMax) / 2)
        }
        _uiState.update {
            it.copy(
                sourceId = profile.id,
                dialText = profile.formatDial(start)
            )
        }
        recalculate()
    }

    fun onDialChanged(text: String) {
        _uiState.update { it.copy(dialText = text) }
        recalculate()
    }

    fun onMicronChanged(text: String) {
        _uiState.update { it.copy(micronText = text) }
        recalculate()
    }

    fun onBrandFilterSelected(brand: String?) {
        _uiState.update { it.copy(brandFilter = brand) }
    }

    fun nudgeDial(deltaSteps: Int) {
        val state = _uiState.value
        val source = state.source ?: return
        val current = state.dialText.replace(',', '.').toDoubleOrNull()
            ?: ((source.dialMin + source.dialMax) / 2)
        val next = GrindConversionProfile.roundToStep(
            (current + deltaSteps * source.dialStep).coerceIn(source.dialMin, source.dialMax),
            source.dialStep
        )
        _uiState.update { it.copy(dialText = source.formatDial(next)) }
        recalculate()
    }

    fun nudgeMicron(deltaSteps: Int) {
        val current = _uiState.value.micronText.replace(',', '.').toDoubleOrNull() ?: 850.0
        val next = (current + deltaSteps * 10.0).coerceIn(0.0, 1600.0)
        val text = if (next == next.toLong().toDouble()) next.toLong().toString() else next.toString()
        _uiState.update { it.copy(micronText = text) }
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        if (state.mode == ConvertMode.MICRON) {
            val micron = state.micronText.replace(',', '.').toDoubleOrNull()
            if (micron == null) {
                _uiState.update { it.copy(micronDials = emptyList()) }
                return
            }
            _uiState.update { it.copy(micronDials = convertGrind.fromMicron(micron)) }
        } else {
            val dial = state.dialText.replace(',', '.').toDoubleOrNull()
            if (dial == null) {
                _uiState.update { it.copy(result = null) }
                return
            }
            _uiState.update { it.copy(result = convertGrind(state.sourceId, dial)) }
        }
    }
}
