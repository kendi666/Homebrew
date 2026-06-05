package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.Grinder
import javax.inject.Inject

/**
 * Built-in grinder profiles used to translate a recommended grind size (microns)
 * into the user's own grinder units. Numbers are approximate filter-range
 * references, not exact specs.
 */
class GetGrindersUseCase @Inject constructor() {

    operator fun invoke(): List<Grinder> = GRINDERS

    companion object {
        private val GRINDERS = listOf(
            Grinder(
                id = "comandante_c40",
                name = "Comandante C40",
                unitLabel = "clicks",
                micronsPerUnit = 30.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 50
            ),
            Grinder(
                id = "1zpresso_jx_pro",
                name = "1Zpresso JX-Pro",
                unitLabel = "clicks",
                micronsPerUnit = 12.5,
                zeroOffsetMicrons = 0.0,
                maxUnits = 120
            ),
            Grinder(
                id = "timemore_c3",
                name = "Timemore C3",
                unitLabel = "clicks",
                micronsPerUnit = 33.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 36
            ),
            Grinder(
                id = "baratza_encore",
                name = "Baratza Encore",
                unitLabel = "steps",
                micronsPerUnit = 38.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 40
            ),
            Grinder(
                id = "kingrinder_k6",
                name = "Kingrinder K6",
                unitLabel = "clicks",
                micronsPerUnit = 16.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 90
            )
        )
    }
}
