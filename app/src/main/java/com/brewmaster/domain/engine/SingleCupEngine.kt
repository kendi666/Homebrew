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
        val bloomSec = kettlePourSec(bloomAmount, 45, BLOOM_G_PER_SEC)

        return (
            timedPour(
                name = "Bloom",
                action = StepAction.BLOOM,
                amount = bloomAmount,
                cumulative = bloomAmount,
                total = totalHotWater,
                startSec = 0,
                endSec = 45,
                pourSec = bloomSec,
                pourInstruction = "Pour ${bloomAmount.roundToInt()}g in ${bloomSec}s",
                waitInstruction = "Let it bloom until 0:45",
            ) + listOf(
                BrewStep(
                    order = 0,
                    name = "Main Pour",
                    action = StepAction.POUR,
                    waterPercentage = mainPourAmount / totalHotWater,
                    waterAmount = mainPourAmount,
                    cumulativeWater = totalHotWater.roundToInt().toDouble(),
                    startTimeSec = 45,
                    endTimeSec = 120,
                    instruction = "Slow circular pour to ${totalHotWater.roundToInt()}g",
                ),
                BrewStep(
                    order = 0,
                    name = "Drawdown",
                    action = StepAction.WAIT,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = totalHotWater.roundToInt().toDouble(),
                    startTimeSec = 120,
                    endTimeSec = 180,
                    instruction = "Wait for complete drawdown",
                ),
            )
            ).withOrders()
    }
}
