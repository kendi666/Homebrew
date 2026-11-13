package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep
import com.brewmaster.domain.model.StepAction
import kotlin.math.roundToInt

/** Peng's bloom is 30 g in 6 s. A normal kettle pulse is a bit faster; Neo is a quick dump. */
internal const val BLOOM_G_PER_SEC = 5.0
internal const val KETTLE_G_PER_SEC = 8.0
internal const val RAPID_G_PER_SEC = 10.0

internal fun phaseClock(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)

/**
 * How long the kettle is actually pouring inside a phase.
 * The phase still ends on its clock mark. The seconds after this are a wait,
 * so the bed can bloom or draw down instead of taking water the whole time.
 */
internal fun kettlePourSec(grams: Double, windowSec: Int, gramsPerSec: Double): Int {
    if (windowSec <= 0 || grams <= 0.0 || gramsPerSec <= 0.0) return windowSec.coerceAtLeast(0)
    val paced = (grams / gramsPerSec).roundToInt().coerceAtLeast(4)
    val latest = (windowSec - 5).coerceAtLeast(4)
    return paced.coerceAtMost(latest).coerceAtMost(windowSec)
}

/** A pour block, plus a Wait step when the phase is longer than the pour. */
internal fun timedPour(
    name: String,
    action: StepAction,
    amount: Double,
    cumulative: Double,
    total: Double,
    startSec: Int,
    endSec: Int,
    pourSec: Int,
    pourInstruction: String,
    waitInstruction: String,
    pourTip: String? = null,
    waitAction: StepAction = StepAction.WAIT,
    waitName: String = "Wait",
): List<BrewStep> {
    val window = (endSec - startSec).coerceAtLeast(1)
    val actual = pourSec.coerceIn(1, window)
    val pourEnd = startSec + actual
    val pour = BrewStep(
        order = 0,
        name = name,
        action = action,
        waterPercentage = if (total > 0.0) amount / total else 0.0,
        waterAmount = amount,
        cumulativeWater = cumulative,
        startTimeSec = startSec,
        endTimeSec = pourEnd,
        instruction = pourInstruction,
        tip = pourTip,
    )
    if (pourEnd >= endSec) return listOf(pour)
    return listOf(
        pour,
        BrewStep(
            order = 0,
            name = waitName,
            action = waitAction,
            waterPercentage = 0.0,
            waterAmount = 0.0,
            cumulativeWater = cumulative,
            startTimeSec = pourEnd,
            endTimeSec = endSec,
            instruction = waitInstruction,
        )
    )
}

internal fun List<BrewStep>.withOrders(): List<BrewStep> =
    mapIndexed { index, step -> step.copy(order = index + 1) }
