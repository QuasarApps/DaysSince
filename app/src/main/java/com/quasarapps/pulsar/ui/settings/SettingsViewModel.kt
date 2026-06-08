package com.quasarapps.pulsar.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quasarapps.pulsar.data.Settings
import com.quasarapps.pulsar.data.SettingsRepository
import com.quasarapps.pulsar.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Exposes app [Settings] as state and forwards edits to [SettingsRepository]. Read at the app root
 * so the theme choice can drive [com.quasarapps.pulsar.ui.theme.PulsarTheme] and the detail screen's
 * units row.
 */
class SettingsViewModel internal constructor(
    app: Application,
    private val repo: SettingsRepository,
) : AndroidViewModel(app) {

    constructor(app: Application) : this(app, SettingsRepository(app))

    val settings: StateFlow<Settings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Settings())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repo.setThemeMode(mode) }
    }

    fun setShowUnits(show: Boolean) {
        viewModelScope.launch { repo.setShowUnits(show) }
    }

    fun setBackupEnabled(enabled: Boolean) {
        viewModelScope.launch { repo.setBackupEnabled(enabled) }
    }
}
