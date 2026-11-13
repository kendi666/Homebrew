package com.brewmaster.domain.model

import kotlin.math.roundToInt

/**
 * A burr grinder. [micronsPerUnit] is burr travel per click, not a measured
 * particle size. Pour-over category uses [dialBands] when they are set.
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
    val dialClickFactor: Double = 1.0,
    /** Dial windows for the pour-over wheel. Empty means "use burr-gap µm". */
    val dialBands: List<DialBand> = emptyList()
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

    /**
     * Category for a dial reading. K-Ultra 8.5 stays Medium (band ends at 8.6);
     * burr-gap clicks are not used, or 8.0 would read as Extra-Coarse.
     */
    fun grindSizeForDial(dial: Double): GrindSize? {
        if (dialBands.isEmpty()) return null
        dialBands.forEachIndexed { index, band ->
            val inBand = if (index == dialBands.lastIndex) {
                dial >= band.from && dial <= band.to
            } else {
                dial >= band.from && dial < band.to
            }
            if (inBand) return band.size
        }
        return if (dial < dialBands.first().from) dialBands.first().size else dialBands.last().size
    }

    fun grindSizeForClicks(clicks: Int): GrindSize =
        grindSizeForDial(clicksToDial(clicks)) ?: nearestGrindSize(clicks)

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
        return sizeSetting
            ?: bandLabel(grindSize)
            ?: techniqueSetting
            ?: "≈ ${unitsForMicrons(grindSize.microns)} $unitLabel"
    }

    /**
     * The dial window for [grindSize] from [dialBands], so the label and the
     * category shown for a dial agree. Null when the size has no band.
     */
    private fun bandLabel(grindSize: GrindSize): String? {
        val index = dialBands.indexOfFirst { it.size == grindSize }
        if (index < 0) return null
        val band = dialBands[index]
        val isLast = index == dialBands.lastIndex
        if (dialStep >= 1.0) {
            val low = kotlin.math.ceil(band.from - 1e-9).toInt()
            // Bands end exclusive, except the last one.
            val high = if (isLast) kotlin.math.floor(band.to + 1e-9).toInt()
            else kotlin.math.ceil(band.to - 1e-9).toInt() - 1
            return when {
                band.from <= 0.0 -> "≤ $high $unitLabel"
                low >= high -> "$low $unitLabel"
                else -> "$low–$high $unitLabel"
            }
        }
        return "${formatDial(band.from)}–${formatDial(band.to)}"
    }

    fun techniqueSettingLabel(techniqueId: String?): String? {
        return techniqueId?.let { techniqueSettings[it] }
    }
}

/** Inclusive start, exclusive end, except the last band which includes [to]. */
data class DialBand(
    val from: Double,
    val to: Double,
    val size: GrindSize
)
