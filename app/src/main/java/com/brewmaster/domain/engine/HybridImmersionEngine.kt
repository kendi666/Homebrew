package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * Sweet Switch hybrid.
 *
 * Clock stays put (bloom until 0:40, closed hold until 1:20, open pours by 1:50
 * and 2:05). Bloom is 3× dose. The closed steep stops at about 7.3× dose, and
 * never past half the kettle — at 1:14.5 those are the same (~110 g on 15 g).
 * Water added by a longer ratio (1:17, 1:18) goes into the two open pours.
 * Drawdown gains 10s for each ratio point above 15.
 * Pouring is only the start of each phase; the rest is a closed hold or a wait.
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

        val bloomSec = kettlePourSec(bloom, 40, BLOOM_G_PER_SEC)
        val immerseAmount = (closed - bloom).coerceAtLeast(0.0)
        val immerseSec = kettlePourSec(immerseAmount, 40, KETTLE_G_PER_SEC)
        val firstSec = kettlePourSec(firstOpen, 25, KETTLE_G_PER_SEC)
        val secondSec = kettlePourSec(secondOpen, 15, KETTLE_G_PER_SEC)

        return (
            timedPour(
                name = "Bloom",
                action = StepAction.BLOOM,
                amount = bloom,
                cumulative = bloom,
                total = total,
                startSec = 0,
                endSec = 40,
                pourSec = bloomSec,
                pourInstruction = "Switch CLOSED. Pour ${bloom.roundToInt()}g in ${bloomSec}s, light swirl",
                waitInstruction = "Valve stays closed until 0:40",
                pourTip = "Keep the valve closed",
            ) + timedPour(
                name = "Immersion",
                action = StepAction.IMMERSE,
                amount = immerseAmount,
                cumulative = closed,
                total = total,
                startSec = 40,
                endSec = 80,
                pourSec = immerseSec,
                pourInstruction = "Still CLOSED. Pour to ${closed.roundToInt()}g in ${immerseSec}s",
                waitInstruction = "Hold, valve closed, until 1:20",
                pourTip = "Short steep — don't stir",
            ) + listOf(
                BrewStep(
                    order = 0,
                    name = "Open",
                    action = StepAction.RELEASE,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = closed,
                    startTimeSec = 80,
                    endTimeSec = 85,
                    instruction = "OPEN the switch",
                )
            ) + timedPour(
                name = "Pour 1",
                action = StepAction.POUR,
                amount = firstOpen,
                cumulative = afterFirst,
                total = total,
                startSec = 85,
                endSec = 110,
                pourSec = firstSec,
                pourInstruction = "Pour to ${afterFirst.roundToInt()}g in ${firstSec}s",
                waitInstruction = "Wait until 1:50",
                pourTip = "Swirl once after this pour",
            ) + timedPour(
                name = "Pour 2",
                action = StepAction.POUR,
                amount = secondOpen,
                cumulative = total,
                total = total,
                startSec = 110,
                endSec = pourTwoEnd,
                pourSec = secondSec,
                pourInstruction = "Pour to ${total.roundToInt()}g in ${secondSec}s",
                waitInstruction = "Wait until ${phaseClock(pourTwoEnd)}",
            ) + listOf(
                BrewStep(
                    order = 0,
                    name = "Drawdown",
                    action = StepAction.WAIT,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = total,
                    startTimeSec = pourTwoEnd,
                    endTimeSec = finish,
                    instruction = "Target finish ~${finish / 60}:${"%02d".format(finish % 60)}",
                    tip = "Bitter or dry → coarser grind, or drop to 92°C",
                )
            )
            ).withOrders()
    }

    private companion object {
        const val BASE_FINISH_SEC = 160
    }
}
