package com.brewmaster.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomStepConfig(
    val action: StepAction,
    val durationSec: Int,
    val waterPercentage: Double, // e.g. 0.20 for 20%
    val instruction: String = "",
    val tip: String? = null
)
