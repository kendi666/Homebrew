package com.brewmaster.presentation.screen.convert

import androidx.lifecycle.ViewModel
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

data class GrindConvertUiState(
    val profiles: List<GrindConversionProfile> = emptyList(),
    val sourceId: String = "fm",
    val dialText: String = "8.0",
    val result: GrindConversionResult? = null,
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

    fun onSourceSelected(profile: GrindConversionProfile) {
        val mid = profile.dialOptions().let { opts ->
            opts.getOrNull(opts.size / 2) ?: ((profile.dialMin + profile.dialMax) / 2)
        }
        _uiState.update {
            it.copy(
                sourceId = profile.id,
                dialText = profile.formatDial(mid)
            )
        }
        recalculate()
    }

    fun onDialChanged(text: String) {
        _uiState.update { it.copy(dialText = text) }
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

    private fun recalculate() {
        val state = _uiState.value
        val dial = state.dialText.replace(',', '.').toDoubleOrNull() ?: return
        val result = convertGrind(state.sourceId, dial)
        _uiState.update { it.copy(result = result) }
    }
}
