package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

class KasuyaEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val pourSize = (totalHotWater / 5.0).roundToInt().toDouble()
        val pourPercentage = pourSize / totalHotWater

        return listOf(
            BrewStep(
                order = 1,
                name = "Pour 1",
                action = StepAction.PULSE,
                waterPercentage = pourPercentage,
                waterAmount = pourSize,
                cumulativeWater = pourSize,
                startTimeSec = 0,
                endTimeSec = 45,
                instruction = "Pour ${pourSize.roundToInt()}g - controls acidity",
                tip = "More water = more acidity. Less = more sweetness."
            ),
            BrewStep(
                order = 2,
                name = "Pour 2",
                action = StepAction.PULSE,
                waterPercentage = pourPercentage,
                waterAmount = pourSize,
                cumulativeWater = (pourSize * 2).roundToInt().toDouble(),
                startTimeSec = 45,
                endTimeSec = 90,
                instruction = "Pour ${pourSize.roundToInt()}g - controls sweetness"
            ),
            BrewStep(
                order = 3,
                name = "Pour 3",
                action = StepAction.PULSE,
                waterPercentage = pourPercentage,
                waterAmount = pourSize,
                cumulativeWater = (pourSize * 3).roundToInt().toDouble(),
                startTimeSec = 90,
                endTimeSec = 120,
                instruction = "Pour ${pourSize.roundToInt()}g - strength adjustment",
                tip = "More pours with less water = lighter body"
            ),
            BrewStep(
                order = 4,
                name = "Pour 4",
                action = StepAction.PULSE,
                waterPercentage = pourPercentage,
                waterAmount = pourSize,
                cumulativeWater = (pourSize * 4).roundToInt().toDouble(),
                startTimeSec = 120,
                endTimeSec = 150,
                instruction = "Pour ${pourSize.roundToInt()}g - strength adjustment"
            ),
            BrewStep(
                order = 5,
                name = "Pour 5",
                action = StepAction.PULSE,
                waterPercentage = pourPercentage,
                waterAmount = pourSize,
                cumulativeWater = totalHotWater.roundToInt().toDouble(),
                startTimeSec = 150,
                endTimeSec = 210,
                instruction = "Pour ${pourSize.roundToInt()}g - final pour"
            )
        )
    }
}
