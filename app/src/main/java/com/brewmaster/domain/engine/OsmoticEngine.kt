package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

class OsmoticEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val centerBloom = (totalHotWater * 0.13).roundToInt().toDouble()
        val expandingPour = totalHotWater.roundToInt().toDouble() - centerBloom

        return listOf(
            BrewStep(
                order = 1,
                name = "Center Bloom",
                action = StepAction.OSMOTIC,
                waterPercentage = centerBloom / totalHotWater,
                waterAmount = centerBloom,
                cumulativeWater = centerBloom,
                startTimeSec = 0,
                endTimeSec = 30,
                instruction = "Thin stream at center only, pour ${centerBloom.roundToInt()}g"
            ),
            BrewStep(
                order = 2,
                name = "Expanding Pour",
                action = StepAction.OSMOTIC,
                waterPercentage = expandingPour / totalHotWater,
                waterAmount = expandingPour,
                cumulativeWater = totalHotWater.roundToInt().toDouble(),
                startTimeSec = 30,
                endTimeSec = 150,
                instruction = "Thin stream expanding outward - do NOT touch the paper filter",
                tip = "The rising dome acts as a natural filter for clarity"
            ),
            BrewStep(
                order = 3,
                name = "Dome Drain",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = totalHotWater.roundToInt().toDouble(),
                startTimeSec = 150,
                endTimeSec = 210,
                instruction = "Wait for drawdown - maintain the dome shape"
            )
        )
    }
}
