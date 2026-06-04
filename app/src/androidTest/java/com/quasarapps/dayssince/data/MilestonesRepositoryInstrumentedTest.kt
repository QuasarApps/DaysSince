package com.quasarapps.dayssince.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

/**
 * On-device integration test for [MilestonesRepository]'s DataStore read/modify/write cycle.
 *
 * Each test gets an isolated, empty Preferences DataStore backed by a throwaway temp file (injected
 * via the internal constructor), so the assertions reflect data that actually round-tripped through
 * the persistence layer on a real device — not a host-JVM stub.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MilestonesRepositoryInstrumentedTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = UnconfinedTestDispatcher(scheduler)
    private val dataStoreScope = CoroutineScope(dispatcher)

    private lateinit var appContext: Context
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repo: MilestonesRepository
    private lateinit var tempFile: File

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()

        tempFile = File.createTempFile("dayssince_androidtest_", ".preferences_pb", appContext.cacheDir)
            .also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) { tempFile }
        repo = MilestonesRepository(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
        tempFile.delete()
    }

    private fun milestone(
        id: String,
        title: String = "T",
        accent: Int = 0,
        createdAt: Long = 0L,
    ) = Milestone(
        id = id,
        title = title,
        date = LocalDate.of(2026, 1, 1),
        time = LocalTime.of(9, 0),
        accent = accent,
        createdAt = createdAt,
    )

    // ---- CRUD ----

    @Test
    fun upsert_insertsNewMilestone() = runTest(dispatcher) {
        repo.upsert(milestone("a", title = "Sober"))

        assertEquals("Sober", repo.snapshot().single().title)
    }

    @Test
    fun upsert_existingId_updatesInPlaceWithoutDuplicating() = runTest(dispatcher) {
        repo.upsert(milestone("a", title = "Old", createdAt = 100L))
        repo.upsert(milestone("a", title = "New", createdAt = 100L))

        assertEquals(listOf("New"), repo.snapshot().map { it.title })
    }

    @Test
    fun milestones_areSortedByCreatedAtDescending() = runTest(dispatcher) {
        repo.upsert(milestone("oldest", createdAt = 100L))
        repo.upsert(milestone("newest", createdAt = 300L))
        repo.upsert(milestone("middle", createdAt = 200L))

        assertEquals(listOf("newest", "middle", "oldest"), repo.snapshot().map { it.id })
    }

    @Test
    fun getById_returnsMatchOrNull() = runTest(dispatcher) {
        repo.upsert(milestone("a"))

        assertEquals("a", repo.getById("a")?.id)
        assertNull(repo.getById("missing"))
    }

    @Test
    fun delete_removesOnlyTheTargetedMilestone() = runTest(dispatcher) {
        repo.upsert(milestone("a", createdAt = 100L))
        repo.upsert(milestone("b", createdAt = 200L))

        repo.delete("a")

        assertEquals(listOf("b"), repo.snapshot().map { it.id })
    }

    // ---- delete -> widget-binding cascade ----

    @Test
    fun delete_alsoUnbindsWidgetsPointingAtThatMilestone() = runTest(dispatcher) {
        repo.upsert(milestone("a"))
        repo.upsert(milestone("b"))
        repo.bindWidget(appWidgetId = 11, milestoneId = "a")
        repo.bindWidget(appWidgetId = 22, milestoneId = "b")

        repo.delete("a")

        assertNull(repo.bindingForWidget(11))
        assertEquals("b", repo.bindingForWidget(22)?.milestoneId)
    }

    // ---- widget bindings ----

    @Test
    fun bindWidget_thenBindingAndMilestoneLookupsResolve() = runTest(dispatcher) {
        repo.upsert(milestone("a", title = "Gym"))
        repo.bindWidget(appWidgetId = 7, milestoneId = "a", transparent = true)

        val binding = repo.bindingForWidget(7)
        assertEquals("a", binding?.milestoneId)
        assertEquals(true, binding?.transparent)
        assertEquals("Gym", repo.milestoneForWidget(7)?.title)
    }

    @Test
    fun unbindWidget_removesBinding() = runTest(dispatcher) {
        repo.upsert(milestone("a"))
        repo.bindWidget(appWidgetId = 7, milestoneId = "a")

        repo.unbindWidget(7)

        assertNull(repo.bindingForWidget(7))
        assertNull(repo.milestoneForWidget(7))
    }

    @Test
    fun milestoneForWidget_isNull_whenBindingPointsAtMissingMilestone() = runTest(dispatcher) {
        repo.bindWidget(appWidgetId = 7, milestoneId = "ghost")

        assertEquals("ghost", repo.bindingForWidget(7)?.milestoneId)
        assertNull(repo.milestoneForWidget(7))
    }
}
