package com.brewmaster.domain.model

/**
 * Roast level shifts the ideal brew temperature.
 *
 * Lighter roasts are denser and less soluble, so they extract better with HOTTER
 * water. Darker roasts are brittle and very soluble, so they get bitter quickly
 * and want COOLER water. The offset (in °C) is applied on top of the technique /
 * process temperature in the brew engine.
 */
enum class RoastLevel(val label: String, val tempOffset: Int) {
    LIGHT("Light", 2),
    MEDIUM_LIGHT("Medium-Light", 1),
    MEDIUM("Medium", 0),
    MEDIUM_DARK("Medium-Dark", -2),
    DARK("Dark", -4);

    companion object {
        /** Tolerant parser for the free-text roast labels stored on a bean. */
        fun fromLabel(raw: String?): RoastLevel {
            val key = raw?.trim()?.lowercase().orEmpty()
            return when {
                key.isEmpty() -> MEDIUM
                key.contains("light") && key.contains("medium") -> MEDIUM_LIGHT
                key.contains("dark") && key.contains("medium") -> MEDIUM_DARK
                key.contains("light") -> LIGHT
                key.contains("dark") -> DARK
                else -> MEDIUM
            }
        }
    }
}
