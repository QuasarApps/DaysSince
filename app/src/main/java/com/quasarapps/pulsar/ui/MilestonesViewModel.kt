package com.quasarapps.pulsar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.data.MilestonesRepository
import com.quasarapps.pulsar.widget.MilestoneWidgets
import com.quasarapps.pulsar.widget.WidgetRefreshScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    fun deleteMilestone(id: String) {
        viewModelScope.launch {
            repo.delete(id)
            refreshWidgets()
        }
    }

    /**
     * Pushes the latest data to placed widgets after a change. Updates in-process for an instant
     * redraw, then enqueues a WorkManager backstop so the refresh still happens if the app is
     * backgrounded / its process is torn down right after the edit. The backstop is guarded because
     * WorkManager isn't initialized in plain (non-instrumented) unit tests.
     */
    private suspend fun refreshWidgets() {
        val app = getApplication<Application>()
        MilestoneWidgets.refreshAll(app)
        runCatching { WidgetRefreshScheduler.refreshNow(app) }
    }
}
