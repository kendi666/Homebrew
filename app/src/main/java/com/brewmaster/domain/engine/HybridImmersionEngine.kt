package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * Sweet Switch hybrid.
 *
 * Clock stays put (bloom 40s, closed hold until 1:20, open pours at 1:20 and 1:50).
 * Bloom is 3× dose. The closed steep stops at about 7.3× dose, and never past half
 * the kettle — at 1:14.5 those are the same (~110 g on 15 g). Water added by a
 * longer ratio (1:17, 1:18) goes into the two open pours. Drawdown gains 10s for
 * each ratio point above 15.
 */
class HybridImmersionEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val total = totalHotWater.roundToInt().toDouble().coerceAtLeast(1.0)
        val bloom = (coffeeWeight * 3.0).roundToInt().toDouble().coerceAtMost(total)
        val closedCap = (coffeeWeight * 110.0 / 15.0).roundToInt().toDouble()
        val half = (total * 0.5).roundToInt().toDouble()
        // 1:14.5 lands 1 g under the 7.3× cap after rounding; keep the card's 110 g.
        val closedTarget = if (half >= closedCap - 1.0) closedCap else half
        val closed = closedTarget.coerceIn(bloom, total)
        val remain = (total - closed).coerceAtLeast(0.0)
        val firstOpen = (remain / 2.0).roundToInt().toDouble()
        val afterFirst = (closed + firstOpen).coerceAtMost(total)
        val secondOpen = (total - afterFirst).coerceAtLeast(0.0)

        val ratio = if (coffeeWeight > 0.0) total / coffeeWeight else 15.0
        val extraDrawdown = ((ratio - 15.0).coerceAtLeast(0.0) * 10.0).roundToInt()
        val target = BASE_FINISH_SEC + extraDrawdown
        val pourTwoEnd = 125
        val finish = maxOf(target, pourTwoEnd + 15)

        return listOf(
            BrewStep(
                order = 1,
                name = "Bloom",
                action = StepAction.BLOOM,
                waterPercentage = bloom / total,
                waterAmount = bloom,
                cumulativeWater = bloom,
                startTimeSec = 0,
                endTimeSec = 40,
                instruction = "Switch CLOSED. Pour ${bloom.roundToInt()}g, light swirl",
                tip = "Keep the valve closed"
            ),
            BrewStep(
                order = 2,
                name = "Immersion",
                action = StepAction.IMMERSE,
                waterPercentage = (closed - bloom).coerceAtLeast(0.0) / total,
                waterAmount = (closed - bloom).coerceAtLeast(0.0),
                cumulativeWater = closed,
                startTimeSec = 40,
                endTimeSec = 80,
                instruction = "Still CLOSED. Pour to ${closed.roundToInt()}g and hold ~40s",
                tip = "Short steep — don't stir"
            ),
            BrewStep(
                order = 3,
                name = "Open",
                action = StepAction.RELEASE,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = closed,
                startTimeSec = 80,
                endTimeSec = 85,
                instruction = "OPEN the switch"
            ),
            BrewStep(
                order = 4,
                name = "Pour 1",
                action = StepAction.POUR,
                waterPercentage = firstOpen / total,
                waterAmount = firstOpen,
                cumulativeWater = afterFirst,
                startTimeSec = 85,
                endTimeSec = 110,
                instruction = "Pour to ${afterFirst.roundToInt()}g",
                tip = "Swirl once after this pour"
            ),
            BrewStep(
                order = 5,
                name = "Pour 2",
                action = StepAction.POUR,
                waterPercentage = secondOpen / total,
                waterAmount = secondOpen,
                cumulativeWater = total,
                startTimeSec = 110,
                endTimeSec = pourTwoEnd,
                instruction = "Pour to ${total.roundToInt()}g"
            ),
            BrewStep(
                order = 6,
                name = "Drawdown",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = total,
                startTimeSec = pourTwoEnd,
                endTimeSec = finish,
                instruction = "Target finish ~${finish / 60}:${"%02d".format(finish % 60)}",
                tip = "Bitter or dry → coarser grind, or drop to 92°C"
            )
        )
    }

    private companion object {
        const val BASE_FINISH_SEC = 160
    }
}
