package com.quasarapps.pulsar.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.data.MilestonesRepository
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

/**
 * Verifies the title-normalization the view model layers on top of the repository
 * (trim + fall back to "Milestone" when blank). A test DataStore-backed repository is injected so
 * the asserted state is the data that actually round-tripped through persistence.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MilestonesViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = UnconfinedTestDispatcher(scheduler)
    private val dataStoreScope = CoroutineScope(dispatcher)

    private lateinit var app: Application
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repo: MilestonesRepository
    private lateinit var viewModel: MilestonesViewModel

    private val date = LocalDate.of(2026, 1, 1)
    private val time = LocalTime.of(9, 0)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        app = RuntimeEnvironment.getApplication()

        val file = File.createTempFile("pulsar_vm_", ".preferences_pb").also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) { file }
        repo = MilestonesRepository(dataStore)
        viewModel = MilestonesViewModel(app, repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        dataStoreScope.cancel()
    }

    @Test
    fun addMilestone_blankTitle_fallsBackToLocalizedDefault() = runTest(dispatcher) {
        viewModel.addMilestone(title = "   ", date = date, time = time, accent = 0)
        advanceUntilIdle()

        // The fallback comes from the string resource, not a hardcoded literal (en -> "Milestone").
        assertEquals(app.getString(R.string.milestone_default_title), repo.snapshot().single().title)
    }

    @Test
    @Config(qualifiers = "de")
    fun addMilestone_blankTitle_usesLocalizedDefaultUnderNonEnglishLocale() = runTest(dispatcher) {
        viewModel.addMilestone(title = "", date = date, time = time, accent = 0)
        advanceUntilIdle()

        // Guards against re-hardcoding the English word: on a German device a blank title must
        // persist the German default ("Meilenstein"), not "Milestone".
        val stored = repo.snapshot().single().title
        assertEquals(app.getString(R.string.milestone_default_title), stored)
        assertEquals("Meilenstein", stored)
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
    fun updateMilestone_blankTitle_fallsBackToLocalizedDefault() = runTest(dispatcher) {
        repo.upsert(
            Milestone(id = "a", title = "Original", date = date, time = time, createdAt = 1L),
        )

        viewModel.updateMilestone(
            Milestone(id = "a", title = "   ", date = date, time = time, createdAt = 1L),
        )
        advanceUntilIdle()

        assertEquals(
            app.getString(R.string.milestone_default_title),
            repo.snapshot().firstOrNull { it.id == "a" }?.title,
        )
    }

    @Test
    fun deleteMilestone_removesItAndExposesPendingUndo() = runTest(dispatcher) {
        repo.upsert(Milestone(id = "a", title = "Gym", date = date, time = time, createdAt = 1L))

        viewModel.deleteMilestone("a")
        advanceUntilIdle()

        assertEquals(emptyList<Milestone>(), repo.snapshot())
        assertEquals("a", viewModel.pendingUndo.value?.milestone?.id)
    }

    @Test
    fun undoDelete_restoresTheMilestoneAndClearsPendingUndo() = runTest(dispatcher) {
        repo.upsert(Milestone(id = "a", title = "Gym", date = date, time = time, accent = 2, createdAt = 1L))
        viewModel.deleteMilestone("a")
        advanceUntilIdle()

        viewModel.undoDelete()
        advanceUntilIdle()

        val restored = repo.snapshot().single()
        assertEquals("a", restored.id)
        assertEquals("Gym", restored.title)
        assertEquals(2, restored.accent)
        assertNull(viewModel.pendingUndo.value)
    }
}
