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
 */
class MattWintonFivePourEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val total = totalHotWater.roundToInt().toDouble()
        val pourSize = (total / POUR_COUNT).roundToInt().toDouble()

        val pours = (1..POUR_COUNT).map { order ->
            val isFinalPour = order == POUR_COUNT
            val amount = if (isFinalPour) (total - pourSize * (POUR_COUNT - 1)).coerceAtLeast(0.0) else pourSize
            val cumulative = if (isFinalPour) total else (pourSize * order).roundToInt().toDouble()
            val start = (order - 1) * 30
            val end = start + 30
            val tempHint = if (order <= 2) "93°C" else "88-90°C"

            BrewStep(
                order = order,
                name = if (order == 1) "Bloom Pour" else "Pour $order",
                action = if (order == 1) StepAction.BLOOM else StepAction.PULSE,
                waterPercentage = amount / total,
                waterAmount = amount,
                cumulativeWater = cumulative,
                startTimeSec = start,
                endTimeSec = end,
                instruction = "Add ${amount.roundToInt()}g with $tempHint water, up to ${cumulative.roundToInt()}g total",
                tip = when (order) {
                    1 -> "Pour aggressively from center outward to saturate the bed"
                    3 -> "Championship routine shifts cooler here for a juicier body"
                    POUR_COUNT -> "Start each pulse as the bed begins to run dry"
                    else -> null
                }
            )
        }

        return pours + BrewStep(
            order = POUR_COUNT + 1,
            name = "Drawdown",
            action = StepAction.WAIT,
            waterPercentage = 0.0,
            waterAmount = 0.0,
            cumulativeWater = total,
            startTimeSec = 150,
            endTimeSec = 210,
            instruction = "Let the brew finish draining completely",
            tip = "Target finish around 3:00-3:30; adjust K-Ultra 3-5 clicks if needed"
        )
    }

    private companion object {
        const val POUR_COUNT = 5
    }
}
