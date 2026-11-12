package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * 2025 World Brewers Cup — George Jinyang Peng.
 * Written for 15 g / 210 g. The Melodrip pour has no published duration;
 * 40 s is what makes the timed steps land on the recipe's 2:15.
 */
class GeorgePengEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val total = totalHotWater.roundToInt().toDouble().coerceAtLeast(1.0)
        val bloom = (total * 30.0 / 210.0).roundToInt().toDouble().coerceAtMost(total)
        val second = (total * 90.0 / 210.0).roundToInt().toDouble()
        val cumSecond = (bloom + second).coerceAtMost(total)
        val third = (total - cumSecond).coerceAtLeast(0.0)

        return listOf(
            BrewStep(
                order = 1,
                name = "Bloom",
                action = StepAction.BLOOM,
                waterPercentage = bloom / total,
                waterAmount = bloom,
                cumulativeWater = bloom,
                startTimeSec = 0,
                endTimeSec = 6,
                instruction = "Pour ${bloom.roundToInt()}g in 6s, three gentle circles. Saturate the bed",
                tip = "96°C. Purge the kettle spout before you start"
            ),
            BrewStep(
                order = 2,
                name = "Wait",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = bloom,
                startTimeSec = 6,
                endTimeSec = 30,
                instruction = "Wait 24 seconds"
            ),
            BrewStep(
                order = 3,
                name = "Pour",
                action = StepAction.POUR,
                waterPercentage = (cumSecond - bloom).coerceAtLeast(0.0) / total,
                waterAmount = (cumSecond - bloom).coerceAtLeast(0.0),
                cumulativeWater = cumSecond,
                startTimeSec = 30,
                endTimeSec = 40,
                instruction = "Circle pour to ${cumSecond.roundToInt()}g in 10 seconds",
                tip = "Still 96°C"
            ),
            BrewStep(
                order = 4,
                name = "Wait",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = cumSecond,
                startTimeSec = 40,
                endTimeSec = 70,
                instruction = "Wait 30 seconds"
            ),
            BrewStep(
                order = 5,
                name = "Melodrip",
                action = StepAction.POUR,
                waterPercentage = third / total,
                waterAmount = third,
                cumulativeWater = total,
                startTimeSec = 70,
                endTimeSec = 110,
                instruction = "Pour to ${total.roundToInt()}g at 80°C through a Melodrip",
                tip = "Cooler water keeps the finish sweet"
            ),
            BrewStep(
                order = 6,
                name = "Drawdown",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = total,
                startTimeSec = 110,
                endTimeSec = 135,
                instruction = "Wait 25 seconds. Brew finishes at 2:15",
                tip = "Drink near 50°C. About a minute in the cup after drawdown"
            )
        )
    }
}
