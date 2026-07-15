package com.brewmaster.domain.model

/**
 * Ranked technique suggestion from grind setting + coffee profile.
 * Not a hard lock — user still picks; this only ranks candidates.
 */
data class BrewSuggestion(
    val technique: BrewTechnique,
    val score: Int,
    val reason: String,
    val inferredGrind: GrindSize,
    val tempMin: Int,
    val tempMax: Int
)
