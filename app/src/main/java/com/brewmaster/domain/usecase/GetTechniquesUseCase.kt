package com.brewmaster.domain.usecase

import com.brewmaster.domain.model.BrewTechnique
import com.brewmaster.domain.model.GrindSize
import javax.inject.Inject

class GetTechniquesUseCase @Inject constructor() {

    operator fun invoke(): List<BrewTechnique> = TECHNIQUES

    companion object {
        private val TECHNIQUES = listOf(
            BrewTechnique(
                id = "hoffmann",
                name = "James Hoffmann",
                author = "James Hoffmann",
                focus = "Balance & Reproducibility",
                description = "Bloom, main pour, final pour with stir and swirl to flatten bed",
                defaultRatio = 16.67,
                defaultGrind = GrindSize.MEDIUM_FINE,
                defaultTempMin = 95,
                defaultTempMax = 100,
                totalBrewTimeSec = 210
            ),
            BrewTechnique(
                id = "kasuya_46",
                name = "Kasuya 4:6",
                author = "Tetsu Kasuya",
                focus = "Sweetness & Acidity Control",
                description = "Five equal pours: first 40% controls taste, remaining 60% controls strength",
                defaultRatio = 15.0,
                defaultGrind = GrindSize.COARSE,
                defaultTempMin = 90,
                defaultTempMax = 92,
                totalBrewTimeSec = 210
            ),
            BrewTechnique(
                id = "neo_kasuya_10",
                name = "Neo Kasuya 10",
                author = "Tetsu Kasuya",
                focus = "High Sweetness & Syrupy Body",
                description = "Ten rapid 30g-style pulses with very coarse grind and high temperature for syrupy sweetness",
                defaultRatio = 15.0,
                defaultGrind = GrindSize.EXTRA_COARSE,
                defaultTempMin = 95,
                defaultTempMax = 96,
                totalBrewTimeSec = 210
            ),
            BrewTechnique(
                id = "matt_winton_5_pour",
                name = "Winton 5 Pour",
                author = "Matt Winton",
                focus = "Clarity & Juicy Body",
                description = "Five coarse pulse pours; championship versions use hotter early pours and cooler late pours",
                defaultRatio = 15.0,
                defaultGrind = GrindSize.MEDIUM_COARSE,
                defaultTempMin = 88,
                defaultTempMax = 93,
                totalBrewTimeSec = 210
            ),
            BrewTechnique(
                id = "rao",
                name = "Scott Rao",
                author = "Scott Rao",
                focus = "High Extraction & Evenness",
                description = "Bloom with excavation, single main pour, spin to flatten bed",
                defaultRatio = 16.67,
                defaultGrind = GrindSize.MEDIUM_FINE,
                defaultTempMin = 96,
                defaultTempMax = 97,
                totalBrewTimeSec = 180
            ),
            BrewTechnique(
                id = "osmotic",
                name = "Osmotic Flow",
                author = "Japanese Method",
                focus = "Clarity & Sweetness",
                description = "Thin center stream expanding outward, dome-shaped bed acts as natural filter",
                defaultRatio = 15.0,
                defaultGrind = GrindSize.MEDIUM_FINE,
                defaultTempMin = 88,
                defaultTempMax = 92,
                totalBrewTimeSec = 210
            ),
            BrewTechnique(
                id = "hario_switch_hybrid",
                name = "Hario Switch Hybrid",
                author = "Hario Switch",
                focus = "Body & Clarity",
                description = "Hybrid Switch recipe: hot percolation phase, optional cooler immersion finish for sweetness",
                defaultRatio = 16.67,
                defaultGrind = GrindSize.MEDIUM,
                defaultTempMin = 90,
                defaultTempMax = 95,
                totalBrewTimeSec = 225
            ),
            BrewTechnique(
                id = "single_cup",
                name = "Single Cup",
                author = "Classic",
                focus = "Simple & Reliable",
                description = "Basic bloom followed by slow continuous circular pour",
                defaultRatio = 16.67,
                defaultGrind = GrindSize.MEDIUM_FINE,
                defaultTempMin = 95,
                defaultTempMax = 100,
                totalBrewTimeSec = 180
            ),
            BrewTechnique(
                id = "bypass",
                name = "Bypass",
                author = "Barista trick",
                focus = "Strong but Smooth",
                description = "Brew concentrate (~1:11), then dilute with hot water in the server — softens sharp acidity",
                defaultRatio = 14.0,
                defaultGrind = GrindSize.MEDIUM,
                defaultTempMin = 93,
                defaultTempMax = 95,
                totalBrewTimeSec = 180
            ),
            BrewTechnique(
                id = "custom",
                name = "Custom",
                author = "You",
                focus = "Your own rules",
                description = "Design your own brewing technique with custom temperature, time, and steps",
                defaultRatio = 15.0,
                defaultGrind = GrindSize.MEDIUM,
                defaultTempMin = 90,
                defaultTempMax = 94,
                totalBrewTimeSec = 180
            )
        )
    }
}
