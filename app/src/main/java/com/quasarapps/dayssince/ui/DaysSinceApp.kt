package com.quasarapps.dayssince.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quasarapps.dayssince.DaysSince
import com.quasarapps.dayssince.SelectedStartDateTime
import com.quasarapps.dayssince.ui.theme.DaysSinceTheme
import com.quasarapps.dayssince.util.EnglishDateFormat
import com.quasarapps.dayssince.widget.DaysHoursMinutesSinceWidgetProvider
import com.quasarapps.dayssince.widget.DaysSinceWidgetProvider
import com.quasarapps.dayssince.widget.WidgetBroadcasts
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DaysSinceApp(darkTheme: Boolean = true) {
    val context = LocalContext.current

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }

    // Tick while this composable is on screen so we react to system time changes.
    var nowTick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            nowTick = System.currentTimeMillis()
            delay(60_000L)
        }
    }

    // Load persisted values once.
    LaunchedEffect(context) {
        val picked = SelectedStartDateTime.load(context)
        selectedDate = picked.date
        selectedTime = picked.time
    }

    val dhmSincePicked by remember(selectedDate, selectedTime, nowTick) {
        derivedStateOf { DaysSince.sincePickedDhm(selectedDate, selectedTime) }
    }

    DaysSinceTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Days since…",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ElapsedTimeBlock(
                    days = dhmSincePicked.days,
                    hours = dhmSincePicked.hours,
                    minutes = dhmSincePicked.minutes
                )

                PickedDateTimeSummary(
                    date = selectedDate,
                    time = selectedTime
                )

                NativePickers(
                    modifier = Modifier.padding(top = 4.dp),
                    selectedDate = selectedDate,
                    selectedTime = selectedTime,
                    onSelectedDateChange = { newDate ->
                        selectedDate = newDate
                        SelectedStartDateTime.persistDate(context, newDate)
                        WidgetBroadcasts.requestUpdate(context, DaysSinceWidgetProvider::class.java)
                        WidgetBroadcasts.requestUpdate(
                            context,
                            DaysHoursMinutesSinceWidgetProvider::class.java
                        )
                    },
                    onSelectedTimeChange = { newTime ->
                        selectedTime = newTime
                        SelectedStartDateTime.persistTime(context, newTime)
                        WidgetBroadcasts.requestUpdate(context, DaysSinceWidgetProvider::class.java)
                        WidgetBroadcasts.requestUpdate(
                            context,
                            DaysHoursMinutesSinceWidgetProvider::class.java
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ElapsedTimeBlock(
    days: Long,
    hours: Long,
    minutes: Long
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "It’s been:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$days days",
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = "$hours hours",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "$minutes minutes",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun PickedDateTimeSummary(
    date: LocalDate,
    time: LocalTime
) {
    Text(
        text = "Since ${EnglishDateFormat.formatOrdinalDate(date)} at %02d:%02d".format(
            time.hour,
            time.minute
        ),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// Remove the preview-only screen (it hid the pickers/buttons area) and instead drive DaysSinceApp
// by writing deterministic preview values into the same persistence it already reads.

@Composable
private fun PersistPreviewSelection(
    context: Context,
    date: LocalDate,
    time: LocalTime
) {
    LaunchedEffect(date, time) {
        SelectedStartDateTime.persistDate(context, date)
        SelectedStartDateTime.persistTime(context, time)
    }
}

@Preview(name = "DaysSince - Dark", showBackground = true)
@Composable
private fun PreviewDaysSinceDark() {
    DaysSinceApp(darkTheme = true)
}

@Preview(name = "DaysSince - Light", showBackground = true)
@Composable
private fun PreviewDaysSinceLight() {
    DaysSinceApp(darkTheme = false)
}

@Preview(name = "DaysSince - Recent (2h 15m)", showBackground = true)
@Composable
private fun PreviewDaysSinceRecent() {
    val context = LocalContext.current
    val date = LocalDate.now()
    val time = LocalTime.now().minusHours(2).minusMinutes(15).withSecond(0).withNano(0)
    PersistPreviewSelection(context = context, date = date, time = time)

    DaysSinceApp(darkTheme = true)
}

@Preview(name = "DaysSince - Long ago", showBackground = true)
@Composable
private fun PreviewDaysSinceLongAgo() {
    val context = LocalContext.current
    val date = LocalDate.now().minusDays(1234)
    val time = LocalTime.of(9, 30)
    PersistPreviewSelection(context = context, date = date, time = time)

    DaysSinceApp(darkTheme = true)
}
