package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.PersonalRecipe
import com.brewmaster.domain.repository.RecipeRepository
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {

    /** A recipe with an id replaces that row. A new recipe is inserted. */
    suspend operator fun invoke(recipe: PersonalRecipe): Long {
        if (recipe.id == 0) return repository.saveRecipe(recipe)
        repository.updateRecipe(recipe)
        return recipe.id.toLong()
    }
}
