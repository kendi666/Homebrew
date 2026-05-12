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
                defaultTempMin = 92,
                defaultTempMax = 96,
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
                defaultTempMin = 92,
                defaultTempMax = 96,
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
                defaultTempMin = 92,
                defaultTempMax = 96,
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
                id = "single_cup",
                name = "Single Cup",
                author = "Classic",
                focus = "Simple & Reliable",
                description = "Basic bloom followed by slow continuous circular pour",
                defaultRatio = 16.67,
                defaultGrind = GrindSize.MEDIUM_FINE,
                defaultTempMin = 92,
                defaultTempMax = 96,
                totalBrewTimeSec = 180
            )
        )
    }
}
