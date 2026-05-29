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
        initScope.launch { Prefs.get(this@DaysSinceApplication) }
        initScope.launch { MilestonesRepository(this@DaysSinceApplication).migrateLegacyIfNeeded() }
    }
}
