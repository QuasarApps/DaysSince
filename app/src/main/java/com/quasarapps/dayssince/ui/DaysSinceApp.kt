package com.quasarapps.dayssince.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DaysSinceApp(darkTheme: Boolean = true) {
    val context = LocalContext.current

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }

    // Tick while this composable is on screen so we react to system time changes.
    // Delay is aligned to the next whole minute boundary so the display changes exactly
    // when the minute turns over rather than up to 59 seconds late.
    var nowTick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            nowTick = System.currentTimeMillis()
            val now = LocalTime.now()
            val msToNextMinute =
                ((60 - now.second) * 1_000L) - (now.nano / 1_000_000L)
            delay(msToNextMinute.coerceAtLeast(100L))
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
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Days Since",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                ElapsedTimeBlock(
                    days = dhmSincePicked.days,
                    hours = dhmSincePicked.hours,
                    minutes = dhmSincePicked.minutes
                )

                Spacer(modifier = Modifier.height(12.dp))

                PickedDateTimeSummary(
                    date = selectedDate,
                    time = selectedTime
                )

                Spacer(modifier = Modifier.height(12.dp))

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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = days.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Days",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = hours.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Hours",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = minutes.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Minutes",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun PickedDateTimeSummary(
    date: LocalDate,
    time: LocalTime
) {
    // Locale-aware short time format (e.g. "2:30 PM" on US devices, "14:30" on EU devices)
    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Since",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = EnglishDateFormat.formatOrdinalDate(date),
            style = MaterialTheme.typography.headlineSmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "At",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = time.format(timeFormatter),
            style = MaterialTheme.typography.headlineSmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DaysSincePreviewScreen(
    selectedDate: LocalDate,
    selectedTime: LocalTime
) {
    val dhmSincePicked by remember(selectedDate, selectedTime) {
        derivedStateOf { DaysSince.sincePickedDhm(selectedDate, selectedTime) }
    }

    DaysSinceTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Days Since",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                ElapsedTimeBlock(
                    days = dhmSincePicked.days,
                    hours = dhmSincePicked.hours,
                    minutes = dhmSincePicked.minutes
                )

                Spacer(modifier = Modifier.height(12.dp))

                PickedDateTimeSummary(
                    date = selectedDate,
                    time = selectedTime
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Previews don't need dialogs/pickers; keep spacing similar.
                NativePickers(
                    modifier = Modifier.padding(top = 4.dp),
                    selectedDate = selectedDate,
                    selectedTime = selectedTime,
                    onSelectedDateChange = {},
                    onSelectedTimeChange = {}
                )
            }
        }
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
    val now = LocalTime.of(12, 0)
    DaysSincePreviewScreen(
        selectedDate = LocalDate.now(),
        selectedTime = now.minusHours(2).minusMinutes(15)
    )
}

@Preview(name = "DaysSince - Long ago", showBackground = true)
@Composable
private fun PreviewDaysSinceLongAgo() {
    DaysSincePreviewScreen(
        selectedDate = LocalDate.now().minusDays(1234),
        selectedTime = LocalTime.of(9, 30)
    )
}
