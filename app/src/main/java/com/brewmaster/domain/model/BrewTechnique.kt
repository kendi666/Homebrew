package com.brewmaster.domain.model

data class BrewTechnique(
    val id: String,
    val name: String,
    val author: String,
    val focus: String,
    val description: String,
    val defaultRatio: Double,
    val defaultGrind: GrindSize,
    val defaultTempMin: Int,
    val defaultTempMax: Int,
    val totalBrewTimeSec: Int
)
