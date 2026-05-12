package com.brewmaster.domain.model

data class PersonalRecipe(
    val id: Int = 0,
    val beanName: String,
    val techniqueId: String,
    val processId: Int,
    val grindSize: GrindSize,
    val ratio: Double,
    val coffeeWeight: Double,
    val isIce: Boolean,
    val iceWeight: Double? = null,
    val notes: String? = null,
    val createdAt: Long
)
