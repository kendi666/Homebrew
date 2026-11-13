package com.brewmaster.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BrewCalculation(
    val technique: BrewTechnique,
    val coffeeWeight: Double,
    val totalVolume: Double,
    val hotWaterVolume: Double,
    val iceWeight: Double,
    val tempMin: Int,
    val tempMax: Int,
    val grindSize: GrindSize,
    val steps: List<BrewStep>,
    val totalBrewTimeSec: Int,
    val processNote: String? = null,
    val targetProfile: TargetProfile = TargetProfile.BALANCED,
    val beanName: String? = null,
    /** Filled by the dashboard so a journal entry can say what was brewed. */
    val processId: Int = 0,
    val grinderName: String? = null,
    val grinderSetting: String? = null
)
