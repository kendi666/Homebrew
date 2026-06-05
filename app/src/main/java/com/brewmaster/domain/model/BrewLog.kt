package com.brewmaster.domain.model

data class BrewLog(
    val id: Int = 0,
    val beanName: String,
    val techniqueId: String,
    val processId: Int,
    val grindSize: GrindSize,
    val ratio: Double,
    val coffeeWeight: Double,
    val isIce: Boolean,
    val tempUsed: Int? = null,
    val rating: Int,            // 1..5
    val notes: String? = null,
    val createdAt: Long
)
