package com.quasarapps.dayssince.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quasarapps.dayssince.data.Milestone
import com.quasarapps.dayssince.data.MilestonesRepository
import com.quasarapps.dayssince.widget.MilestoneWidgets
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class MilestonesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MilestonesRepository(app)

    val milestones: StateFlow<List<Milestone>> = repo.milestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // The legacy single-counter migration runs in DaysSinceApplication.onCreate so it
    // fires once per process regardless of whether the user enters via MainActivity or
    // the widget config picker — see DaysSinceApplication for the rationale.

    fun addMilestone(title: String, date: LocalDate, time: LocalTime, accent: Int) {
        viewModelScope.launch {
            repo.upsert(
                Milestone(
                    id = Milestone.newId(),
                    title = title.trim().ifBlank { "Milestone" },
                    date = date,
                    time = time,
                    accent = accent,
                )
            )
            MilestoneWidgets.refreshAll(getApplication())
        }
    }

    fun updateMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repo.upsert(milestone.copy(title = milestone.title.trim().ifBlank { "Milestone" }))
            MilestoneWidgets.refreshAll(getApplication())
        }
    }

    fun deleteMilestone(id: String) {
        viewModelScope.launch {
            repo.delete(id)
            MilestoneWidgets.refreshAll(getApplication())
        }
    }
}
