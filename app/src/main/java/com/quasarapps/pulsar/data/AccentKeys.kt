package com.quasarapps.pulsar.data

/**
 * Canonical, stable identifiers for milestone accents — the persistence contract for
 * [Milestone.accent].
 *
 * The list position is the accent *index* used at runtime (and the bare integer that pre-key data
 * persisted); the string entry at that position is the stable *key* that new data persists, so the
 * visual palette can be reordered/extended later without recoloring milestones.
 *
 * This lives in the data layer (not `ui.theme`) on purpose: key↔index resolution is a persistence
 * concern, and keeping it here means [MilestoneJson] doesn't have to depend on the UI. The order
 * here must stay in sync with `ui.theme.MilestoneAccents`; `AccentTest` enforces that.
 */
internal object AccentKeys {

    val ordered: List<String> = listOf(
        "magenta",
        "violet",
        "indigo",
        "nebula",
        "aurora",
        "solar",
        "ember",
        "deep",
    )

    /** Index used for missing/unknown accents — the default accent ("magenta"). */
    const val DEFAULT_INDEX = 0

    /**
     * Stable key for the accent at [index]. Out-of-range indices fall back to the default accent
     * key, mirroring `ui.theme.accentOrDefault`, so an encode of a corrupt index still yields a
     * real key.
     */
    fun keyForIndex(index: Int): String = ordered.getOrElse(index) { ordered[DEFAULT_INDEX] }

    /** Index for a stable [key]; a null/blank/unknown key falls back to [DEFAULT_INDEX]. */
    fun indexForKey(key: String?): Int {
        val index = ordered.indexOf(key)
        return if (index >= 0) index else DEFAULT_INDEX
    }
}
