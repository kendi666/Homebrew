package com.brewmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.brewmaster.data.local.entity.BrewLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrewLogDao {

    @Query("SELECT * FROM brew_logs ORDER BY created_at DESC")
    fun getAll(): Flow<List<BrewLogEntity>>

    @Insert
    suspend fun insert(log: BrewLogEntity): Long

    @Query("DELETE FROM brew_logs WHERE id = :id")
    suspend fun deleteById(id: Int)
}
