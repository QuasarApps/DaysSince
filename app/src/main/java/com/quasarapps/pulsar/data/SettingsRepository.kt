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

/** How the Home list is ordered. */
enum class SortOrder {
    /** Most recently added first (createdAt descending) — the default and the original behaviour. */
    RECENTLY_ADDED,
    /** Most elapsed days first — i.e. the earliest start instant first (clock-independent). */
    MOST_DAYS,
    /** By title, case-insensitive A→Z. */
    ALPHABETICAL;

    /** Returns [milestones] ordered per this option. Pure (doesn't read the current clock). */
    fun sort(milestones: List<Milestone>): List<Milestone> = when (this) {
        RECENTLY_ADDED -> milestones.sortedByDescending { it.createdAt }
        MOST_DAYS -> milestones.sortedWith(compareBy({ it.date }, { it.time }))
        ALPHABETICAL -> milestones.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
    }

    companion object {
        /** Parse a stored token back to an order, defaulting to [RECENTLY_ADDED] for unknown/missing. */
        fun fromStorage(value: String?): SortOrder = entries.firstOrNull { it.name == value } ?: RECENTLY_ADDED
    }
}

/** User-facing app preferences, independent of milestone data. */
data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Whether the detail screen shows the live hours/minutes/seconds breakdown under the day count. */
    val showUnits: Boolean = true,
    /** How the Home list is ordered. */
    val sortOrder: SortOrder = SortOrder.RECENTLY_ADDED,
    /**
     * Whether milestone data may be included in the device backup (default true). When false,
     * [com.quasarapps.pulsar.backup.MilestoneBackupAgent] skips the backup so data stays on-device.
     */
    val backupEnabled: Boolean = true,
)

// Settings store, separate from the milestones store. A DataStore name may only be instantiated once
// per process, so this name is intentionally distinct from MilestonesRepository's "pulsar_store".
private val Context.settingsDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "pulsar_settings")

/**
 * Single source of truth for app [Settings], backed by Preferences DataStore. A test store can be
 * injected via the internal constructor for isolated unit tests (mirrors [MilestonesRepository]).
 */
class SettingsRepository internal constructor(
    private val dataStore: DataStore<Preferences>,
) {

    constructor(context: Context) : this(context.applicationContext.settingsDataStore)

    val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            themeMode = ThemeMode.fromStorage(prefs[KEY_THEME_MODE]),
            showUnits = prefs[KEY_SHOW_UNITS] ?: true,
            backupEnabled = prefs[KEY_BACKUP_ENABLED] ?: true,
            sortOrder = SortOrder.fromStorage(prefs[KEY_SORT_ORDER]),
        )
    }.distinctUntilChanged()

    suspend fun snapshot(): Settings = settings.first()

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setShowUnits(show: Boolean) {
        dataStore.edit { it[KEY_SHOW_UNITS] = show }
    }

    suspend fun setBackupEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_BACKUP_ENABLED] = enabled }
    }

    suspend fun setSortOrder(order: SortOrder) {
        dataStore.edit { it[KEY_SORT_ORDER] = order.name }
    }

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_SHOW_UNITS = booleanPreferencesKey("show_units")
        private val KEY_BACKUP_ENABLED = booleanPreferencesKey("backup_enabled")
        private val KEY_SORT_ORDER = stringPreferencesKey("sort_order")
    }
}
