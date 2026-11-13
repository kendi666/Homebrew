package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * Concentrate brew (~1:11) then dilute with hot water in the server.
 * totalHotWater = full cup water (brew + bypass).
 * The concentrate goes in as two short pulses; the clock between them is a wait.
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
        val first = (pour / 2.0).roundToInt().toDouble()
        val second = (pour - first).coerceAtLeast(0.0)
        val afterFirst = (bloom + first).coerceAtMost(brewEnd)

        val bloomSec = kettlePourSec(bloom, 35, BLOOM_G_PER_SEC)
        val firstSec = kettlePourSec(first, 35, KETTLE_G_PER_SEC)
        val secondSec = kettlePourSec(second, 35, KETTLE_G_PER_SEC)
        val bypassSec = kettlePourSec(bypass, 30, KETTLE_G_PER_SEC)

        return (
            timedPour(
                name = "Bloom",
                action = StepAction.BLOOM,
                amount = bloom,
                cumulative = bloom,
                total = totalHotWater,
                startSec = 0,
                endSec = 35,
                pourSec = bloomSec,
                pourInstruction = "Pour ${bloom.roundToInt()}g in ${bloomSec}s, swirl",
                waitInstruction = "Let it bloom until 0:35",
            ) + timedPour(
                name = "Concentrate 1",
                action = StepAction.POUR,
                amount = first,
                cumulative = afterFirst,
                total = totalHotWater,
                startSec = 35,
                endSec = 70,
                pourSec = firstSec,
                pourInstruction = "Pour to ${afterFirst.roundToInt()}g in ${firstSec}s",
                waitInstruction = "Wait until 1:10",
            ) + timedPour(
                name = "Concentrate 2",
                action = StepAction.POUR,
                amount = second,
                cumulative = brewEnd,
                total = totalHotWater,
                startSec = 70,
                endSec = 105,
                pourSec = secondSec,
                pourInstruction = "Pour to ${brewEnd.roundToInt()}g in ${secondSec}s",
                waitInstruction = "Wait until 1:45",
                pourTip = "Concentrate is about 1:11",
            ) + listOf(
                BrewStep(
                    order = 0,
                    name = "Drawdown",
                    action = StepAction.WAIT,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = brewEnd,
                    startTimeSec = 105,
                    endTimeSec = 150,
                    instruction = "Wait for drip to finish",
                )
            ) + timedPour(
                name = "Bypass",
                action = StepAction.POUR,
                amount = bypass,
                cumulative = totalHotWater.roundToInt().toDouble(),
                total = totalHotWater,
                startSec = 150,
                endSec = 180,
                pourSec = bypassSec,
                pourInstruction = "Add ${bypass.roundToInt()}g hot water (~94°C) into the server",
                waitInstruction = "Swirl the server",
                pourTip = "Strong brew, smooth cup — acidity softens without over-extracting",
            )
            ).withOrders()
    }
}
