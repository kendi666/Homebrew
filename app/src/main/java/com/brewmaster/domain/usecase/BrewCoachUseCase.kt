package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.BrewSymptom
import javax.inject.Inject

data class BrewCoachAdvice(
    val diagnosis: String,
    val primaryFix: String,
    val secondaryFixes: List<String>,
    val timeNote: String?,
    val suggestedSymptom: BrewSymptom?
)

/**
 * Post-brew coach: brew time + taste → one primary lever (grind first).
 */
class BrewCoachUseCase @Inject constructor() {

    operator fun invoke(
        elapsedSec: Int,
        targetSec: Int,
        symptom: BrewSymptom?
    ): BrewCoachAdvice {
        val timeNote = when {
            elapsedSec < 120 -> "Brew finished fast (<2:00) — usually too coarse or channeling."
            elapsedSec > 240 -> "Brew ran long (>4:00) — usually too fine or clogged."
            elapsedSec < targetSec - 30 -> "A bit faster than target ${format(targetSec)}."
            elapsedSec > targetSec + 30 -> "A bit slower than target ${format(targetSec)}."
            else -> null
        }

        val fromTime: BrewSymptom? = when {
            elapsedSec < 120 -> BrewSymptom.FAST
            elapsedSec > 240 -> BrewSymptom.SLOW
            else -> null
        }

        val chosen = symptom ?: fromTime ?: BrewSymptom.FLAT
        // One variable: first fix is the primary lever
        val primary = chosen.fixes.firstOrNull()
            ?: "Change one variable next cup — usually grind ±1–2 clicks."

        return BrewCoachAdvice(
            diagnosis = chosen.diagnosis,
            primaryFix = primary,
            secondaryFixes = chosen.fixes.drop(1).take(2),
            timeNote = timeNote,
            suggestedSymptom = fromTime
        )
    }

    private fun format(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)
}
