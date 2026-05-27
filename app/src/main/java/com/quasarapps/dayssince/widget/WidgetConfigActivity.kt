@file:OptIn(ExperimentalMaterial3Api::class)

package com.quasarapps.dayssince.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quasarapps.dayssince.DaysSince
import com.quasarapps.dayssince.MainActivity
import com.quasarapps.dayssince.data.Milestone
import com.quasarapps.dayssince.data.MilestonesRepository
import com.quasarapps.dayssince.ui.theme.DaysSinceTheme
import com.quasarapps.dayssince.ui.theme.accentBrush
import com.quasarapps.dayssince.util.EnglishDateFormat
import kotlinx.coroutines.launch

/**
 * Launched when a widget is placed (declared via android:configure). Lets the user pick which
 * milestone this widget instance tracks, binds it, and finishes with RESULT_OK.
 */
class WidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If the user backs out, the widget should not be added.
        setResult(RESULT_CANCELED, resultIntent(appWidgetId))
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        enableEdgeToEdge()
        val repo = MilestonesRepository(this)

        setContent {
            DaysSinceTheme {
                val scope = rememberCoroutineScope()
                val milestones by repo.milestones.collectAsState(initial = emptyList())
                WidgetConfigScreen(
                    milestones = milestones,
                    onPick = { id ->
                        scope.launch {
                            repo.bindWidget(appWidgetId, id)
                            MilestoneWidgets.refreshAll(applicationContext)
                            setResult(RESULT_OK, resultIntent(appWidgetId))
                            finish()
                        }
                    },
                    onOpenApp = {
                        startActivity(Intent(this, MainActivity::class.java))
                    },
                )
            }
        }
    }

    private fun resultIntent(id: Int): Intent =
        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
}

@Composable
private fun WidgetConfigScreen(
    milestones: List<Milestone>,
    onPick: (String) -> Unit,
    onOpenApp: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text("Choose a milestone") })
        },
    ) { padding ->
        if (milestones.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "No milestones yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Open Days Since to add one, then place the widget again.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = onOpenApp) { Text("Open Days Since") }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(milestones, key = { it.id }) { milestone ->
                    MilestoneRow(milestone = milestone, onClick = { onPick(milestone.id) })
                }
            }
        }
    }
}

@Composable
private fun MilestoneRow(milestone: Milestone, onClick: () -> Unit) {
    val days = DaysSince.sincePickedDhm(milestone.date, milestone.time).days
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentBrush(milestone.accent)),
            )
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$days days · since ${EnglishDateFormat.formatOrdinalDate(milestone.date)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
