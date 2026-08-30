package com.brewmaster.domain.model

import kotlin.math.round

/**
 * Linear dial ↔ micron map used by the Grind Converter.
 *
 * micron ≈ slope * dial + intercept
 *
 * FM 120 is the master scale. Always dial to taste.
 */
data class GrindConversionProfile(
    val id: String,
    val name: String,
    val brand: String,
    /** Microns gained per dial unit. */
    val slope: Double,
    /** Micron estimate at dial 0. */
    val intercept: Double,
    val dialMin: Double,
    val dialMax: Double,
    val dialStep: Double,
    val unitLabel: String = "dial",
    val note: String? = null,
    /** True when slope/intercept come from a published/shared chart. */
    val calibrated: Boolean = false
) {
    fun dialToMicron(dial: Double): Double = slope * dial + intercept

    fun micronToDial(micron: Double): Double {
        if (slope == 0.0) return dialMin
        val raw = (micron - intercept) / slope
        return roundToStep(raw.coerceIn(dialMin, dialMax), dialStep)
    }

    fun formatDial(dial: Double): String {
        return if (dialStep >= 1.0) {
            dial.toInt().toString()
        } else if (dialStep >= 0.5) {
            String.format(java.util.Locale.US, "%.1f", dial)
        } else {
            String.format(java.util.Locale.US, "%.1f", dial)
        }
    }

    fun dialOptions(): List<Double> {
        val out = mutableListOf<Double>()
        var v = dialMin
        val step = dialStep.coerceAtLeast(0.05)
        while (v <= dialMax + step * 0.01) {
            out += roundToStep(v, step)
            v += step
        }
        return out.distinct()
    }

    companion object {
        fun roundToStep(value: Double, step: Double): Double {
            if (step <= 0.0) return value
            return round(value / step) * step
        }
    }
}

data class GrindConversionResult(
    val source: GrindConversionProfile,
    val sourceDial: Double,
    val estimatedMicron: Double,
    val targets: List<ConvertedGrindDial>
)

data class ConvertedGrindDial(
    val profile: GrindConversionProfile,
    val dial: Double,
    val micronCheck: Double
)
