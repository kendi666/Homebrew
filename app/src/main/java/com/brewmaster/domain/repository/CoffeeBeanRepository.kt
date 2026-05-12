package com.brewmaster.domain.repository

import com.brewmaster.domain.model.CoffeeBean
import kotlinx.coroutines.flow.Flow

interface CoffeeBeanRepository {
    fun getAllBeans(): Flow<List<CoffeeBean>>
    suspend fun getBeanById(id: Int): CoffeeBean?
    suspend fun saveBean(bean: CoffeeBean): Long
    suspend fun deleteBean(id: Int)
}
