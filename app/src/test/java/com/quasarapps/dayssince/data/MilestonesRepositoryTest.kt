package com.quasarapps.dayssince.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.quasarapps.dayssince.Prefs
import com.quasarapps.dayssince.SelectedStartDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
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

    private lateinit var appContext: Context
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repo: MilestonesRepository

    @Before
    fun setUp() {
        appContext = RuntimeEnvironment.getApplication()
        // Migration reads/writes the legacy single-counter SharedPreferences; start clean.
        Prefs.get(appContext).edit().clear().commit()

        val file = File.createTempFile("dayssince_test_", ".preferences_pb").also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) { file }
        repo = MilestonesRepository(appContext, dataStore)
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

    // ---- legacy migration ----

    @Test
    fun migrate_seedsAMilestone_fromStoredLegacyCounter() = runTest(dispatcher) {
        val date = LocalDate.of(2025, 6, 15)
        val time = LocalTime.of(7, 30)
        SelectedStartDateTime.persistDate(appContext, date)
        SelectedStartDateTime.persistTime(appContext, time)

        repo.migrateLegacyIfNeeded()

        val seeded = repo.snapshot().single()
        assertEquals(date, seeded.date)
        assertEquals(time, seeded.time)
        assertEquals("Milestone", seeded.title)
    }

    @Test
    fun migrate_doesNotSeed_onFreshInstallWithNoLegacyData() = runTest(dispatcher) {
        // setUp() already cleared the legacy prefs, so hasStored() is false here.
        repo.migrateLegacyIfNeeded()

        assertTrue(repo.snapshot().isEmpty())
    }

    @Test
    fun migrate_doesNotSeed_whenMilestonesAlreadyExist() = runTest(dispatcher) {
        SelectedStartDateTime.persistDate(appContext, LocalDate.of(2025, 6, 15))
        repo.upsert(milestone("existing"))

        repo.migrateLegacyIfNeeded()

        val ids = repo.snapshot().map { it.id }
        assertEquals(listOf("existing"), ids)
    }

    @Test
    fun migrate_runsOnlyOnce_evenIfLegacyDataAppearsLater() = runTest(dispatcher) {
        // First run with no legacy data sets the "migrated" flag without seeding.
        repo.migrateLegacyIfNeeded()

        // Legacy data shows up afterwards; a second run must not retroactively seed it.
        SelectedStartDateTime.persistDate(appContext, LocalDate.of(2025, 6, 15))
        repo.migrateLegacyIfNeeded()

        assertTrue(repo.snapshot().isEmpty())
    }
}
