package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * Concentrate brew (~1:11) then dilute with hot water in the server.
 * totalHotWater = full cup water (brew + bypass).
 */
class BypassEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val brewWater = (coffeeWeight * 11.0)
            .coerceAtMost(totalHotWater * 0.85)
            .coerceAtLeast(coffeeWeight * 8)
        val bypass = (totalHotWater - brewWater).coerceAtLeast(0.0)
        val bloom = (coffeeWeight * 3.0).coerceAtMost(brewWater * 0.4)
        val pour = (brewWater - bloom).coerceAtLeast(0.0)
        val brewEnd = brewWater.roundToInt().toDouble()

        return listOf(
            BrewStep(
                order = 1,
                name = "Bloom",
                action = StepAction.BLOOM,
                waterPercentage = bloom / totalHotWater,
                waterAmount = bloom,
                cumulativeWater = bloom,
                startTimeSec = 0,
                endTimeSec = 35,
                instruction = "Pour ${bloom.roundToInt()}g for bloom, swirl"
            ),
            BrewStep(
                order = 2,
                name = "Concentrate",
                action = StepAction.POUR,
                waterPercentage = pour / totalHotWater,
                waterAmount = pour,
                cumulativeWater = brewEnd,
                startTimeSec = 35,
                endTimeSec = 105,
                instruction = "Pour to ${brewEnd.roundToInt()}g in 2 phases (concentrate ~1:11)"
            ),
            BrewStep(
                order = 3,
                name = "Drawdown",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = brewEnd,
                startTimeSec = 105,
                endTimeSec = 150,
                instruction = "Wait for drip to finish"
            ),
            BrewStep(
                order = 4,
                name = "Bypass",
                action = StepAction.POUR,
                waterPercentage = bypass / totalHotWater,
                waterAmount = bypass,
                cumulativeWater = totalHotWater.roundToInt().toDouble(),
                startTimeSec = 150,
                endTimeSec = 180,
                instruction = "Add ${bypass.roundToInt()}g hot water (~94°C) into the server, swirl",
                tip = "Strong brew, smooth cup — acidity softens without over-extracting"
            )
        )
    }
}
