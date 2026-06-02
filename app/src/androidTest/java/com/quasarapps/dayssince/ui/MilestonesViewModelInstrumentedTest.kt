package com.quasarapps.dayssince.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.dayssince.data.Milestone
import com.quasarapps.dayssince.data.MilestonesRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

/**
 * On-device test for the title-normalization [MilestonesViewModel] layers on top of the repository
 * (trim + fall back to "Milestone" when blank). A test DataStore-backed repository is injected so
 * the asserted state is data that actually round-tripped through persistence on the device.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MilestonesViewModelInstrumentedTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = UnconfinedTestDispatcher(scheduler)
    private val dataStoreScope = CoroutineScope(dispatcher)

    private lateinit var app: Application
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repo: MilestonesRepository
    private lateinit var viewModel: MilestonesViewModel
    private lateinit var tempFile: File

    private val date = LocalDate.of(2026, 1, 1)
    private val time = LocalTime.of(9, 0)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        app = ApplicationProvider.getApplicationContext()

        tempFile = File.createTempFile("dayssince_vm_androidtest_", ".preferences_pb", app.cacheDir)
            .also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) { tempFile }
        repo = MilestonesRepository(app, dataStore)
        viewModel = MilestonesViewModel(app, repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        dataStoreScope.cancel()
        tempFile.delete()
    }

    @Test
    fun addMilestone_blankTitle_fallsBackToMilestone() = runTest(dispatcher) {
        viewModel.addMilestone(title = "   ", date = date, time = time, accent = 0)
        advanceUntilIdle()

        assertEquals("Milestone", repo.snapshot().single().title)
    }

    @Test
    fun addMilestone_trimsSurroundingWhitespace() = runTest(dispatcher) {
        viewModel.addMilestone(title = "  Gym  ", date = date, time = time, accent = 2)
        advanceUntilIdle()

        val stored = repo.snapshot().single()
        assertEquals("Gym", stored.title)
        assertEquals(2, stored.accent)
        assertEquals(date, stored.date)
        assertEquals(time, stored.time)
    }

    @Test
    fun updateMilestone_blankTitle_fallsBackToMilestone() = runTest(dispatcher) {
        repo.upsert(Milestone(id = "a", title = "Original", date = date, time = time, createdAt = 1L))

        viewModel.updateMilestone(
            Milestone(id = "a", title = "   ", date = date, time = time, createdAt = 1L),
        )
        advanceUntilIdle()

        assertEquals("Milestone", repo.getById("a")?.title)
    }

    @Test
    fun deleteMilestone_removesIt() = runTest(dispatcher) {
        repo.upsert(Milestone(id = "a", title = "Gym", date = date, time = time, createdAt = 1L))

        viewModel.deleteMilestone("a")
        advanceUntilIdle()

        assertTrue(repo.snapshot().isEmpty())
    }
}
