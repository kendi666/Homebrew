package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

class HoffmannEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val bloomAmount = (coffeeWeight * 2.0).roundToInt().toDouble()
        val pourOneCumulative = (totalHotWater * 0.60).roundToInt().toDouble()
        val pourOneAmount = pourOneCumulative - bloomAmount
        val pourTwoAmount = totalHotWater.roundToInt().toDouble() - pourOneCumulative

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
                instruction = "Pour ${bloomAmount.roundToInt()}g, then gentle swirl"
            ),
            BrewStep(
                order = 2,
                name = "First Pour",
                action = StepAction.POUR,
                waterPercentage = pourOneAmount / totalHotWater,
                waterAmount = pourOneAmount,
                cumulativeWater = pourOneCumulative,
                startTimeSec = 45,
                endTimeSec = 75,
                instruction = "Pour to ${pourOneCumulative.roundToInt()}g in gentle circles"
            ),
            BrewStep(
                order = 3,
                name = "Second Pour",
                action = StepAction.POUR,
                waterPercentage = pourTwoAmount / totalHotWater,
                waterAmount = pourTwoAmount,
                cumulativeWater = totalHotWater.roundToInt().toDouble(),
                startTimeSec = 75,
                endTimeSec = 105,
                instruction = "Pour to ${totalHotWater.roundToInt()}g to finish"
            ),
            BrewStep(
                order = 4,
                name = "Stir",
                action = StepAction.STIR,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = totalHotWater.roundToInt().toDouble(),
                startTimeSec = 105,
                endTimeSec = 110,
                instruction = "Stir gently back and forth with a spoon"
            ),
            BrewStep(
                order = 5,
                name = "Swirl",
                action = StepAction.SWIRL,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = totalHotWater.roundToInt().toDouble(),
                startTimeSec = 110,
                endTimeSec = 115,
                instruction = "Give a single swirl to flatten the bed"
            ),
            BrewStep(
                order = 6,
                name = "Drawdown",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = totalHotWater.roundToInt().toDouble(),
                startTimeSec = 115,
                endTimeSec = 210,
                instruction = "Wait for complete drawdown"
            )
        )
    }
}
