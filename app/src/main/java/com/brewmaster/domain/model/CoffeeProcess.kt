package com.brewmaster.domain.model

data class CoffeeProcess(
    val id: Int,
    val processName: String,
    val tempMin: Int,
    val tempMax: Int,
    val grindRecommendation: GrindSize,
    val extractionNote: String,
    val restingDays: Int = 15,
    val ratioMin: Double = 16.0
)
