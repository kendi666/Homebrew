package com.brewmaster.domain.usecase

import com.brewmaster.domain.repository.RecipeRepository
import javax.inject.Inject

class DeleteRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {

    suspend operator fun invoke(id: Int) = repository.deleteRecipe(id)
}
