package com.brewmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brewmaster.data.local.entity.CoffeeBeanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoffeeBeanDao {
    @Query("SELECT * FROM coffee_beans ORDER BY name ASC")
    fun getAll(): Flow<List<CoffeeBeanEntity>>

    @Query("SELECT * FROM coffee_beans WHERE id = :id")
    suspend fun getById(id: Int): CoffeeBeanEntity?

    @Query("SELECT COUNT(*) FROM coffee_beans")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bean: CoffeeBeanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(beans: List<CoffeeBeanEntity>)

    @Delete
    suspend fun delete(bean: CoffeeBeanEntity)

    @Query("DELETE FROM coffee_beans WHERE id = :id")
    suspend fun deleteById(id: Int)
}
