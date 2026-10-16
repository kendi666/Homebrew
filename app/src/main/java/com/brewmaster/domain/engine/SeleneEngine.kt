package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * Selene — card recipe written for 10 g / 190 g (1:19), 91–92°C, medium.
 * Pours scale with the kettle so another dose keeps the same split:
 * 30 + 80 + 40 + 40 on 190 g. Finish sits at the card's 3:45 mark.
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

        return listOf(
            BrewStep(
                order = 1,
                name = "Bloom",
                action = StepAction.BLOOM,
                waterPercentage = bloom / total,
                waterAmount = bloom,
                cumulativeWater = bloom,
                startTimeSec = 0,
                endTimeSec = 60,
                instruction = "Pour ${bloom.roundToInt()}g and bloom for 1 minute",
                tip = "Steady pour, not too slow and not too fast"
            ),
            BrewStep(
                order = 2,
                name = "Pour 2",
                action = StepAction.POUR,
                waterPercentage = (cumTwo - bloom).coerceAtLeast(0.0) / total,
                waterAmount = (cumTwo - bloom).coerceAtLeast(0.0),
                cumulativeWater = cumTwo,
                startTimeSec = 60,
                endTimeSec = 130,
                instruction = "Pour to ${cumTwo.roundToInt()}g, then wait until the bed dries"
            ),
            BrewStep(
                order = 3,
                name = "Pour 3",
                action = StepAction.POUR,
                waterPercentage = (cumThree - cumTwo).coerceAtLeast(0.0) / total,
                waterAmount = (cumThree - cumTwo).coerceAtLeast(0.0),
                cumulativeWater = cumThree,
                startTimeSec = 130,
                endTimeSec = 180,
                instruction = "Pour to ${cumThree.roundToInt()}g, then wait until the bed dries"
            ),
            BrewStep(
                order = 4,
                name = "Pour 4",
                action = StepAction.POUR,
                waterPercentage = pourFour / total,
                waterAmount = pourFour,
                cumulativeWater = total,
                startTimeSec = 180,
                endTimeSec = 210,
                instruction = "Pour to ${total.roundToInt()}g once the bed has dried"
            ),
            BrewStep(
                order = 5,
                name = "Drawdown",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = total,
                startTimeSec = 210,
                endTimeSec = 225,
                instruction = "Finish around 3:30–3:45"
            )
        )
    }
}
