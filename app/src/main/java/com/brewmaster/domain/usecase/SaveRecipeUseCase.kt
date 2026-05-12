package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.PersonalRecipe
import com.brewmaster.domain.repository.RecipeRepository
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {

    suspend operator fun invoke(recipe: PersonalRecipe): Long = repository.saveRecipe(recipe)
}
