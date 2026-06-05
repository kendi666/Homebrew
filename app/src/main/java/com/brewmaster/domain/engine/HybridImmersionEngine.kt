package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * v60 Hybrid Immersion (Hario Switch / Clever style).
 *
 * Combines two extraction regimes in one brew:
 *  1. IMMERSION phase — the valve stays CLOSED so the grounds steep fully
 *     submerged. This gives an even, forgiving, full-bodied extraction.
 *  2. PERCOLATION phase — the valve is OPENED so the remaining water flows
 *     through the bed like a normal V60, adding clarity and a clean finish.
 *
 * The split here is roughly 60% of the water steeped during immersion, then the
 * final ~40% poured as percolation after the switch is opened.
 */
class HybridImmersionEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val total = totalHotWater.roundToInt().toDouble()
        val bloomAmount = (coffeeWeight * 2.0).roundToInt().toDouble()
        val immersionCumulative = (total * 0.60).roundToInt().toDouble()
        val immersionAmount = (immersionCumulative - bloomAmount).coerceAtLeast(0.0)
        val percolationAmount = (total - immersionCumulative).coerceAtLeast(0.0)

        return listOf(
            BrewStep(
                order = 1,
                name = "Bloom",
                action = StepAction.BLOOM,
                waterPercentage = bloomAmount / total,
                waterAmount = bloomAmount,
                cumulativeWater = bloomAmount,
                startTimeSec = 0,
                endTimeSec = 45,
                instruction = "Switch CLOSED. Pour ${bloomAmount.roundToInt()}g, swirl to saturate all grounds",
                tip = "Keep the valve closed for the whole immersion phase"
            ),
            BrewStep(
                order = 2,
                name = "Immersion Pour",
                action = StepAction.IMMERSE,
                waterPercentage = immersionAmount / total,
                waterAmount = immersionAmount,
                cumulativeWater = immersionCumulative,
                startTimeSec = 45,
                endTimeSec = 90,
                instruction = "Still CLOSED. Pour up to ${immersionCumulative.roundToInt()}g and let it submerge"
            ),
            BrewStep(
                order = 3,
                name = "Steep",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = immersionCumulative,
                startTimeSec = 90,
                endTimeSec = 165,
                instruction = "Full immersion - let the coffee steep with the switch closed",
                tip = "Longer steep = heavier body and sweetness"
            ),
            BrewStep(
                order = 4,
                name = "Open Switch",
                action = StepAction.RELEASE,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = immersionCumulative,
                startTimeSec = 165,
                endTimeSec = 170,
                instruction = "OPEN the switch to release and start the drawdown"
            ),
            BrewStep(
                order = 5,
                name = "Percolation Pour",
                action = StepAction.POUR,
                waterPercentage = percolationAmount / total,
                waterAmount = percolationAmount,
                cumulativeWater = total,
                startTimeSec = 170,
                endTimeSec = 210,
                instruction = "Switch OPEN. Pour to ${total.roundToInt()}g in slow circles for clarity"
            ),
            BrewStep(
                order = 6,
                name = "Drawdown",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = total,
                startTimeSec = 210,
                endTimeSec = 225,
                instruction = "Let it finish draining completely"
            )
        )
    }
}
