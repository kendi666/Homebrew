package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/**
 * Tetsu Kasuya V60 Neo 10-pour method.
 *
 * Uses very coarse grounds and high temperature. The repeated small pulses build
 * sweetness and syrupy body without pushing bitterness too far.
 */
class NeoKasuyaTenPourEngine : BrewEngine {

    override fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep> {
        val total = totalHotWater.roundToInt().toDouble()
        val pourSize = (total / POUR_COUNT).roundToInt().toDouble()
        val steps = mutableListOf<BrewStep>()
        var currentTime = 0

        repeat(POUR_COUNT) { index ->
            val order = index + 1
            val isFinalPour = order == POUR_COUNT
            val cumulative = if (isFinalPour) total else (pourSize * order).roundToInt().toDouble()
            val amount = if (isFinalPour) (total - pourSize * (POUR_COUNT - 1)).coerceAtLeast(0.0) else pourSize
            val duration = if (order == 1) 30 else 15
            val endTime = currentTime + duration

            steps.add(
                BrewStep(
                    order = order,
                    name = if (order == 1) "Bloom Pour" else if (isFinalPour) "Final Pour 10" else "Pour $order",
                    action = if (order == 1) StepAction.BLOOM else StepAction.PULSE,
                    waterPercentage = amount / total,
                    waterAmount = amount,
                    cumulativeWater = cumulative,
                    startTimeSec = currentTime,
                    endTimeSec = endTime,
                    instruction = if (order == 1) {
                        "Rapidly pour ${amount.roundToInt()}g for bloom, then wait 30s"
                    } else {
                        "Rapidly add ${amount.roundToInt()}g, up to ${cumulative.roundToInt()}g total"
                    },
                    tip = when (order) {
                        1 -> "Use very coarse grind and 95-96°C water"
                        POUR_COUNT -> "Let drawdown finish around 3:00-3:30, then swirl the server"
                        else -> null
                    }
                )
            )
            currentTime = endTime
        }

        steps.add(
            BrewStep(
                order = POUR_COUNT + 1,
                name = "Drawdown",
                action = StepAction.WAIT,
                waterPercentage = 0.0,
                waterAmount = 0.0,
                cumulativeWater = total,
                startTimeSec = currentTime,
                endTimeSec = 210,
                instruction = "Allow the water to fully draw down",
                tip = "Swirl the server gently before serving to integrate the layers"
            )
        )

        return steps
    }

    private companion object {
        const val POUR_COUNT = 10
    }
}
