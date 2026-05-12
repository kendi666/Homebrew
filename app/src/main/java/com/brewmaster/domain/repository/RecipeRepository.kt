package com.brewmaster.domain.repository

import com.brewmaster.domain.model.PersonalRecipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<PersonalRecipe>>
    suspend fun getRecipeById(id: Int): PersonalRecipe?
    suspend fun saveRecipe(recipe: PersonalRecipe): Long
    suspend fun updateRecipe(recipe: PersonalRecipe)
    suspend fun deleteRecipe(id: Int)
}
