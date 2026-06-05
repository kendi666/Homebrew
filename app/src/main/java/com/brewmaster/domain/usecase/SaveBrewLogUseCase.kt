package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.BrewLog
import com.brewmaster.domain.repository.BrewLogRepository
import javax.inject.Inject

class SaveBrewLogUseCase @Inject constructor(
    private val repository: BrewLogRepository
) {
    suspend operator fun invoke(log: BrewLog): Long = repository.saveLog(log)
}
