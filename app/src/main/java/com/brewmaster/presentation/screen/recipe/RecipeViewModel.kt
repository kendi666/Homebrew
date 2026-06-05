package com.brewmaster.presentation.screen.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewmaster.domain.model.PersonalRecipe
import com.brewmaster.domain.usecase.DeleteRecipeUseCase
import com.brewmaster.domain.usecase.GetProcessPresetsUseCase
import com.brewmaster.domain.usecase.GetRecipesUseCase
import com.brewmaster.domain.usecase.GetTechniquesUseCase
import com.brewmaster.domain.usecase.SaveRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeUiState(
    val recipes: List<PersonalRecipe> = emptyList(),
    val isLoading: Boolean = true,
    // The recipe the user has asked to delete, awaiting confirmation.
    // Null means no confirmation dialog is showing.
    val recipePendingDeletion: PersonalRecipe? = null,
    // Lookups so the cards can show friendly names instead of raw ids.
    val techniqueNames: Map<String, String> = emptyMap(),
    val processNames: Map<Int, String> = emptyMap()
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val getTechniquesUseCase: GetTechniquesUseCase,
    private val getProcessPresetsUseCase: GetProcessPresetsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        // Techniques are a static list, so resolve their names once.
        val techniqueNames = getTechniquesUseCase().associate { it.id to it.name }
        _uiState.update { it.copy(techniqueNames = techniqueNames) }

        viewModelScope.launch {
            getRecipesUseCase().collect { recipes ->
                _uiState.update {
                    it.copy(recipes = recipes, isLoading = false)
                }
            }
        }

        viewModelScope.launch {
            getProcessPresetsUseCase().collect { processes ->
                val processNames = processes.associate { it.id to it.processName }
                _uiState.update { it.copy(processNames = processNames) }
            }
        }
    }

    /** Ask to delete a recipe — opens the confirmation dialog instead of deleting right away. */
    fun requestDeleteRecipe(recipe: PersonalRecipe) {
        _uiState.update { it.copy(recipePendingDeletion = recipe) }
    }

    /** User dismissed the confirmation dialog — keep the recipe. */
    fun cancelDeleteRecipe() {
        _uiState.update { it.copy(recipePendingDeletion = null) }
    }

    /** User confirmed the safety prompt — perform the actual deletion. */
    fun confirmDeleteRecipe() {
        val target = _uiState.value.recipePendingDeletion ?: return
        viewModelScope.launch {
            deleteRecipeUseCase(target.id)
        }
        _uiState.update { it.copy(recipePendingDeletion = null) }
    }

    suspend fun saveRecipe(recipe: PersonalRecipe): Long {
        return saveRecipeUseCase(recipe)
    }
}
