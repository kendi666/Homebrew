package com.brewmaster.domain.repository

import com.brewmaster.domain.model.BrewLog
import kotlinx.coroutines.flow.Flow

interface BrewLogRepository {
    fun getAllLogs(): Flow<List<BrewLog>>
    suspend fun saveLog(log: BrewLog): Long
    suspend fun deleteLog(id: Int)
}
