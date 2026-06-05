package com.brewmaster.presentation.screen.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewmaster.domain.model.BrewLog
import com.brewmaster.domain.model.BrewTechnique
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.domain.usecase.DeleteBrewLogUseCase
import com.brewmaster.domain.usecase.GetBrewLogsUseCase
import com.brewmaster.domain.usecase.GetTechniquesUseCase
import com.brewmaster.domain.usecase.SaveBrewLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrewJournalUiState(
    val logs: List<BrewLog> = emptyList(),
    val isLoading: Boolean = true,
    val techniques: List<BrewTechnique> = emptyList(),
    val techniqueNames: Map<String, String> = emptyMap(),
    val showAddDialog: Boolean = false,
    val logPendingDeletion: BrewLog? = null
) {
    val averageRating: Double?
        get() = logs.takeIf { it.isNotEmpty() }?.map { it.rating }?.average()
}

@HiltViewModel
class BrewJournalViewModel @Inject constructor(
    private val getBrewLogsUseCase: GetBrewLogsUseCase,
    private val saveBrewLogUseCase: SaveBrewLogUseCase,
    private val deleteBrewLogUseCase: DeleteBrewLogUseCase,
    private val getTechniquesUseCase: GetTechniquesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrewJournalUiState())
    val uiState: StateFlow<BrewJournalUiState> = _uiState.asStateFlow()

    init {
        val techniques = getTechniquesUseCase()
        _uiState.update {
            it.copy(
                techniques = techniques,
                techniqueNames = techniques.associate { t -> t.id to t.name }
            )
        }
        viewModelScope.launch {
            getBrewLogsUseCase().collect { logs ->
                _uiState.update { it.copy(logs = logs, isLoading = false) }
            }
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun dismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun saveLog(
        beanName: String,
        techniqueId: String,
        grindSize: GrindSize,
        ratio: Double,
        coffeeWeight: Double,
        isIce: Boolean,
        rating: Int,
        notes: String?
    ) {
        viewModelScope.launch {
            saveBrewLogUseCase(
                BrewLog(
                    beanName = beanName.ifBlank { "Untitled" },
                    techniqueId = techniqueId,
                    processId = 0,
                    grindSize = grindSize,
                    ratio = ratio,
                    coffeeWeight = coffeeWeight,
                    isIce = isIce,
                    tempUsed = null,
                    rating = rating.coerceIn(1, 5),
                    notes = notes?.ifBlank { null },
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun requestDeleteLog(log: BrewLog) {
        _uiState.update { it.copy(logPendingDeletion = log) }
    }

    fun cancelDeleteLog() {
        _uiState.update { it.copy(logPendingDeletion = null) }
    }

    fun confirmDeleteLog() {
        val target = _uiState.value.logPendingDeletion ?: return
        viewModelScope.launch {
            deleteBrewLogUseCase(target.id)
        }
        _uiState.update { it.copy(logPendingDeletion = null) }
    }
}
