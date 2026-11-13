package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * Selene — card recipe written for 10 g / 190 g (1:19), 91–92°C, medium.
 * Pours scale with the kettle so another dose keeps the same split:
 * 30 + 80 + 40 + 40 on 190 g. The minute marks are bloom and drawdown, not
 * a minute of pouring. Finish sits at the card's 3:45 mark.
 */
class SeleneEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val total = totalHotWater.roundToInt().toDouble().coerceAtLeast(1.0)
        val bloom = (total * 30.0 / 190.0).roundToInt().toDouble().coerceAtMost(total)
        val pourTwo = (total * 80.0 / 190.0).roundToInt().toDouble()
        val pourThree = (total * 40.0 / 190.0).roundToInt().toDouble()
        val cumTwo = (bloom + pourTwo).coerceAtMost(total)
        val cumThree = (cumTwo + pourThree).coerceAtMost(total)
        val pourFour = (total - cumThree).coerceAtLeast(0.0)

        val bloomSec = kettlePourSec(bloom, 60, BLOOM_G_PER_SEC)
        val secondSec = kettlePourSec(cumTwo - bloom, 70, KETTLE_G_PER_SEC)
        val thirdSec = kettlePourSec(cumThree - cumTwo, 50, KETTLE_G_PER_SEC)
        val fourthSec = kettlePourSec(pourFour, 30, KETTLE_G_PER_SEC)

        return (
            timedPour(
                name = "Bloom",
                action = StepAction.BLOOM,
                amount = bloom,
                cumulative = bloom,
                total = total,
                startSec = 0,
                endSec = 60,
                pourSec = bloomSec,
                pourInstruction = "Pour ${bloom.roundToInt()}g in ${bloomSec}s",
                waitInstruction = "Let it bloom until 1:00",
                pourTip = "Steady pour, not too slow and not too fast",
            ) + timedPour(
                name = "Pour 2",
                action = StepAction.POUR,
                amount = (cumTwo - bloom).coerceAtLeast(0.0),
                cumulative = cumTwo,
                total = total,
                startSec = 60,
                endSec = 130,
                pourSec = secondSec,
                pourInstruction = "Pour to ${cumTwo.roundToInt()}g in ${secondSec}s",
                waitInstruction = "Wait until the bed dries",
            ) + timedPour(
                name = "Pour 3",
                action = StepAction.POUR,
                amount = (cumThree - cumTwo).coerceAtLeast(0.0),
                cumulative = cumThree,
                total = total,
                startSec = 130,
                endSec = 180,
                pourSec = thirdSec,
                pourInstruction = "Pour to ${cumThree.roundToInt()}g in ${thirdSec}s",
                waitInstruction = "Wait until the bed dries",
            ) + timedPour(
                name = "Pour 4",
                action = StepAction.POUR,
                amount = pourFour,
                cumulative = total,
                total = total,
                startSec = 180,
                endSec = 210,
                pourSec = fourthSec,
                pourInstruction = "Pour to ${total.roundToInt()}g in ${fourthSec}s",
                waitInstruction = "Wait until 3:30",
            ) + listOf(
                BrewStep(
                    order = 0,
                    name = "Drawdown",
                    action = StepAction.WAIT,
                    waterPercentage = 0.0,
                    waterAmount = 0.0,
                    cumulativeWater = total,
                    startTimeSec = 210,
                    endTimeSec = 225,
                    instruction = "Finish around 3:30–3:45",
                )
            )
            ).withOrders()
    }
}
