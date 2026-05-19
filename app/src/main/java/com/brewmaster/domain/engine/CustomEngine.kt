package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.CustomStepConfig

class CustomEngine(
    private val customSteps: List<CustomStepConfig>
) : BrewEngine {
    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        var currentTime = 0
        var currentWater = 0.0
        val steps = mutableListOf<BrewStep>()

        customSteps.forEachIndexed { index, config ->
            val stepWaterAmount = totalHotWater * config.waterPercentage
            currentWater += stepWaterAmount
            val endTime = currentTime + config.durationSec

            steps.add(
                BrewStep(
                    order = index + 1,
                    name = config.action.label,
                    action = config.action,
                    waterPercentage = config.waterPercentage,
                    waterAmount = stepWaterAmount,
                    cumulativeWater = currentWater,
                    startTimeSec = currentTime,
                    endTimeSec = endTime,
                    instruction = config.instruction.ifBlank { "${config.action.label} ${formatWeight(stepWaterAmount)}g" },
                    tip = config.tip
                )
            )
            currentTime = endTime
        }

        return steps
    }

    private fun formatWeight(weight: Double): String =
        if (weight == weight.toLong().toDouble()) {
            weight.toLong().toString()
        } else {
            "%.1f".format(weight)
        }
}
