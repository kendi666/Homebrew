package com.brewmaster.data.repository

import com.brewmaster.data.local.dao.PersonalRecipeDao
import com.brewmaster.data.local.entity.PersonalRecipeEntity
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.domain.model.PersonalRecipe
import com.brewmaster.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val personalRecipeDao: PersonalRecipeDao
) : RecipeRepository {

    override fun getAllRecipes(): Flow<List<PersonalRecipe>> {
        return personalRecipeDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRecipeById(id: Int): PersonalRecipe? {
        return personalRecipeDao.getById(id)?.toDomain()
    }

    override suspend fun saveRecipe(recipe: PersonalRecipe): Long {
        return personalRecipeDao.insert(recipe.toEntity())
    }

    override suspend fun updateRecipe(recipe: PersonalRecipe) {
        personalRecipeDao.update(recipe.toEntity())
    }

    override suspend fun deleteRecipe(id: Int) {
        personalRecipeDao.deleteById(id)
    }

    private fun PersonalRecipeEntity.toDomain(): PersonalRecipe {
        return PersonalRecipe(
            id = id,
            beanName = beanName,
            techniqueId = techniqueId,
            processId = processId,
            grindSize = GrindSize.valueOf(grindSize),
            ratio = ratio,
            coffeeWeight = coffeeWeight,
            isIce = isIce,
            iceWeight = iceWeight,
            notes = notes,
            createdAt = createdAt
        )
    }

    private fun PersonalRecipe.toEntity(): PersonalRecipeEntity {
        return PersonalRecipeEntity(
            id = id,
            beanName = beanName,
            techniqueId = techniqueId,
            processId = processId,
            grindSize = grindSize.name,
            ratio = ratio,
            coffeeWeight = coffeeWeight,
            isIce = isIce,
            iceWeight = iceWeight,
            notes = notes,
            createdAt = createdAt
        )
    }
}
