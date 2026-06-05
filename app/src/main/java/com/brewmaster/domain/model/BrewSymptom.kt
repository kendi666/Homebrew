package com.brewmaster.domain.model

/**
 * Taste-based troubleshooting knowledge for manual pour-over.
 *
 * Each symptom maps to a likely cause and a short list of concrete adjustments,
 * ordered from the highest-impact lever (usually grind) downward. The golden rule:
 * change ONE variable at a time so you can tell what actually helped.
 */
enum class BrewSymptom(
    val label: String,
    val emoji: String,
    val diagnosis: String,
    val fixes: List<String>
) {
    SOUR(
        label = "Too sour / sharp",
        emoji = "🍋",
        diagnosis = "Under-extracted — not enough flavour was pulled from the grounds.",
        fixes = listOf(
            "Grind finer (1–2 clicks) to slow the flow and extract more",
            "Use hotter water (+2–3 °C)",
            "Slow your pour / extend total brew time slightly",
            "Make sure the bloom wets every ground evenly"
        )
    ),
    BITTER(
        label = "Bitter / harsh",
        emoji = "🥃",
        diagnosis = "Over-extracted — too much was pulled, including bitter compounds.",
        fixes = listOf(
            "Grind coarser (1–2 clicks) to speed up the flow",
            "Use cooler water (−2–3 °C)",
            "Shorten the brew: fewer/faster pours",
            "Reduce agitation (gentler pours, less stirring)"
        )
    ),
    WEAK(
        label = "Weak / watery",
        emoji = "💧",
        diagnosis = "Low strength — too little coffee dissolved relative to water.",
        fixes = listOf(
            "Lower the ratio (more coffee), e.g. 1:15 instead of 1:16.7",
            "Grind a touch finer",
            "Check for fast drawdown / channeling (uneven bed)"
        )
    ),
    TOO_STRONG(
        label = "Too strong / heavy",
        emoji = "🪨",
        diagnosis = "High strength — too concentrated for your taste.",
        fixes = listOf(
            "Raise the ratio (less coffee), e.g. 1:17–1:18",
            "Grind a touch coarser",
            "Add a little more water at the end"
        )
    ),
    ASTRINGENT(
        label = "Drying / astringent",
        emoji = "😖",
        diagnosis = "Uneven over-extraction, often from too many fines or harsh agitation.",
        fixes = listOf(
            "Grind slightly coarser to reduce fines",
            "Lower water temperature",
            "Pour more gently; avoid digging into the bed",
            "Rinse the paper filter thoroughly before brewing"
        )
    ),
    FLAT(
        label = "Flat / muted aroma",
        emoji = "😐",
        diagnosis = "Often stale beans or water that's too cool to develop aromatics.",
        fixes = listOf(
            "Use fresher beans — sweet spot is ~7–21 days off roast",
            "Raise water temperature",
            "Give a longer or more vigorous bloom to release CO₂"
        )
    ),
    UNEVEN(
        label = "Bitter AND sour together",
        emoji = "🌀",
        diagnosis = "Uneven extraction / channeling — some grounds over-, some under-extracted.",
        fixes = listOf(
            "Even, consistent bloom — saturate all grounds",
            "Flatten the bed with a swirl or gentle stir (Rao spin)",
            "Pour in steady concentric circles, keep the level even",
            "Consider a more uniform grinder if it persists"
        )
    ),
    SLOW(
        label = "Drains too slow / clogged",
        emoji = "🐢",
        diagnosis = "Flow is choked — usually too fine, too many fines, or over-agitation.",
        fixes = listOf(
            "Grind coarser",
            "Reduce agitation and pour height",
            "Avoid stirring the bed mid-brew"
        )
    ),
    FAST(
        label = "Drains too fast / thin",
        emoji = "🏃",
        diagnosis = "Water runs through before it can extract — usually too coarse or uneven.",
        fixes = listOf(
            "Grind finer",
            "Split into more pour stages to keep the bed saturated",
            "Make sure the bloom soaks everything evenly"
        )
    )
}
