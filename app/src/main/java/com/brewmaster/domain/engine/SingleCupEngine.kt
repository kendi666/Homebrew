package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

class SingleCupEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val bloomAmount = (totalHotWater * 0.20).roundToInt().toDouble()
        val mainPourAmount = totalHotWater.roundToInt().toDouble() - bloomAmount

        return listOf(
            BrewStep(
                order = 1,
                name = "Bloom",
                action = StepAction.BLOOM,
                waterPercentage = bloomAmount / totalHotWater,
                waterAmount = bloomAmount,
                cumulativeWater = bloomAmount,
                startTimeSec = 0,
                endTimeSec = 45,
                instruction = "Pour ${bloomAmount.roundToInt()}g for bloom, wait 45 seconds"
            ),
            BrewStep(
                order = 2,
                name = "Main Pour",
                action = StepAction.POUR,
                waterPercentage = mainPourAmount / totalHotWater,
                waterAmount = mainPourAmount,
                cumulativeWater = totalHotWater.roundToInt().toDouble(),
                startTimeSec = 45,
                endTimeSec = 120,
                instruction = "Slow circular pour to ${totalHotWater.roundToInt()}g"
            ),
            BrewStep(
                order = 3,
                name = "Drawdown",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = totalHotWater.roundToInt().toDouble(),
                startTimeSec = 120,
                endTimeSec = 180,
                instruction = "Wait for complete drawdown"
            )
        )
    }
}
