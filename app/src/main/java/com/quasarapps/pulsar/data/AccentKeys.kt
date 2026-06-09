package com.quasarapps.pulsar.data

/**
 * Canonical, stable identifiers for milestone accents — the persistence contract for [Milestone.accent].
 * The list position is the runtime *index* (and the legacy persisted integer); the entry at that
 * position is the stable *key* new data persists, so the palette can be reordered later without
 * recoloring milestones. Lives in the data layer (not `ui.theme`) so [MilestoneJson] needn't depend on
 * the UI; the order must stay in sync with `ui.theme.MilestoneAccents` (enforced by `AccentTest`).
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

    /** Stable key for the accent at [index]; out-of-range falls back to the default key. */
    fun keyForIndex(index: Int): String = ordered.getOrElse(index) { ordered[DEFAULT_INDEX] }

    /** Index for a stable [key]; a null/blank/unknown key falls back to [DEFAULT_INDEX]. */
    fun indexForKey(key: String?): Int {
        val index = ordered.indexOf(key)
        return if (index >= 0) index else DEFAULT_INDEX
    }
}
