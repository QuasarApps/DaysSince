package com.quasarapps.pulsar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.data.MilestonesRepository
import com.quasarapps.pulsar.data.RemovedMilestone
import com.quasarapps.pulsar.widget.WidgetRefreshScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class MilestonesViewModel internal constructor(
    app: Application,
    private val repo: MilestonesRepository,
) : AndroidViewModel(app) {

    constructor(app: Application) : this(app, MilestonesRepository(app))

    val milestones: StateFlow<List<Milestone>> = repo.milestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Localized fallback title for a milestone the user left blank. Resolved from resources (not a
     * hardcoded literal) so a blank title persists the user's-language default — e.g. "Meilenstein"
     * on a German device — matching what the edit screen's preview already shows.
     */
    private val defaultTitle: String
        get() = getApplication<Application>().getString(R.string.milestone_default_title)

    fun addMilestone(title: String, date: LocalDate, time: LocalTime, accent: Int) {
        viewModelScope.launch {
            repo.upsert(
                Milestone(
                    id = Milestone.newId(),
                    title = title.trim().ifBlank { defaultTitle },
                    date = date,
                    time = time,
                    accent = accent,
                )
            )
            refreshWidgets()
        }
    }

    fun updateMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repo.upsert(milestone.copy(title = milestone.title.trim().ifBlank { defaultTitle }))
            refreshWidgets()
        }
    }

    /**
     * The most recent delete, available to undo, or null if there's nothing pending. The UI observes
     * this to show an "Undo" snackbar; [undoDelete] or [clearPendingUndo] clear it.
     */
    private val _pendingUndo = MutableStateFlow<RemovedMilestone?>(null)
    val pendingUndo: StateFlow<RemovedMilestone?> = _pendingUndo.asStateFlow()

    fun deleteMilestone(id: String) {
        viewModelScope.launch {
            _pendingUndo.value = repo.delete(id)
            refreshWidgets()
        }
    }

    /** Restores the pending deleted milestone (and its widget bindings); no-op if nothing's pending. */
    fun undoDelete() {
        val removed = _pendingUndo.value ?: return
        _pendingUndo.value = null
        viewModelScope.launch {
            repo.restore(removed)
            refreshWidgets()
        }
    }

    /** Drops the pending undo (e.g. the snackbar timed out or was dismissed). */
    fun clearPendingUndo() {
        _pendingUndo.value = null
    }

    /**
     * Pushes the latest data to placed widgets after a change via a single WorkManager one-off.
     * Routing through WorkManager (rather than an in-process `updateAll` too) keeps it to one redraw
     * and makes it durable if the app is backgrounded / torn down right after the edit; the one-off
     * is unconstrained so it lands within ~a second. Enqueues nothing when no widget is placed, and
     * is guarded because WorkManager isn't initialized in plain (non-instrumented) unit tests.
     */
    private fun refreshWidgets() {
        runCatching { WidgetRefreshScheduler.refreshNow(getApplication()) }
    }
}
