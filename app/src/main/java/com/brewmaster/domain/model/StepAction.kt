package com.brewmaster.domain.model

enum class StepAction(val label: String, val icon: String) {
    BLOOM("Bloom", "water_drop"),
    POUR("Pour", "waves"),
    STIR("Stir", "refresh"),
    SWIRL("Swirl", "sync"),
    WAIT("Wait", "hourglass_empty"),
    EXCAVATE("Excavate", "landscape"),
    PULSE("Pulse", "graphic_eq"),
    OSMOTIC("Osmotic", "opacity");

    override fun toString(): String = label
}
