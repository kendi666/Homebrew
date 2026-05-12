package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.PersonalRecipe
import com.brewmaster.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {

    operator fun invoke(): Flow<List<PersonalRecipe>> = repository.getAllRecipes()
}
