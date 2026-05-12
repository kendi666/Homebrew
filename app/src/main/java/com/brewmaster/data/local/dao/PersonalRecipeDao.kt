package com.brewmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.brewmaster.data.local.entity.PersonalRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecipeDao {

    @Query("SELECT * FROM personal_recipes")
    fun getAll(): Flow<List<PersonalRecipeEntity>>

    @Query("SELECT * FROM personal_recipes WHERE id = :id")
    suspend fun getById(id: Int): PersonalRecipeEntity?

    @Insert
    suspend fun insert(recipe: PersonalRecipeEntity): Long

    @Update
    suspend fun update(recipe: PersonalRecipeEntity)

    @Delete
    suspend fun delete(recipe: PersonalRecipeEntity)

    @Query("DELETE FROM personal_recipes WHERE id = :id")
    suspend fun deleteById(id: Int)
}
