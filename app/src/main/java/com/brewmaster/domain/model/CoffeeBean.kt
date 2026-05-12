package com.brewmaster.domain.model

data class CoffeeBean(
    val id: Int = 0,
    val name: String,
    val origin: String,
    val processId: Int,
    val processName: String,
    val roastLevel: String,  // "Light", "Medium", "Dark"
    val notes: String? = null,
    val restingDays: Int? = null
)
