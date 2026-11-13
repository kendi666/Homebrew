package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * Kasuya's improved Devil's recipe on a V60 Switch.
 * Card (20 g / 300 g): closed bloom 50 g, open to 120 g, open to 200 g,
 * closed pour to 300 g at 70°C, open for drawdown. Finish 3:30.
 * First three pours are 92°C. Other doses keep 50/70/80/100 of 300.
 */
class KasuyaDevilsEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val bloom = (totalHotWater * 50.0 / 300.0).roundToInt().toDouble()
        val second = (totalHotWater * 70.0 / 300.0).roundToInt().toDouble()
        val third = (totalHotWater * 80.0 / 300.0).roundToInt().toDouble()
        val last = (totalHotWater - bloom - second - third).coerceAtLeast(0.0)
        val at120 = bloom + second
        val at200 = at120 + third

        val bloomSec = kettlePourSec(bloom, 40, KETTLE_G_PER_SEC)
        val secondSec = kettlePourSec(second, 50, KETTLE_G_PER_SEC)
        val thirdSec = kettlePourSec(third, 40, KETTLE_G_PER_SEC)
        val lastSec = kettlePourSec(last, 35, KETTLE_G_PER_SEC)

        return (
            timedPour(
                name = "Bloom",
                action = StepAction.BLOOM,
                amount = bloom,
                cumulative = bloom,
                total = totalHotWater,
                startSec = 0,
                endSec = 40,
                pourSec = bloomSec,
                pourInstruction = "Switch CLOSED. Pour ${bloom.roundToInt()}g at 92°C in ${bloomSec}s",
                waitInstruction = "Valve stays closed until 0:40",
                pourTip = "First three pours are 92°C",
            ) + timedPour(
                name = "Pour 2",
                action = StepAction.POUR,
                amount = second,
                cumulative = at120,
                total = totalHotWater,
                startSec = 40,
                endSec = 90,
                pourSec = secondSec,
                pourInstruction = "OPEN the switch. Pour ${second.roundToInt()}g at 92°C in ${secondSec}s",
                waitInstruction = "Valve stays open until 1:30",
                waitAction = StepAction.RELEASE,
                waitName = "Open",
            ) + timedPour(
                name = "Pour 3",
                action = StepAction.POUR,
                amount = third,
                cumulative = at200,
                total = totalHotWater,
                startSec = 90,
                endSec = 130,
                pourSec = thirdSec,
                pourInstruction = "Pour ${third.roundToInt()}g at 92°C in ${thirdSec}s",
                waitInstruction = "Valve stays open until 2:10",
                waitAction = StepAction.RELEASE,
                waitName = "Open",
            ) + timedPour(
                name = "Pour 4",
                action = StepAction.POUR,
                amount = last,
                cumulative = totalHotWater,
                total = totalHotWater,
                startSec = 130,
                endSec = 165,
                pourSec = lastSec,
                pourInstruction = "Switch CLOSED. Pour ${last.roundToInt()}g at 70°C in ${lastSec}s",
                waitInstruction = "Valve stays closed until 2:45. This pour is 70°C",
                pourTip = "Last pour is cooler: 70°C, not 92°C",
            ) + listOf(
                BrewStep(
                    order = 0,
                    name = "Open",
                    action = StepAction.RELEASE,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = totalHotWater,
                    startTimeSec = 165,
                    endTimeSec = 210,
                    instruction = "OPEN the switch. Draw down to 3:30",
                    tip = "The last pour was 70°C",
                )
            )
            ).withOrders()
    }
}
