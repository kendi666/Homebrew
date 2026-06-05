package com.brewmaster.domain.model

import kotlin.math.roundToInt

/**
 * A burr grinder with an approximate linear mapping between particle size
 * (microns) and its own adjustment unit (clicks/steps).
 *
 * The values are rough community references for the filter range, NOT exact
 * factory specs — they exist to turn an abstract "Medium-Fine" into something
 * actionable like "≈ 22 clicks". Always dial to taste from there.
 */
data class Grinder(
    val id: String,
    val name: String,
    val unitLabel: String,         // "clicks" or "steps"
    val micronsPerUnit: Double,    // size change per click/step
    val zeroOffsetMicrons: Double, // nominal microns at unit 0
    val maxUnits: Int              // for clamping the displayed value
) {
    /** Approximate setting (clicks/steps from zero) for a target micron size. */
    fun unitsForMicrons(microns: Int): Int {
        val raw = (microns - zeroOffsetMicrons) / micronsPerUnit
        return raw.roundToInt().coerceIn(0, maxUnits)
    }

    /** Friendly hint such as "≈ 22 clicks". */
    fun settingLabel(microns: Int): String =
        "≈ ${unitsForMicrons(microns)} $unitLabel"
}
