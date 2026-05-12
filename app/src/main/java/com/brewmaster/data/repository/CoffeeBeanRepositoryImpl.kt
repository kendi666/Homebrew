package com.brewmaster.data.repository

import com.brewmaster.data.local.dao.CoffeeBeanDao
import com.brewmaster.data.local.entity.CoffeeBeanEntity
import com.brewmaster.domain.model.CoffeeBean
import com.brewmaster.domain.repository.CoffeeBeanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CoffeeBeanRepositoryImpl @Inject constructor(
    private val coffeeBeanDao: CoffeeBeanDao
) : CoffeeBeanRepository {

    override fun getAllBeans(): Flow<List<CoffeeBean>> {
        return coffeeBeanDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBeanById(id: Int): CoffeeBean? {
        return coffeeBeanDao.getById(id)?.toDomain()
    }

    override suspend fun saveBean(bean: CoffeeBean): Long {
        return coffeeBeanDao.insert(bean.toEntity())
    }

    override suspend fun deleteBean(id: Int) {
        coffeeBeanDao.deleteById(id)
    }

    private fun CoffeeBeanEntity.toDomain(): CoffeeBean {
        return CoffeeBean(
            id = id,
            name = name,
            origin = origin,
            processId = processId,
            processName = processName,
            roastLevel = roastLevel,
            notes = notes,
            restingDays = restingDays
        )
    }

    private fun CoffeeBean.toEntity(): CoffeeBeanEntity {
        return CoffeeBeanEntity(
            id = id,
            name = name,
            origin = origin,
            processId = processId,
            processName = processName,
            roastLevel = roastLevel,
            notes = notes,
            restingDays = restingDays
        )
    }
}
