package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * Ryan Wibawa double immersion, written for 15 g / 220 g.
 * Switch closed, bloom 100 g at 86°C, open and drain 5 s, close and pour the
 * rest at 92°C over 20 s, then open through 2:00. Other doses keep 100/120.
 * The clock does not scale.
 */
class RyanWibawaEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val total = totalHotWater.roundToInt().toDouble().coerceAtLeast(1.0)
        val bloom = (total * 100.0 / 220.0).roundToInt().toDouble().coerceAtMost(total)
        val second = (total - bloom).coerceAtLeast(0.0)
        val bloomSec = kettlePourSec(bloom, 40, KETTLE_G_PER_SEC)

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
                pourInstruction = "Switch CLOSED. Pour ${bloom.roundToInt()}g at 86°C in ${bloomSec}s",
                waitInstruction = "Valve stays closed until 0:40",
                pourTip = "First immersion is the cooler pour"
            ) + listOf(
                BrewStep(
                    order = 0,
                    name = "Open",
                    action = StepAction.RELEASE,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = bloom,
                    startTimeSec = 40,
                    endTimeSec = 45,
                    instruction = "OPEN the switch. Drain about 5 seconds"
                ),
                BrewStep(
                    order = 0,
                    name = "Pour",
                    action = StepAction.POUR,
                    waterPercentage = second / total,
                    waterAmount = second,
                    cumulativeWater = total,
                    startTimeSec = 45,
                    endTimeSec = 65,
                    instruction = "Switch CLOSED. Pour to ${total.roundToInt()}g at 92°C in 20s",
                    tip = "Second immersion is the hotter pour"
                ),
                BrewStep(
                    order = 0,
                    name = "Open",
                    action = StepAction.RELEASE,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = total,
                    startTimeSec = 65,
                    endTimeSec = 120,
                    instruction = "OPEN the switch. Draw down to 2:00",
                    tip = "Swirl the cup gently before serving"
                )
            )
            ).withOrders()
    }
}
