package com.quasarapps.dayssince

import android.app.Application
import com.quasarapps.dayssince.data.MilestonesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Process-wide init that has to happen before any UI / widget callback can run.
 *
 * Two things, both off the main thread:
 *
 * 1. Warm the legacy [Prefs] SharedPreferences cache so the first widget `onUpdate`
 *    after a cold start doesn't pay a synchronous disk read on the main thread
 *    (PERF-2 short-term fix).
 *
 * 2. Run the one-time single-counter → multi-counter migration eagerly. Previously
 *    this only happened when [MilestonesViewModel] was first instantiated, which
 *    meant a user who placed a widget before ever opening the app would see an
 *    empty milestone list in the widget config picker. Application.onCreate fires
 *    on every cold start regardless of entry point, so the widget flow gets the
 *    migrated data too.
 */
class DaysSinceApplication : Application() {

    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initScope.launch {
            // Touch the prefs file off the main thread so the first widget callback
            // doesn't pay the disk read. `getSharedPreferences` itself kicks off the
            // background load inside SharedPreferencesImpl, but calling `getAll()`
            // forces that load to complete here instead of on whichever thread first
            // reads a value later — usually the main thread inside `onUpdate`.
            Prefs.get(this@DaysSinceApplication).all
        }
        initScope.launch { MilestonesRepository(this@DaysSinceApplication).migrateLegacyIfNeeded() }
    }
}
