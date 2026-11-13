package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.ConvertedGrindDial
import com.brewmaster.domain.model.GrindConversionProfile
import com.brewmaster.domain.model.GrindConversionResult
import javax.inject.Inject
import kotlin.math.round

/**
 * Convert a dial on grinder A into estimated dials on every other profile
 * via the shared micron bridge. Source dial is snapped to that grinder's step
 * before converting; µm is not pre-rounded (that would shift target dials).
 */
class ConvertGrindUseCase @Inject constructor(
    private val getProfiles: GetGrindConversionProfilesUseCase
) {

    operator fun invoke(
        sourceId: String,
        sourceDial: Double
    ): GrindConversionResult? {
        val profiles = getProfiles()
        val source = profiles.find { it.id == sourceId } ?: return null
        val dial = GrindConversionProfile.roundToStep(
            sourceDial.coerceIn(source.dialMin, source.dialMax),
            source.dialStep
        )
        val half = source.dialStep / 2
        val sourceClamped = sourceDial < source.dialMin - half || sourceDial > source.dialMax + half
        val micron = source.dialToMicron(dial)

        val targets = profiles
            .filter { it.id != source.id }
            .map { target ->
                convertedDial(target, micron)
            }
            .sortedWith(targetOrder)

        return GrindConversionResult(
            source = source,
            sourceDial = dial,
            estimatedMicron = round(micron),
            targets = targets,
            sourceClamped = sourceClamped
        )
    }

    /** Map a bridge micron onto every grinder. M is the converter unit, not a measured particle size. */
    fun fromMicron(micron: Double): List<ConvertedGrindDial> {
        val m = micron.coerceIn(0.0, 1600.0)
        return getProfiles()
            .map { target ->
                convertedDial(target, m)
            }
            .sortedWith(targetOrder)
    }

    /** Flags a dial that hit the grinder's limit, so 350 µm on a ZP6 is not shown as a plain 0.0. */
    private fun convertedDial(target: GrindConversionProfile, micron: Double): ConvertedGrindDial {
        val targetDial = target.micronToDial(micron)
        val raw = if (target.slope == 0.0) target.dialMin else (micron - target.intercept) / target.slope
        val half = target.dialStep / 2
        return ConvertedGrindDial(
            profile = target,
            dial = targetDial,
            micronCheck = round(target.dialToMicron(targetDial)),
            belowRange = raw < target.dialMin - half,
            aboveRange = raw > target.dialMax + half
        )
    }

    private val targetOrder =
        compareByDescending<ConvertedGrindDial> { it.profile.calibrated }
            .thenBy { it.profile.brand }
            .thenBy { it.profile.name }
}
