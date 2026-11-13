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
        // Pulls a little less: acidity comes through when the cup is not pushed to full extraction.
        description = "Brighter cup: a touch coarser and cooler, a bit more water",
        tempOffset = -1,
        ratioOffset = 0.5,
        grindShift = 1
    ),
    MORE_BODY(
        label = "More Body",
        icon = "fitness_center",
        // Body follows strength and fines: less water and a finer grind, same temperature.
        description = "Heavier mouthfeel: stronger ratio and a finer grind",
        tempOffset = 0,
        ratioOffset = -1.0,
        grindShift = -1
    )
}
