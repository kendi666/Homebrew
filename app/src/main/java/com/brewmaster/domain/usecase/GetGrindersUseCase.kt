package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.Grinder
import com.brewmaster.domain.model.GrindSize
import javax.inject.Inject

/**
 * Built-in grinder profiles used to translate a recommended grind size (microns)
 * into the user's own grinder units. Numbers are approximate filter-range
 * references, not exact specs.
 */
class GetGrindersUseCase @Inject constructor() {

    operator fun invoke(): List<Grinder> = GRINDERS

    companion object {
        private val GRINDERS = listOf(
            Grinder(
                id = "comandante_c40_mk4",
                name = "Comandante C40 MK4",
                unitLabel = "clicks",       // internal dial; ~30 µm/click stock axle
                micronsPerUnit = 30.0,       // Red Clix axle ≈ 15 µm/click for espresso
                zeroOffsetMicrons = 0.0,
                maxUnits = 50
            ),
            Grinder(
                id = "1zpresso_k_ultra",
                name = "1Zpresso K-Ultra",
                unitLabel = "clicks",
                micronsPerUnit = 20.0,   // 100 clicks / rotation, 20 µm per click
                zeroOffsetMicrons = 0.0,
                maxUnits = 110,
                grindSizeSettings = mapOf(
                    GrindSize.EXTRA_FINE to "3.0-4.0 (espresso range)",
                    GrindSize.FINE to "5.0-6.0 (moka / fine AeroPress)",
                    GrindSize.MEDIUM_FINE to "7.0-8.0 (finer V60 / Switch)",
                    GrindSize.MEDIUM to "8.0-8.6 (80-86 clicks)",
                    GrindSize.MEDIUM_COARSE to "8.6-9.2 (86-92 clicks)",
                    GrindSize.COARSE to "9.0-9.5 (90-95 clicks)",
                    GrindSize.EXTRA_COARSE to "9.5-10.0 (95-100 clicks)"
                ),
                techniqueSettings = mapOf(
                    "hoffmann" to "7.5-8.2 (75-82 clicks), medium-fine V60",
                    "kasuya_46" to "8.5-9.0 (85-90 clicks), coarse 4:6",
                    "neo_kasuya_10" to "9.0-9.5 (90-95 clicks), very coarse 10 pours",
                    "matt_winton_5_pour" to "8.6-9.2 (86-92 clicks), coarse 5 pours",
                    "hario_switch_hybrid" to "7.8-8.5 (78-85 clicks), Switch hybrid",
                    "hybrid_immersion" to "7.8-8.5 (78-85 clicks), Switch hybrid",
                    "single_cup" to "7.5-8.5 (75-85 clicks), standard V60"
                ),
                sourceNote = "K-Ultra: 100 clicks/rotation, 20µm/click. V60 sources converge around 8.0-9.2; Kasuya-style pulse recipes are coarser."
            ),
            Grinder(
                id = "1zpresso_k_pro_max_plus",
                name = "1Zpresso K-Pro / K-Max / K-Plus",
                unitLabel = "clicks",
                micronsPerUnit = 22.0,   // 90 clicks / rotation, 22 µm per click
                zeroOffsetMicrons = 0.0,
                maxUnits = 100,
                grindSizeSettings = mapOf(
                    GrindSize.EXTRA_FINE to "3.0-4.0 (espresso range)",
                    GrindSize.FINE to "5.0-6.0 (moka / fine AeroPress)",
                    GrindSize.MEDIUM_FINE to "7.0-8.0 (finer V60 / Switch)",
                    GrindSize.MEDIUM to "8.0-8.5 (80-85 clicks)",
                    GrindSize.MEDIUM_COARSE to "8.5-9.0 (85-90 clicks)",
                    GrindSize.COARSE to "9.0-9.5 (90-95 clicks)",
                    GrindSize.EXTRA_COARSE to "9.5-10.0 (95-100 clicks)"
                ),
                techniqueSettings = mapOf(
                    "hoffmann" to "7.5-8.2 (75-82 clicks), medium-fine V60",
                    "kasuya_46" to "8.5-9.0 (85-90 clicks), coarse 4:6",
                    "neo_kasuya_10" to "9.0-9.5 (90-95 clicks), very coarse 10 pours",
                    "matt_winton_5_pour" to "8.5-9.0 (85-90 clicks), coarse 5 pours",
                    "hario_switch_hybrid" to "7.8-8.5 (78-85 clicks), Switch hybrid",
                    "hybrid_immersion" to "7.8-8.5 (78-85 clicks), Switch hybrid",
                    "single_cup" to "7.5-8.5 (75-85 clicks), standard V60"
                ),
                sourceNote = "K-Pro/K-Max/K-Plus: 90 clicks/rotation, 22µm/click. The attached chart uses 10 clicks between whole dial numbers."
            ),
            Grinder(
                id = "1zpresso_x_ultra",
                name = "1Zpresso X-Ultra",
                unitLabel = "clicks",
                micronsPerUnit = 12.5,   // external dial, true filter + espresso
                zeroOffsetMicrons = 0.0,
                maxUnits = 120
            ),
            Grinder(
                id = "1zpresso_zp6",
                name = "1Zpresso ZP6",
                unitLabel = "clicks",    // external ring (Red Dot); FILTER-ONLY clarity, ~240–1050 µm
                micronsPerUnit = 22.0,   // 0.022 mm/click, 90 clicks/rotation
                zeroOffsetMicrons = 0.0,
                maxUnits = 90
            ),
            Grinder(
                id = "mhw3bomber_blade_r3",
                name = "MHW-3 Bomber Blade R3",
                unitLabel = "clicks",    // external dial, 180 settings, upgradeable 48mm burr
                micronsPerUnit = 16.0,   // 0.016 mm per grid
                zeroOffsetMicrons = 0.0,
                maxUnits = 120
            ),
            Grinder(
                id = "timemore_c3",
                name = "Timemore C3",
                unitLabel = "clicks",
                micronsPerUnit = 33.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 36
            ),
            Grinder(
                id = "baratza_encore",
                name = "Baratza Encore",
                unitLabel = "steps",
                micronsPerUnit = 38.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 40
            ),
            Grinder(
                id = "kingrinder_k6",
                name = "Kingrinder K6",
                unitLabel = "clicks",
                micronsPerUnit = 16.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 90
            )
        )
    }
}
