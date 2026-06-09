package com.quasarapps.pulsar.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

/**
 * JSON (de)serialization for the milestone list, extracted from the repository so it can be unit-
 * tested without DataStore. Decoding is defensive: bad data yields an empty list / sensible defaults.
 */
internal object MilestoneJson {

    fun encode(list: List<Milestone>): String {
        val arr = JSONArray()
        list.forEach { m ->
            arr.put(
                JSONObject().apply {
                    put("id", m.id)
                    put("title", m.title)
                    put("date", m.date.toString())
                    put("time", m.time.toString())
                    // Persist a stable key, not the list index, so reordering accents later doesn't
                    // recolor existing milestones. (Downgrade caveat: an older build expecting an int
                    // reads the string and falls back to its default accent — "Dynamic" in the builds
                    // that had it. Downgrades aren't supported, so that's acceptable.)
                    put("accent", AccentKeys.keyForIndex(m.accent))
                    put("createdAt", m.createdAt)
                }
            )
        }
        return arr.toString()
    }

    fun decode(json: String?): List<Milestone> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val date = runCatching { LocalDate.parse(o.optString("date")) }
                    .getOrNull() ?: LocalDate.now()
                val time = runCatching { LocalTime.parse(o.optString("time")) }
                    .getOrNull() ?: LocalTime.MIDNIGHT
                Milestone(
                    id = o.optString("id").ifBlank { Milestone.newId() },
                    title = o.optString("title"),
                    date = date,
                    time = time,
                    // New data stores a stable string key; legacy data stored the raw index. Accept both.
                    accent = when (val raw = o.opt("accent")) {
                        is Number -> raw.toInt()
                        is String -> AccentKeys.indexForKey(raw)
                        else -> AccentKeys.DEFAULT_INDEX
                    },
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                )
            }
        }.getOrDefault(emptyList())
    }
}
