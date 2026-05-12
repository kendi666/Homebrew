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
}
