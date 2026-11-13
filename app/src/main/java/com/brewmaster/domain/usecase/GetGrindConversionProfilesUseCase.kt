package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.GrindConversionProfile
import javax.inject.Inject

/**
 * Dial ↔ dial conversion via one shared bridge unit, M.
 * M is not a measured particle size.
 *
 *   M = 75 * FM120 + 50
 *   Sculptor = 0.5 * FM120 + 2      →  M = 150 * SC − 250
 *   K-Ultra  = M/100 − 0.5          →  M = 100 * D_K + 50
 *   C40      = 30 * clicks          →  28 clicks = 840
 *   ZP6      = pour-over center 4.3 → 900, French press center 5.5 → 1000
 *   Guerrero = (M − 150) / 11       →  M = 11 * D_G + 150
 *
 * C3 / C5 / JX-S share the same M (intercept 50) with typical brew-window fits.
 * K-Ultra 20 µm/click and ZP6 22 µm/click are burr travel, not this M.
 */
class GetGrindConversionProfilesUseCase @Inject constructor() {

    operator fun invoke(): List<GrindConversionProfile> = PROFILES

    companion object {
        private val PROFILES = listOf(
            GrindConversionProfile(
                id = "fm",
                name = "FM 120",
                brand = "FM",
                slope = 75.0,
                intercept = 50.0,
                dialMin = 1.0,
                dialMax = 12.0,
                dialStep = 0.1,
                note = "Espresso ~4.0 (350 µm), filter ~8.0 (650 µm), Gesha ~11–12",
                calibrated = true
            ),
            GrindConversionProfile(
                id = "1zpresso_k_ultra",
                name = "K-Ultra",
                brand = "1Zpresso",
                slope = 100.0,
                intercept = 50.0,
                dialMin = 0.0,
                dialMax = 11.0,
                dialStep = 0.1,
                note = "D = M/100 − 0.5 (not factory 20 µm/click gap)",
                calibrated = true
            ),
            GrindConversionProfile(
                id = "comandante_c40",
                name = "Comandante C40",
                brand = "Comandante",
                slope = 30.0,
                intercept = 0.0,
                dialMin = 0.0,
                dialMax = 40.0,
                dialStep = 1.0,
                unitLabel = "clicks",
                note = "Bridge: 30 per click. 28 clicks = 840. Not a measured particle size.",
                calibrated = true
            ),
            GrindConversionProfile(
                id = "1zpresso_zp6",
                name = "ZP6 Special",
                brand = "1Zpresso",
                slope = 250.0 / 3.0,
                intercept = 1625.0 / 3.0,
                dialMin = 0.0,
                dialMax = 9.0,
                dialStep = 0.1,
                note = "Factory chart: pour-over ~4.0–4.6, French press ~5.0–6.0, lined up with K-Ultra 8.5 and 9.5. Not the 22 µm burr step.",
                calibrated = false
            ),
            GrindConversionProfile(
                id = "guerrero_gr64",
                name = "GR64 / 64 Pro",
                brand = "Guerrero",
                slope = 11.0,
                intercept = 150.0,
                dialMin = 0.0,
                dialMax = 90.0,
                dialStep = 1.0,
                note = "M = 11×dial + 150; espresso start ~18",
                calibrated = true
            ),
            GrindConversionProfile(
                id = "timemore_sculptor",
                name = "Sculptor",
                brand = "Timemore",
                slope = 150.0,
                intercept = -250.0,
                dialMin = 2.0,
                dialMax = 18.0,
                dialStep = 0.5,
                unitLabel = "dial",
                note = "D = 0.5×FM + 2 (filter ~6–8)",
                calibrated = true
            ),
            GrindConversionProfile(
                id = "timemore_c3",
                name = "Chestnut C3",
                brand = "Timemore",
                slope = 37.5,
                intercept = 50.0,
                dialMin = 6.0,
                dialMax = 30.0,
                dialStep = 1.0,
                unitLabel = "clicks",
                note = "Fitted: espresso ~8, filter ~16, Gesha ~24 (same M as FM 120)",
                calibrated = false
            ),
            GrindConversionProfile(
                id = "timemore_c5",
                name = "Chestnut C5 / C5 Pro",
                brand = "Timemore",
                slope = 31.0,
                intercept = 50.0,
                dialMin = 3.0,
                dialMax = 48.0,
                dialStep = 1.0,
                unitLabel = "clicks",
                note = "Fitted C5 Pro 31 µm/click; espresso ~10, filter ~19 (not C5 ESP)",
                calibrated = false
            ),
            GrindConversionProfile(
                id = "1zpresso_jx_s",
                name = "JX-S / JX",
                brand = "1Zpresso",
                slope = 12.5,
                intercept = 50.0,
                dialMin = 0.0,
                dialMax = 90.0,
                dialStep = 1.0,
                unitLabel = "clicks",
                note = "Fitted: 30 clicks/turn; espresso ~24, filter ~48 (1.6 turns)",
                calibrated = false
            )
        )
    }
}
