package com.brewmaster.domain.model

data class BrewStep(
    val order: Int,
    val name: String,
    val action: StepAction,
    val waterPercentage: Double,
    val waterAmount: Double,
    val cumulativeWater: Double,
    val startTimeSec: Int,
    val endTimeSec: Int,
    val instruction: String,
    val tip: String? = null
) {
    val durationSec: Int get() = endTimeSec - startTimeSec

    /** Water is going in. Waits and drawdowns stay false so the timer can show a gap. */
    val isPour: Boolean
        get() = waterAmount > 0.0 && action in setOf(
            StepAction.BLOOM,
            StepAction.POUR,
            StepAction.PULSE,
            StepAction.OSMOTIC,
            StepAction.IMMERSE
        )
}
