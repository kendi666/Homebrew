package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.CoffeeBean
import com.brewmaster.domain.repository.CoffeeBeanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBeansUseCase @Inject constructor(
    private val repository: CoffeeBeanRepository
) {
    operator fun invoke(): Flow<List<CoffeeBean>> = repository.getAllBeans()
}
