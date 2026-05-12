package com.brewmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brewmaster.data.local.entity.CoffeeProcessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoffeeProcessDao {

    @Query("SELECT * FROM coffee_processes")
    fun getAll(): Flow<List<CoffeeProcessEntity>>

    @Query("SELECT * FROM coffee_processes WHERE id = :id")
    suspend fun getById(id: Int): CoffeeProcessEntity?

    @Query("SELECT COUNT(*) FROM coffee_processes")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(processes: List<CoffeeProcessEntity>)
}
