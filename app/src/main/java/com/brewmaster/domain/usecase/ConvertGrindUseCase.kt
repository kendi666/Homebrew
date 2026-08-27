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
        val micron = source.dialToMicron(dial)

        val targets = profiles
            .filter { it.id != source.id }
            .map { target ->
                val targetDial = target.micronToDial(micron)
                ConvertedGrindDial(
                    profile = target,
                    dial = targetDial,
                    micronCheck = round(target.dialToMicron(targetDial))
                )
            }
            .sortedWith(
                compareByDescending<ConvertedGrindDial> { it.profile.calibrated }
                    .thenBy { it.profile.brand }
                    .thenBy { it.profile.name }
            )

        return GrindConversionResult(
            source = source,
            sourceDial = dial,
            estimatedMicron = round(micron),
            targets = targets
        )
    }
}
