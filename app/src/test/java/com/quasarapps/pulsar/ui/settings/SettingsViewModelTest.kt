package com.quasarapps.pulsar.ui.settings

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.quasarapps.pulsar.data.SettingsRepository
import com.quasarapps.pulsar.data.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

/**
 * Verifies the view model forwards edits to the repository and that they round-trip through
 * persistence. A test DataStore-backed repository is injected so the assertions reflect data that
 * actually persisted (mirrors [com.quasarapps.pulsar.ui.MilestonesViewModelTest]).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = UnconfinedTestDispatcher(scheduler)
    private val dataStoreScope = CoroutineScope(dispatcher)

    private lateinit var app: Application
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repo: SettingsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        app = RuntimeEnvironment.getApplication()

        val file = File.createTempFile("pulsar_settings_vm_", ".preferences_pb").also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) { file }
        repo = SettingsRepository(dataStore)
        viewModel = SettingsViewModel(app, repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        dataStoreScope.cancel()
    }

    @Test
    fun setThemeMode_persistsThroughRepository() = runTest(dispatcher) {
        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, repo.snapshot().themeMode)
    }

    @Test
    fun setShowUnits_persistsThroughRepository() = runTest(dispatcher) {
        viewModel.setShowUnits(false)
        advanceUntilIdle()

        assertFalse(repo.snapshot().showUnits)
    }
}
