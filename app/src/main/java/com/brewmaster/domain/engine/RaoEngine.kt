package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

class RaoEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val bloomAmount = (coffeeWeight * 3.0).roundToInt().toDouble()
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
                    name = "Excavate",
                    action = StepAction.EXCAVATE,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = bloomAmount,
                    startTimeSec = 45,
                    endTimeSec = 50,
                    instruction = "Excavate: stir to ensure all grounds are wet",
                ),
                BrewStep(
                    order = 0,
                    name = "Main Pour",
                    action = StepAction.POUR,
                    waterPercentage = mainPourAmount / totalHotWater,
                    waterAmount = mainPourAmount,
                    cumulativeWater = totalHotWater.roundToInt().toDouble(),
                    startTimeSec = 50,
                    endTimeSec = 105,
                    instruction = "Single continuous pour to ${totalHotWater.roundToInt()}g in steady circles",
                ),
                BrewStep(
                    order = 0,
                    name = "Swirl",
                    action = StepAction.SWIRL,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = totalHotWater.roundToInt().toDouble(),
                    startTimeSec = 105,
                    endTimeSec = 110,
                    instruction = "Spin the dripper to flatten the coffee bed",
                ),
                BrewStep(
                    order = 0,
                    name = "Drawdown",
                    action = StepAction.WAIT,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = totalHotWater.roundToInt().toDouble(),
                    startTimeSec = 110,
                    endTimeSec = 180,
                    instruction = "Wait for complete drawdown",
                ),
            )
            ).withOrders()
    }
}
