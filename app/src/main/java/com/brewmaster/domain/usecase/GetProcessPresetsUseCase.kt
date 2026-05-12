package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.CoffeeProcess
import com.brewmaster.domain.repository.CoffeeProcessRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProcessPresetsUseCase @Inject constructor(
    private val repository: CoffeeProcessRepository
) {

    operator fun invoke(): Flow<List<CoffeeProcess>> = repository.getAllProcesses()
}
