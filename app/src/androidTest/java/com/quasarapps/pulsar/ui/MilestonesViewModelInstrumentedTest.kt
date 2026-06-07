package com.quasarapps.pulsar.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.data.MilestonesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
 * (trim + fall back to "Milestone" when blank). A real temp-file DataStore-backed repository is
 * injected so the asserted state is data that actually round-tripped through persistence.
 *
 * Unlike the Robolectric `MilestonesViewModelTest`, this does NOT use `Dispatchers.setMain` + test
 * dispatchers: on a real device `Dispatchers.Main` is the live main looper that the instrumentation
 * runner also uses, so swapping it in races ("Main is used concurrently with setting it"). Instead
 * the view model runs on the real main dispatcher and we await the async write by collecting the
 * repository's DataStore flow until it reaches the expected state.
 */
@RunWith(AndroidJUnit4::class)
class MilestonesViewModelInstrumentedTest {

    // DataStore's collector is a long-running job; give it its own scope on the IO dispatcher.
    private val dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var app: Application
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repo: MilestonesRepository
    private lateinit var viewModel: MilestonesViewModel
    private lateinit var tempFile: File

    private val date = LocalDate.of(2026, 1, 1)
    private val time = LocalTime.of(9, 0)

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()

        tempFile = File.createTempFile("pulsar_vm_androidtest_", ".preferences_pb", app.cacheDir)
            .also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) { tempFile }
        repo = MilestonesRepository(dataStore)
        viewModel = MilestonesViewModel(app, repo)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
        tempFile.delete()
    }

    /** Blocks until the persisted milestone list satisfies [predicate], or times out. */
    private fun awaitMilestones(predicate: (List<Milestone>) -> Boolean): List<Milestone> =
        runBlocking { withTimeout(TIMEOUT_MS) { repo.milestones.first(predicate) } }

    /** Blocks until milestone [id]'s title equals [expected], returning that title. */
    private fun awaitTitle(id: String, expected: String): String =
        runBlocking {
            withTimeout(TIMEOUT_MS) {
                repo.milestones
                    .map { list -> list.firstOrNull { it.id == id }?.title }
                    .first { it == expected }!!
            }
        }

    @Test
    fun addMilestone_blankTitle_fallsBackToMilestone() {
        viewModel.addMilestone(title = "   ", date = date, time = time, accent = 0)

        assertEquals("Milestone", awaitMilestones { it.isNotEmpty() }.single().title)
    }

    @Test
    fun addMilestone_trimsSurroundingWhitespace() {
        viewModel.addMilestone(title = "  Gym  ", date = date, time = time, accent = 2)

        val stored = awaitMilestones { it.isNotEmpty() }.single()
        assertEquals("Gym", stored.title)
        assertEquals(2, stored.accent)
        assertEquals(date, stored.date)
        assertEquals(time, stored.time)
    }

    @Test
    fun updateMilestone_blankTitle_fallsBackToMilestone() {
        runBlocking { repo.upsert(Milestone(id = "a", title = "Original", date = date, time = time, createdAt = 1L)) }

        viewModel.updateMilestone(
            Milestone(id = "a", title = "   ", date = date, time = time, createdAt = 1L),
        )

        assertEquals("Milestone", awaitTitle("a", "Milestone"))
    }

    @Test
    fun deleteMilestone_removesIt() {
        runBlocking { repo.upsert(Milestone(id = "a", title = "Gym", date = date, time = time, createdAt = 1L)) }

        viewModel.deleteMilestone("a")

        assertTrue(awaitMilestones { it.isEmpty() }.isEmpty())
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
