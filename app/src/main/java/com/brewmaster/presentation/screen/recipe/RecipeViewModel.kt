package com.brewmaster.presentation.screen.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewmaster.domain.model.PersonalRecipe
import com.brewmaster.domain.usecase.DeleteRecipeUseCase
import com.brewmaster.domain.usecase.GetRecipesUseCase
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
    val isLoading: Boolean = true
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getRecipesUseCase().collect { recipes ->
                _uiState.update {
                    it.copy(recipes = recipes, isLoading = false)
                }
            }
        }
    }

    fun deleteRecipe(id: Int) {
        viewModelScope.launch {
            deleteRecipeUseCase(id)
        }
    }

    suspend fun saveRecipe(recipe: PersonalRecipe): Long {
        return saveRecipeUseCase(recipe)
    }
}
