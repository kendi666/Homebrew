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
        val marks = listOf(0 to 45, 45 to 90, 90 to 120, 120 to 150, 150 to 210)
        val notes = listOf(
            "controls acidity" to "More water = more acidity. Less = more sweetness.",
            "controls sweetness" to null,
            "strength adjustment" to "More pours with less water = lighter body",
            "strength adjustment" to null,
            "final pour" to null,
        )

        return marks.mapIndexed { index, (start, end) ->
            val isLast = index == marks.lastIndex
            val amount = if (isLast) {
                (totalHotWater - pourSize * (marks.size - 1)).coerceAtLeast(0.0)
            } else {
                pourSize
            }
            val cumulative = if (isLast) {
                totalHotWater.roundToInt().toDouble()
            } else {
                (pourSize * (index + 1)).roundToInt().toDouble()
            }
            val pourSec = kettlePourSec(amount, end - start, KETTLE_G_PER_SEC)
            val (note, tip) = notes[index]
            timedPour(
                name = "Pour ${index + 1}",
                action = StepAction.PULSE,
                amount = amount,
                cumulative = cumulative,
                total = totalHotWater,
                startSec = start,
                endSec = end,
                pourSec = pourSec,
                pourInstruction = "Pour ${amount.roundToInt()}g in ${pourSec}s — $note",
                waitInstruction = if (isLast) {
                    "Let it draw down until ${phaseClock(end)}"
                } else {
                    "Wait until ${phaseClock(end)}"
                },
                pourTip = tip,
            )
        }.flatten().withOrders()
    }
}
