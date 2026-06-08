package com.quasarapps.pulsar.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Exercises the DataStore read/modify/write cycle of [SettingsRepository]. A test DataStore is
 * injected via the internal constructor so each test gets an isolated, empty store backed by a
 * throwaway temp file (mirrors [MilestonesRepositoryTest]).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = UnconfinedTestDispatcher(scheduler)

    // DataStore's collector is a long-running job; keep it in its own scope so it doesn't trip
    // runTest's "lingering coroutines" check. Shares the scheduler for one clock.
    private val dataStoreScope = CoroutineScope(dispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repo: SettingsRepository

    @Before
    fun setUp() {
        val file = File.createTempFile("pulsar_settings_test_", ".preferences_pb").also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) { file }
        repo = SettingsRepository(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun defaults_areSystemThemeWithUnitsShown() = runTest(dispatcher) {
        val settings = repo.snapshot()
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertTrue(settings.showUnits)
    }

    @Test
    fun setThemeMode_persistsAndRoundTrips() = runTest(dispatcher) {
        repo.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repo.snapshot().themeMode)

        repo.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, repo.snapshot().themeMode)
    }

    @Test
    fun setShowUnits_persistsAndRoundTrips() = runTest(dispatcher) {
        repo.setShowUnits(false)
        assertFalse(repo.snapshot().showUnits)

        repo.setShowUnits(true)
        assertTrue(repo.snapshot().showUnits)
    }

    @Test
    fun themeModeAndShowUnits_areStoredIndependently() = runTest(dispatcher) {
        repo.setThemeMode(ThemeMode.DARK)
        repo.setShowUnits(false)

        val settings = repo.snapshot()
        assertEquals(ThemeMode.DARK, settings.themeMode)
        assertFalse(settings.showUnits)
    }

    @Test
    fun themeMode_fromStorage_unknownOrMissingFallsBackToSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("not-a-mode"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage("DARK"))
    }
}
