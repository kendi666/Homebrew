package com.brewmaster.domain.usecase

import com.brewmaster.domain.repository.BrewLogRepository
import javax.inject.Inject

class DeleteBrewLogUseCase @Inject constructor(
    private val repository: BrewLogRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteLog(id)
}
