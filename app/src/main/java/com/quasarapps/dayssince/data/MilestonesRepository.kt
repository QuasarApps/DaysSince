package com.quasarapps.dayssince.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.milestonesDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "dayssince_store")

/** Per-appWidgetId configuration: which milestone, and whether to render transparently. */
data class WidgetBinding(
    val milestoneId: String,
    val transparent: Boolean = false,
)

/**
 * Single source of truth for milestones and widget bindings, backed by Preferences DataStore.
 *
 * Milestones are stored as a JSON array string (see [MilestoneJson]); widget bindings as a
 * JSON object string mapping appWidgetId -> {id, transparent}. Old data that stored only the
 * milestone id as a bare string is decoded back into a [WidgetBinding] with transparent=false.
 */
class MilestonesRepository internal constructor(
    private val dataStore: DataStore<Preferences>,
) {

    constructor(context: Context) : this(context.applicationContext.milestonesDataStore)

    val milestones: Flow<List<Milestone>> = dataStore.data.map { prefs ->
        MilestoneJson.decode(prefs[KEY_MILESTONES]).sortedByDescending { it.createdAt }
    }

    suspend fun snapshot(): List<Milestone> = milestones.first()

    suspend fun getById(id: String): Milestone? = snapshot().firstOrNull { it.id == id }

    suspend fun upsert(milestone: Milestone) {
        dataStore.edit { prefs ->
            val current = MilestoneJson.decode(prefs[KEY_MILESTONES]).toMutableList()
            val idx = current.indexOfFirst { it.id == milestone.id }
            if (idx >= 0) current[idx] = milestone else current.add(milestone)
            prefs[KEY_MILESTONES] = MilestoneJson.encode(current)
        }
    }

    suspend fun delete(id: String) {
        dataStore.edit { prefs ->
            val remaining = MilestoneJson.decode(prefs[KEY_MILESTONES]).filterNot { it.id == id }
            prefs[KEY_MILESTONES] = MilestoneJson.encode(remaining)
            val bindings = decodeBindings(prefs[KEY_BINDINGS]).filterValues { it.milestoneId != id }
            prefs[KEY_BINDINGS] = encodeBindings(bindings)
        }
    }

    // ---- widget bindings (appWidgetId -> WidgetBinding) ----

    suspend fun bindWidget(appWidgetId: Int, milestoneId: String, transparent: Boolean = false) {
        dataStore.edit { prefs ->
            val bindings = decodeBindings(prefs[KEY_BINDINGS]).toMutableMap()
            bindings[appWidgetId] = WidgetBinding(milestoneId, transparent)
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

    suspend fun bindingForWidget(appWidgetId: Int): WidgetBinding? {
        val prefs = dataStore.data.first()
        return decodeBindings(prefs[KEY_BINDINGS])[appWidgetId]
    }

    suspend fun milestoneForWidget(appWidgetId: Int): Milestone? {
        val binding = bindingForWidget(appWidgetId) ?: return null
        val prefs = dataStore.data.first()
        return MilestoneJson.decode(prefs[KEY_MILESTONES])
            .firstOrNull { it.id == binding.milestoneId }
    }

    companion object {
        private val KEY_MILESTONES = stringPreferencesKey("milestones_json")
        private val KEY_BINDINGS = stringPreferencesKey("widget_bindings_json")

        internal fun encodeBindings(map: Map<Int, WidgetBinding>): String {
            val o = JSONObject()
            map.forEach { (k, v) ->
                o.put(
                    k.toString(),
                    JSONObject().apply {
                        put("id", v.milestoneId)
                        put("transparent", v.transparent)
                    },
                )
            }
            return o.toString()
        }

        /**
         * Decodes the bindings JSON, accepting both the current `{id, transparent}` shape and
         * the legacy plain-string shape (just the milestone id), upgrading legacy values to a
         * [WidgetBinding] with transparent=false.
         */
        internal fun decodeBindings(json: String?): Map<Int, WidgetBinding> {
            if (json.isNullOrBlank()) return emptyMap()
            return runCatching {
                val o = JSONObject(json)
                buildMap {
                    val keys = o.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val widgetId = key.toIntOrNull() ?: continue
                        val value = o.opt(key)
                        val binding: WidgetBinding? = when (value) {
                            is String -> if (value.isNotBlank()) WidgetBinding(value, false) else null
                            is JSONObject -> {
                                val id = value.optString("id")
                                if (id.isNotBlank()) {
                                    WidgetBinding(id, value.optBoolean("transparent", false))
                                } else null
                            }
                            else -> null
                        }
                        if (binding != null) put(widgetId, binding)
                    }
                }
            }.getOrDefault(emptyMap())
        }
    }
}
