package com.brewmaster.data.repository

import com.brewmaster.data.local.dao.BrewLogDao
import com.brewmaster.data.local.entity.BrewLogEntity
import com.brewmaster.domain.model.BrewLog
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.domain.repository.BrewLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BrewLogRepositoryImpl @Inject constructor(
    private val brewLogDao: BrewLogDao
) : BrewLogRepository {

    override fun getAllLogs(): Flow<List<BrewLog>> {
        return brewLogDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveLog(log: BrewLog): Long {
        return brewLogDao.insert(log.toEntity())
    }

    override suspend fun deleteLog(id: Int) {
        brewLogDao.deleteById(id)
    }

    private fun BrewLogEntity.toDomain(): BrewLog {
        return BrewLog(
            id = id,
            beanName = beanName,
            techniqueId = techniqueId,
            processId = processId,
            grindSize = runCatching { GrindSize.valueOf(grindSize) }.getOrDefault(GrindSize.MEDIUM),
            ratio = ratio,
            coffeeWeight = coffeeWeight,
            isIce = isIce,
            tempUsed = tempUsed,
            rating = rating,
            notes = notes,
            createdAt = createdAt
        )
    }

    private fun BrewLog.toEntity(): BrewLogEntity {
        return BrewLogEntity(
            id = id,
            beanName = beanName,
            techniqueId = techniqueId,
            processId = processId,
            grindSize = grindSize.name,
            ratio = ratio,
            coffeeWeight = coffeeWeight,
            isIce = isIce,
            tempUsed = tempUsed,
            rating = rating,
            notes = notes,
            createdAt = createdAt
        )
    }
}
