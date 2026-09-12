package com.brewmaster.domain.usecase

import com.brewmaster.domain.engine.BypassEngine
import com.brewmaster.domain.engine.HoffmannEngine
import com.brewmaster.domain.engine.HybridImmersionEngine
import com.brewmaster.domain.engine.KasuyaEngine
import com.brewmaster.domain.engine.MattWintonFivePourEngine
import com.brewmaster.domain.engine.NeoKasuyaTenPourEngine
import com.brewmaster.domain.engine.OsmoticEngine
import com.brewmaster.domain.engine.RaoEngine
import com.brewmaster.domain.engine.SingleCupEngine
import com.brewmaster.domain.model.BrewCalculation
import com.brewmaster.domain.model.BrewMode
import com.brewmaster.domain.model.BrewTechnique
import com.brewmaster.domain.model.CoffeeBean
import com.brewmaster.domain.model.CoffeeProcess
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.domain.model.RoastLevel
import com.brewmaster.domain.model.TargetProfile
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class CalculateBrewUseCase @Inject constructor() {

    operator fun invoke(
        technique: BrewTechnique,
        coffeeWeight: Double,
        ratio: Double,
        grindSize: GrindSize,
        brewMode: BrewMode,
        iceWeight: Double = 0.0,
        process: CoffeeProcess? = null,
        targetProfile: TargetProfile = TargetProfile.BALANCED,
        bean: CoffeeBean? = null,
        customSteps: List<com.brewmaster.domain.model.CustomStepConfig>? = null,
        customTempMin: Int? = null,
        customTempMax: Int? = null
    ): BrewCalculation {
        // Apply target profile ratio offset
        val adjustedRatio = (ratio + targetProfile.ratioOffset).coerceAtLeast(10.0)

        val totalVolume = coffeeWeight * adjustedRatio
        // B1: keep enough hot water for bloom (up to 3×) + a remaining pour, including
        // Bypass's 8× concentrate floor, so engines never emit negative pour grams.
        val minHotWater = coffeeWeight * MIN_HOT_MULTIPLIER
        val cappedIce = if (brewMode == BrewMode.ICE) {
            iceWeight.coerceAtLeast(0.0).coerceAtMost((totalVolume - minHotWater).coerceAtLeast(0.0))
        } else {
            0.0
        }
        val hotWaterVolume = if (brewMode == BrewMode.ICE) {
            (totalVolume - cappedIce).coerceAtLeast(minHotWater)
        } else {
            totalVolume
        }

        val engine = when (technique.id) {
            "hoffmann" -> HoffmannEngine()
            "kasuya_46" -> KasuyaEngine()
            "neo_kasuya_10" -> NeoKasuyaTenPourEngine()
            "matt_winton_5_pour" -> MattWintonFivePourEngine()
            "rao" -> RaoEngine()
            "osmotic" -> OsmoticEngine()
            "hario_switch_hybrid" -> HybridImmersionEngine()
            "hybrid_immersion" -> HybridImmersionEngine()
            "single_cup" -> SingleCupEngine()
            "bypass" -> BypassEngine()
            "custom" -> customSteps?.let { com.brewmaster.domain.engine.CustomEngine(it) } ?: SingleCupEngine()
            else -> SingleCupEngine()
        }

        val steps = if (coffeeWeight <= 0.0 || hotWaterVolume <= 0.0) {
            emptyList()
        } else {
            engine.generateSteps(coffeeWeight, hotWaterVolume)
        }

        // Temperature resolution: technique defaults -> process override -> profile offset
        var tempMin: Int
        var tempMax: Int
        val processNote: String?

        val baseTempMin = customTempMin ?: technique.defaultTempMin
        val baseTempMax = customTempMax ?: technique.defaultTempMax

        if (process != null) {
            val finalTempMin = max(baseTempMin, process.tempMin)
            val finalTempMax = min(baseTempMax, process.tempMax)

            if (finalTempMin > finalTempMax) {
                tempMin = process.tempMin
                tempMax = process.tempMax
            } else {
                tempMin = finalTempMin
                tempMax = finalTempMax
            }
            processNote = process.extractionNote
        } else {
            tempMin = baseTempMin
            tempMax = baseTempMax
            processNote = null
        }

        // Apply target profile temp offset
        tempMin = (tempMin + targetProfile.tempOffset).coerceIn(80, 100)
        tempMax = (tempMax + targetProfile.tempOffset).coerceIn(80, 100)
        if (tempMin > tempMax) tempMin = tempMax

        // Apply roast-level offset: lighter roasts brew hotter, darker roasts cooler.
        bean?.let {
            val roastOffset = RoastLevel.fromLabel(it.roastLevel).tempOffset
            tempMin = (tempMin + roastOffset).coerceIn(80, 100)
            tempMax = (tempMax + roastOffset).coerceIn(80, 100)
            if (tempMin > tempMax) tempMin = tempMax
        }

        val totalBrewTimeSec = if (technique.id == "custom" && customSteps != null) {
            customSteps.sumOf { it.durationSec }
        } else {
            technique.totalBrewTimeSec
        }

        return BrewCalculation(
            technique = technique,
            coffeeWeight = coffeeWeight,
            totalVolume = totalVolume,
            hotWaterVolume = hotWaterVolume,
            iceWeight = cappedIce,
            tempMin = tempMin,
            tempMax = tempMax,
            grindSize = grindSize,
            steps = steps,
            totalBrewTimeSec = totalBrewTimeSec,
            processNote = processNote,
            targetProfile = targetProfile,
            beanName = bean?.name
        )
    }

    companion object {
        private const val MIN_HOT_MULTIPLIER = 8.0
    }

}
