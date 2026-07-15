package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.BrewMode
import com.brewmaster.domain.model.BrewSuggestion
import com.brewmaster.domain.model.BrewTechnique
import com.brewmaster.domain.model.CoffeeProcess
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.domain.model.Grinder
import com.brewmaster.domain.model.TargetProfile
import javax.inject.Inject
import kotlin.math.abs

/**
 * Invert the usual flow: grinder clicks (+ profile) → ranked techniques.
 * Scores are heuristic — present top matches, never auto-force a method.
 */
class SuggestBrewUseCase @Inject constructor(
    private val getTechniquesUseCase: GetTechniquesUseCase
) {

    operator fun invoke(
        grinder: Grinder?,
        clicks: Int?,
        grindSize: GrindSize,
        process: CoffeeProcess?,
        targetProfile: TargetProfile,
        brewMode: BrewMode
    ): List<BrewSuggestion> {
        val inferred = when {
            grinder != null && clicks != null -> grinder.nearestGrindSize(clicks)
            else -> grindSize
        }

        return getTechniquesUseCase()
            .filter { it.id != "custom" }
            .map { technique ->
                score(technique, inferred, process, targetProfile, brewMode)
            }
            .sortedByDescending { it.score }
            .take(3)
    }

    private fun score(
        technique: BrewTechnique,
        grind: GrindSize,
        process: CoffeeProcess?,
        target: TargetProfile,
        brewMode: BrewMode
    ): BrewSuggestion {
        var score = 100
        val reasons = mutableListOf<String>()

        val grindGap = abs(grind.ordinal - technique.defaultGrind.ordinal)
        score -= grindGap * 22
        if (grindGap == 0) {
            reasons += "${grind.label} matches ${technique.name}"
        } else if (grindGap == 1) {
            reasons += "near ${technique.defaultGrind.label} zone"
        } else {
            reasons += "grind farther from ${technique.defaultGrind.label}"
        }

        process?.let { p ->
            val processGap = abs(grind.ordinal - p.grindRecommendation.ordinal)
            if (processGap <= 1) {
                score += 12
                reasons += "fits ${p.processName}"
            }
            // Prefer techniques whose temp window overlaps process window
            val overlap = technique.defaultTempMax >= p.tempMin && technique.defaultTempMin <= p.tempMax
            if (overlap) score += 8 else score -= 6
        }

        when (target) {
            TargetProfile.SWEET -> when (technique.id) {
                "kasuya_46", "neo_kasuya_10", "bypass" -> score += 15
                "matt_winton_5_pour" -> score += 8
            }
            TargetProfile.MORE_ACIDITY -> when (technique.id) {
                "hoffmann", "rao", "single_cup" -> score += 12
                "kasuya_46" -> score += 6
            }
            TargetProfile.MORE_BODY -> when (technique.id) {
                "bypass", "neo_kasuya_10", "hario_switch_hybrid", "hybrid_immersion" -> score += 15
                "matt_winton_5_pour" -> score += 8
            }
            TargetProfile.BALANCED -> when (technique.id) {
                "hoffmann", "single_cup", "rao" -> score += 10
            }
        }

        if (brewMode == BrewMode.ICE) {
            when (technique.id) {
                "hoffmann", "single_cup", "bypass" -> score += 10
                "neo_kasuya_10" -> score -= 8 // very coarse + ice is awkward
            }
            reasons += "ice-friendly"
        }

        var tempMin = technique.defaultTempMin
        var tempMax = technique.defaultTempMax
        process?.let {
            tempMin = maxOf(tempMin, it.tempMin)
            tempMax = minOf(tempMax, it.tempMax)
            if (tempMin > tempMax) {
                tempMin = it.tempMin
                tempMax = it.tempMax
            }
        }
        tempMin = (tempMin + target.tempOffset).coerceIn(80, 100)
        tempMax = (tempMax + target.tempOffset).coerceIn(80, 100)
        if (tempMin > tempMax) tempMin = tempMax

        return BrewSuggestion(
            technique = technique,
            score = score,
            reason = reasons.distinct().take(2).joinToString(" · "),
            inferredGrind = grind,
            tempMin = tempMin,
            tempMax = tempMax
        )
    }
}
