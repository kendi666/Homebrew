package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * Matt Winton-inspired five-pour V60 method.
 *
 * Uses a coarse grind and repeated pulse pours. Championship versions use a
 * hotter first phase and cooler later pours; here that is represented in the
 * instructions while BrewMaster keeps one recommended temp range.
 * Each pulse is a short pour; the rest of the 30s is the bed running down.
 */
class MattWintonFivePourEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val total = totalHotWater.roundToInt().toDouble()
        val pourSize = (total / POUR_COUNT).roundToInt().toDouble()

        val pours = (1..POUR_COUNT).flatMap { order ->
            val isFinalPour = order == POUR_COUNT
            val amount = if (isFinalPour) (total - pourSize * (POUR_COUNT - 1)).coerceAtLeast(0.0) else pourSize
            val cumulative = if (isFinalPour) total else (pourSize * order).roundToInt().toDouble()
            val start = (order - 1) * 30
            val end = start + 30
            val tempHint = if (order <= 2) "93°C" else "88-90°C"
            val pourSec = kettlePourSec(amount, end - start, RAPID_G_PER_SEC)

            timedPour(
                name = if (order == 1) "Bloom Pour" else "Pour $order",
                action = if (order == 1) StepAction.BLOOM else StepAction.PULSE,
                amount = amount,
                cumulative = cumulative,
                total = total,
                startSec = start,
                endSec = end,
                pourSec = pourSec,
                pourInstruction = "Add ${amount.roundToInt()}g in ${pourSec}s with $tempHint water, up to ${cumulative.roundToInt()}g",
                waitInstruction = "Let the bed run down until ${phaseClock(end)}",
                pourTip = when (order) {
                    1 -> "Pour aggressively from center outward to saturate the bed"
                    3 -> "Championship routine shifts cooler here for a juicier body"
                    POUR_COUNT -> "Start each pulse as the bed begins to run dry"
                    else -> null
                },
            )
        }

        return (pours + BrewStep(
            order = 0,
            name = "Drawdown",
            action = StepAction.WAIT,
            waterPercentage = 0.0,
            waterAmount = 0.0,
            cumulativeWater = total,
            startTimeSec = 150,
            endTimeSec = 210,
            instruction = "Let the brew finish draining completely",
            tip = "Target finish around 3:00-3:30; adjust K-Ultra 3-5 clicks if needed"
        )).withOrders()
    }

    private companion object {
        const val POUR_COUNT = 5
    }
}
