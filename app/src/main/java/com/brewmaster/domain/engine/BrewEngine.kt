package com.brewmaster.domain.engine

import com.brewmaster.domain.model.BrewStep

interface BrewEngine {
    fun generateSteps(
        coffeeWeight: Double,
        totalHotWater: Double
    ): List<BrewStep>
}
