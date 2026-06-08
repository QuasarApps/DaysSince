package com.quasarapps.pulsar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** How the app chooses light vs. dark: follow the OS, or pin one. */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        /** Parse a stored token back to a mode, defaulting to [SYSTEM] for unknown/missing values. */
        fun fromStorage(value: String?): ThemeMode = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

/** User-facing app preferences, independent of milestone data. */
data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Whether the detail screen shows the live hours/minutes/seconds breakdown under the day count. */
    val showUnits: Boolean = true,
)

// A store dedicated to settings, separate from the milestones store, so the two evolve independently.
// NOTE: a given DataStore name may only be instantiated once per process; this name is intentionally
// distinct from MilestonesRepository's "pulsar_store".
private val Context.settingsDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "pulsar_settings")

/**
 * Single source of truth for app [Settings], backed by Preferences DataStore.
 *
 * A test store can be injected via the internal constructor so unit tests get an isolated, empty
 * store backed by a throwaway temp file (mirrors [MilestonesRepository]).
 */
class SettingsRepository internal constructor(
    private val dataStore: DataStore<Preferences>,
) {

    constructor(context: Context) : this(context.applicationContext.settingsDataStore)

    val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            themeMode = ThemeMode.fromStorage(prefs[KEY_THEME_MODE]),
            showUnits = prefs[KEY_SHOW_UNITS] ?: true,
        )
    }.distinctUntilChanged()

    suspend fun snapshot(): Settings = settings.first()

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setShowUnits(show: Boolean) {
        dataStore.edit { it[KEY_SHOW_UNITS] = show }
    }

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_SHOW_UNITS = booleanPreferencesKey("show_units")
    }
}
