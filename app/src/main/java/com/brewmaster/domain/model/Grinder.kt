package com.brewmaster.domain.model

import kotlin.math.roundToInt

/**
 * A burr grinder with an approximate linear mapping between particle size
 * (microns) and its own adjustment unit (clicks/steps).
 *
 * [filterDialMin]/[filterDialMax] limit the pour-over picker (espresso finer
 * settings stay out of the wheel). Dial labels use [dialStep]; multiply by
 * [dialClickFactor] to get absolute clicks from zero (K-Ultra: 8.2 → 82).
 */
data class Grinder(
    val id: String,
    val name: String,
    val unitLabel: String,         // "clicks" or "steps"
    val micronsPerUnit: Double,    // size change per click/step
    val zeroOffsetMicrons: Double, // nominal microns at unit 0
    val maxUnits: Int,             // for clamping the displayed value
    val grindSizeSettings: Map<GrindSize, String> = emptyMap(),
    val techniqueSettings: Map<String, String> = emptyMap(),
    val sourceNote: String? = null,
    val filterDialMin: Double? = null,
    val filterDialMax: Double? = null,
    val dialStep: Double = 0.1,
    val dialClickFactor: Double = 1.0
) {
    fun unitsForMicrons(microns: Int): Int {
        val raw = (microns - zeroOffsetMicrons) / micronsPerUnit
        return raw.roundToInt().coerceIn(0, maxUnits)
    }

    fun micronsForUnits(units: Int): Double =
        zeroOffsetMicrons + units * micronsPerUnit

    fun nearestGrindSize(units: Int): GrindSize {
        val microns = micronsForUnits(units.coerceIn(0, maxUnits))
        return GrindSize.entries.minBy { kotlin.math.abs(it.microns - microns) }
    }

    fun dialToClicks(dial: Double): Int =
        (dial * dialClickFactor).roundToInt().coerceIn(0, maxUnits)

    fun clicksToDial(clicks: Int): Double =
        if (dialClickFactor == 0.0) clicks.toDouble() else clicks / dialClickFactor

    /** Pour-over dial labels only (e.g. K-Ultra 6.5…10.0). */
    fun filterDialLabels(): List<String> {
        val min = filterDialMin
            ?: clicksToDial(unitsForMicrons(GrindSize.MEDIUM_FINE.microns))
        val max = filterDialMax
            ?: clicksToDial(unitsForMicrons(GrindSize.EXTRA_COARSE.microns))
        val step = dialStep.coerceAtLeast(0.05)
        val out = mutableListOf<String>()
        var v = min
        // Guard against float drift
        while (v <= max + step * 0.01) {
            out += formatDial(v)
            v += step
        }
        return out.distinct()
    }

    fun formatDial(dial: Double): String {
        return if (dialStep >= 1.0) {
            dial.roundToInt().toString()
        } else {
            String.format(java.util.Locale.US, "%.1f", dial)
        }
    }

    fun settingLabel(grindSize: GrindSize, techniqueId: String? = null): String {
        val techniqueSetting = techniqueId?.let { techniqueSettings[it] }
        val sizeSetting = grindSizeSettings[grindSize]
        return sizeSetting ?: techniqueSetting ?: "≈ ${unitsForMicrons(grindSize.microns)} $unitLabel"
    }

    fun techniqueSettingLabel(techniqueId: String?): String? {
        return techniqueId?.let { techniqueSettings[it] }
    }
}
