package com.brewmaster.domain.model

enum class GrindSize(val label: String, val microns: Int) {
    EXTRA_FINE("Extra-Fine", 200),
    FINE("Fine", 400),
    MEDIUM_FINE("Medium-Fine", 600),
    MEDIUM("Medium", 800),
    MEDIUM_COARSE("Medium-Coarse", 1000),
    COARSE("Coarse", 1200),
    EXTRA_COARSE("Extra-Coarse", 1400);

    override fun toString(): String = label

    companion object {
        /** B2: never throw on a stale / unknown DB or share-link string. */
        fun fromStored(value: String): GrindSize =
            runCatching { valueOf(value) }.getOrDefault(MEDIUM)
    }
}
