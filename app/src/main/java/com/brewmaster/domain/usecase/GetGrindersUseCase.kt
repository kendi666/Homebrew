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
                unitLabel = "clicks",
                micronsPerUnit = 30.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 50,
                filterDialMin = 12.0,
                filterDialMax = 30.0,
                dialStep = 1.0,
                dialClickFactor = 1.0,
                sourceNote = "Filter pour-over typically ~12–30 clicks; finer is espresso/moka."
            ),
            Grinder(
                id = "1zpresso_k_ultra",
                name = "1Zpresso K-Ultra",
                unitLabel = "clicks",
                micronsPerUnit = 20.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 110,
                filterDialMin = 6.5,
                filterDialMax = 10.0,
                dialStep = 0.1,
                dialClickFactor = 10.0,
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
                    "single_cup" to "7.5-8.5 (75-85 clicks), standard V60",
                    "bypass" to "8.0-8.6 (80-86 clicks), medium concentrate"
                ),
                sourceNote = "Filter dial 6.5–10.0 only. Below 6.5 is espresso/moka — not shown here."
            ),
            Grinder(
                id = "1zpresso_k_pro_max_plus",
                name = "1Zpresso K-Pro / K-Max / K-Plus",
                unitLabel = "clicks",
                micronsPerUnit = 22.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 100,
                filterDialMin = 6.5,
                filterDialMax = 10.0,
                dialStep = 0.1,
                dialClickFactor = 10.0,
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
                    "single_cup" to "7.5-8.5 (75-85 clicks), standard V60",
                    "bypass" to "8.0-8.5 (80-85 clicks), medium concentrate"
                ),
                sourceNote = "Filter dial 6.5–10.0 only. Below 6.5 is espresso/moka — not shown here."
            ),
            Grinder(
                id = "1zpresso_x_ultra",
                name = "1Zpresso X-Ultra",
                unitLabel = "clicks",
                micronsPerUnit = 12.5,
                zeroOffsetMicrons = 0.0,
                maxUnits = 120,
                filterDialMin = 40.0,
                filterDialMax = 90.0,
                dialStep = 1.0,
                dialClickFactor = 1.0
            ),
            Grinder(
                id = "1zpresso_zp6",
                name = "1Zpresso ZP6",
                unitLabel = "clicks",
                micronsPerUnit = 22.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 90,
                filterDialMin = 30.0,
                filterDialMax = 70.0,
                dialStep = 1.0,
                dialClickFactor = 1.0,
                sourceNote = "ZP6 is filter-focused; dial shows typical V60 window."
            ),
            Grinder(
                id = "mhw3bomber_blade_r3",
                name = "MHW-3 Bomber Blade R3",
                unitLabel = "clicks",
                micronsPerUnit = 16.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 120,
                filterDialMin = 40.0,
                filterDialMax = 80.0,
                dialStep = 1.0,
                dialClickFactor = 1.0
            ),
            Grinder(
                id = "timemore_c3",
                name = "Timemore C3",
                unitLabel = "clicks",
                micronsPerUnit = 33.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 36,
                filterDialMin = 12.0,
                filterDialMax = 24.0,
                dialStep = 1.0,
                dialClickFactor = 1.0
            ),
            Grinder(
                id = "baratza_encore",
                name = "Baratza Encore",
                unitLabel = "steps",
                micronsPerUnit = 38.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 40,
                filterDialMin = 15.0,
                filterDialMax = 30.0,
                dialStep = 1.0,
                dialClickFactor = 1.0
            ),
            Grinder(
                id = "kingrinder_k6",
                name = "Kingrinder K6",
                unitLabel = "clicks",
                micronsPerUnit = 16.0,
                zeroOffsetMicrons = 0.0,
                maxUnits = 90,
                filterDialMin = 40.0,
                filterDialMax = 80.0,
                dialStep = 1.0,
                dialClickFactor = 1.0
            )
        )
    }
}
