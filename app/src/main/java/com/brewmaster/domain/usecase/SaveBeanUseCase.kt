package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.CoffeeBean
import com.brewmaster.domain.repository.CoffeeBeanRepository
import javax.inject.Inject

class SaveBeanUseCase @Inject constructor(
    private val repository: CoffeeBeanRepository
) {
    suspend operator fun invoke(bean: CoffeeBean): Long = repository.saveBean(bean)
}
