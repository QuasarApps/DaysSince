package com.quasarapps.dayssince.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quasarapps.dayssince.SelectedStartDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

private val Context.milestonesDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "dayssince_store")

/**
 * Single source of truth for milestones and widget bindings, backed by Preferences DataStore.
 *
 * Milestones are stored as a JSON array string (org.json — no extra dependency / annotation
 * processor); widget bindings as a JSON object string mapping appWidgetId -> milestoneId.
 */
class MilestonesRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dataStore get() = appContext.milestonesDataStore

    val milestones: Flow<List<Milestone>> = dataStore.data.map { prefs ->
        decodeList(prefs[KEY_MILESTONES]).sortedByDescending { it.createdAt }
    }

    suspend fun snapshot(): List<Milestone> = milestones.first()

    suspend fun getById(id: String): Milestone? = snapshot().firstOrNull { it.id == id }

    suspend fun upsert(milestone: Milestone) {
        dataStore.edit { prefs ->
            val current = decodeList(prefs[KEY_MILESTONES]).toMutableList()
            val idx = current.indexOfFirst { it.id == milestone.id }
            if (idx >= 0) current[idx] = milestone else current.add(milestone)
            prefs[KEY_MILESTONES] = encodeList(current)
        }
    }

    suspend fun delete(id: String) {
        dataStore.edit { prefs ->
            val remaining = decodeList(prefs[KEY_MILESTONES]).filterNot { it.id == id }
            prefs[KEY_MILESTONES] = encodeList(remaining)
            val bindings = decodeBindings(prefs[KEY_BINDINGS]).filterValues { it != id }
            prefs[KEY_BINDINGS] = encodeBindings(bindings)
        }
    }

    // ---- widget bindings (appWidgetId -> milestoneId) ----

    suspend fun bindWidget(appWidgetId: Int, milestoneId: String) {
        dataStore.edit { prefs ->
            val bindings = decodeBindings(prefs[KEY_BINDINGS]).toMutableMap()
            bindings[appWidgetId] = milestoneId
            prefs[KEY_BINDINGS] = encodeBindings(bindings)
        }
    }

    suspend fun unbindWidget(appWidgetId: Int) {
        dataStore.edit { prefs ->
            val bindings = decodeBindings(prefs[KEY_BINDINGS]).toMutableMap()
            bindings.remove(appWidgetId)
            prefs[KEY_BINDINGS] = encodeBindings(bindings)
        }
    }

    suspend fun milestoneForWidget(appWidgetId: Int): Milestone? {
        val prefs = dataStore.data.first()
        val id = decodeBindings(prefs[KEY_BINDINGS])[appWidgetId] ?: return null
        return decodeList(prefs[KEY_MILESTONES]).firstOrNull { it.id == id }
    }

    /**
     * One-time migration: if there are no milestones yet but the old single-counter prefs exist,
     * seed a first milestone from them so upgrading users keep their counter.
     */
    suspend fun migrateLegacyIfNeeded() {
        dataStore.edit { prefs ->
            if (prefs[KEY_MIGRATED] == "1") return@edit
            if (decodeList(prefs[KEY_MILESTONES]).isEmpty()) {
                val legacy = SelectedStartDateTime.load(appContext)
                val seeded = Milestone(
                    id = Milestone.newId(),
                    title = "Milestone",
                    date = legacy.date,
                    time = legacy.time,
                    accent = 0,
                )
                prefs[KEY_MILESTONES] = encodeList(listOf(seeded))
            }
            prefs[KEY_MIGRATED] = "1"
        }
    }

    companion object {
        private val KEY_MILESTONES = stringPreferencesKey("milestones_json")
        private val KEY_BINDINGS = stringPreferencesKey("widget_bindings_json")
        private val KEY_MIGRATED = stringPreferencesKey("legacy_migrated")

        private fun encodeList(list: List<Milestone>): String {
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

        private fun decodeList(json: String?): List<Milestone> {
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

        private fun encodeBindings(map: Map<Int, String>): String {
            val o = JSONObject()
            map.forEach { (k, v) -> o.put(k.toString(), v) }
            return o.toString()
        }

        private fun decodeBindings(json: String?): Map<Int, String> {
            if (json.isNullOrBlank()) return emptyMap()
            return runCatching {
                val o = JSONObject(json)
                buildMap {
                    val keys = o.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val id = o.optString(key)
                        val widgetId = key.toIntOrNull()
                        if (widgetId != null && id.isNotBlank()) put(widgetId, id)
                    }
                }
            }.getOrDefault(emptyMap())
        }
    }
}
