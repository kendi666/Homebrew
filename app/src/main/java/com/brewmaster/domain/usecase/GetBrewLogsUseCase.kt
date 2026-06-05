package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.BrewLog
import com.brewmaster.domain.repository.BrewLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBrewLogsUseCase @Inject constructor(
    private val repository: BrewLogRepository
) {
    operator fun invoke(): Flow<List<BrewLog>> = repository.getAllLogs()
}
