package com.quasarapps.dayssince.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

/**
 * Exercises the DataStore read/modify/write cycle of [MilestonesRepository] — the layer the
 * serialization tests don't reach. A test DataStore is injected via the internal constructor so
 * each test gets an isolated, empty store backed by a throwaway temp file.
 *
 * Robolectric is required because the repository's JSON (de)serialization uses org.json, which is
 * stubbed out in plain JVM tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MilestonesRepositoryTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = UnconfinedTestDispatcher(scheduler)

    // The DataStore's internal collector is a long-running job; keep it in its own scope so it
    // doesn't trip runTest's "lingering coroutines" check. Shares the scheduler for one clock.
    private val dataStoreScope = CoroutineScope(dispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repo: MilestonesRepository

    @Before
    fun setUp() {
        val file = File.createTempFile("dayssince_test_", ".preferences_pb").also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) { file }
        repo = MilestonesRepository(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
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

        val all = repo.snapshot()
        assertEquals(1, all.size)
        assertEquals("Sober", all.single().title)
    }

    @Test
    fun upsert_existingId_updatesInPlaceWithoutDuplicating() = runTest(dispatcher) {
        repo.upsert(milestone("a", title = "Old", createdAt = 100L))
        repo.upsert(milestone("a", title = "New", createdAt = 100L))

        val all = repo.snapshot()
        assertEquals(1, all.size)
        assertEquals("New", all.single().title)
    }

    @Test
    fun milestones_areSortedByCreatedAtDescending() = runTest(dispatcher) {
        repo.upsert(milestone("oldest", createdAt = 100L))
        repo.upsert(milestone("newest", createdAt = 300L))
        repo.upsert(milestone("middle", createdAt = 200L))

        val ids = repo.snapshot().map { it.id }
        assertEquals(listOf("newest", "middle", "oldest"), ids)
    }

    @Test
    fun delete_removesOnlyTheTargetedMilestone() = runTest(dispatcher) {
        repo.upsert(milestone("a", createdAt = 100L))
        repo.upsert(milestone("b", createdAt = 200L))

        repo.delete("a")

        val ids = repo.snapshot().map { it.id }
        assertEquals(listOf("b"), ids)
    }

    // ---- delete -> widget-binding cascade ----

    @Test
    fun delete_alsoUnbindsWidgetsPointingAtThatMilestone() = runTest(dispatcher) {
        repo.upsert(milestone("a"))
        repo.upsert(milestone("b"))
        repo.bindWidget(appWidgetId = 11, milestoneId = "a")
        repo.bindWidget(appWidgetId = 22, milestoneId = "b")

        repo.delete("a")

        // The binding for the deleted milestone is gone; the unrelated one survives.
        assertNull(repo.bindingForWidget(11))
        assertEquals("b", repo.bindingForWidget(22)?.milestoneId)
    }

    // ---- widget bindings ----

    @Test
    fun bindWidget_thenBindingAndRenderDataResolve() = runTest(dispatcher) {
        repo.upsert(milestone("a", title = "Gym"))
        repo.bindWidget(appWidgetId = 7, milestoneId = "a", transparent = true)

        val binding = repo.bindingForWidget(7)
        assertEquals("a", binding?.milestoneId)
        assertEquals(true, binding?.transparent)
        val render = repo.widgetRenderDataFlow(7).first()
        assertEquals("Gym", render.milestone?.title)
        assertEquals(true, render.transparent)
    }

    @Test
    fun unbindWidget_removesBinding() = runTest(dispatcher) {
        repo.upsert(milestone("a"))
        repo.bindWidget(appWidgetId = 7, milestoneId = "a")

        repo.unbindWidget(7)

        assertNull(repo.bindingForWidget(7))
        assertNull(repo.widgetRenderDataFlow(7).first().milestone)
    }

    @Test
    fun widgetRenderData_milestoneIsNull_whenBindingPointsAtMissingMilestone() = runTest(dispatcher) {
        repo.bindWidget(appWidgetId = 7, milestoneId = "ghost")

        assertEquals("ghost", repo.bindingForWidget(7)?.milestoneId)
        assertNull(repo.widgetRenderDataFlow(7).first().milestone)
    }
}
