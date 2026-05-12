package com.brewmaster.presentation.screen.cheatsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewmaster.domain.model.CoffeeBean
import com.brewmaster.domain.model.CoffeeProcess
import com.brewmaster.domain.repository.CoffeeBeanRepository
import com.brewmaster.domain.usecase.GetBeansUseCase
import com.brewmaster.domain.usecase.GetProcessPresetsUseCase
import com.brewmaster.domain.usecase.SaveBeanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheatSheetUiState(
    val processes: List<CoffeeProcess> = emptyList(),
    val beans: List<CoffeeBean> = emptyList(),
    val showAddBeanDialog: Boolean = false
)

@HiltViewModel
class CheatSheetViewModel @Inject constructor(
    private val getProcessPresetsUseCase: GetProcessPresetsUseCase,
    private val getBeansUseCase: GetBeansUseCase,
    private val saveBeanUseCase: SaveBeanUseCase,
    private val coffeeBeanRepository: CoffeeBeanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheatSheetUiState())
    val uiState: StateFlow<CheatSheetUiState> = _uiState.asStateFlow()

    init {
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

    fun onAddBeanOpen() {
        _uiState.update { it.copy(showAddBeanDialog = true) }
    }

    fun onAddBeanDismissed() {
        _uiState.update { it.copy(showAddBeanDialog = false) }
    }

    fun onSaveBean(bean: CoffeeBean) {
        viewModelScope.launch {
            saveBeanUseCase(bean)
            _uiState.update { it.copy(showAddBeanDialog = false) }
        }
    }

    fun onDeleteBean(id: Int) {
        viewModelScope.launch {
            coffeeBeanRepository.deleteBean(id)
        }
    }
}
