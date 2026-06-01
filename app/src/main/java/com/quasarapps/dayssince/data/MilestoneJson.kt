package com.quasarapps.dayssince.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

/**
 * JSON (de)serialization for the milestone list. Extracted from the repository so the
 * encode/decode logic can be unit-tested without DataStore.
 *
 * Decoding is defensive: malformed or partial data yields an empty list / sensible defaults
 * rather than throwing.
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
                    put("accent", m.accent)
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
                    accent = o.optInt("accent", 0),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                )
            }
        }.getOrDefault(emptyList())
    }
}
