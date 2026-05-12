package com.brewmaster.data.repository

import com.brewmaster.data.local.dao.CoffeeProcessDao
import com.brewmaster.data.local.entity.CoffeeProcessEntity
import com.brewmaster.domain.model.CoffeeProcess
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.domain.repository.CoffeeProcessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CoffeeProcessRepositoryImpl @Inject constructor(
    private val coffeeProcessDao: CoffeeProcessDao
) : CoffeeProcessRepository {

    override fun getAllProcesses(): Flow<List<CoffeeProcess>> {
        return coffeeProcessDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProcessById(id: Int): CoffeeProcess? {
        return coffeeProcessDao.getById(id)?.toDomain()
    }

    private fun CoffeeProcessEntity.toDomain(): CoffeeProcess {
        return CoffeeProcess(
            id = id,
            processName = processName,
            tempMin = tempMin,
            tempMax = tempMax,
            grindRecommendation = GrindSize.valueOf(grindRecommendation),
            extractionNote = extractionNote,
            restingDays = restingDays,
            ratioMin = ratioMin
        )
    }
}
