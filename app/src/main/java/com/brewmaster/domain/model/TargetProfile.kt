package com.brewmaster.domain.model

enum class TargetProfile(
    val label: String,
    val icon: String,
    val description: String,
    val tempOffset: Int,
    val ratioOffset: Double,
    val grindShift: Int  // -1 = finer, +1 = coarser, 0 = no change
) {
    BALANCED(
        label = "Balance & Clean",
        icon = "balance",
        description = "Even extraction with clean finish",
        tempOffset = 0,
        ratioOffset = 0.0,
        grindShift = 0
    ),
    SWEET(
        label = "Sweet",
        icon = "favorite",
        description = "Emphasize sweetness and body",
        tempOffset = 1,
        ratioOffset = -0.5,
        grindShift = -1
    ),
    MORE_ACIDITY(
        label = "More Acidity",
        icon = "bolt",
        description = "Brighter, more vibrant acidity",
        tempOffset = 2,
        ratioOffset = 0.5,
        grindShift = -1
    ),
    MORE_BODY(
        label = "More Body",
        icon = "fitness_center",
        description = "Heavier mouthfeel, richer texture",
        tempOffset = -2,
        ratioOffset = -1.0,
        grindShift = 1
    )
}
