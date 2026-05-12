package com.brewmaster.domain.repository

import com.brewmaster.domain.model.CoffeeProcess
import kotlinx.coroutines.flow.Flow

interface CoffeeProcessRepository {
    fun getAllProcesses(): Flow<List<CoffeeProcess>>
    suspend fun getProcessById(id: Int): CoffeeProcess?
}
