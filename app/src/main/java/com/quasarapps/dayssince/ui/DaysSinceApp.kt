package com.quasarapps.dayssince.ui

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
import androidx.compose.ui.unit.dp
import com.quasarapps.dayssince.DaysSince
import com.quasarapps.dayssince.SelectedStartDateTime
import com.quasarapps.dayssince.ui.theme.DaysSinceTheme
import com.quasarapps.dayssince.util.EnglishDateFormat
import com.quasarapps.dayssince.widget.DaysHoursMinutesSinceWidgetProvider
import com.quasarapps.dayssince.widget.DaysSinceWidgetProvider
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

    val daysSincePicked by remember(dhmSincePicked) {
        derivedStateOf { dhmSincePicked.days }
    }

    DaysSinceTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = daysSincePicked.toString(),
                    style = MaterialTheme.typography.displayMedium
                )

                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = "%d days, %02d hours, %02d minutes".format(
                        dhmSincePicked.days,
                        dhmSincePicked.hours,
                        dhmSincePicked.minutes
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Days Since",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    modifier = Modifier.padding(top = 20.dp),
                    text = EnglishDateFormat.formatOrdinalDate(selectedDate),
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "at",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "%02d:%02d".format(selectedTime.hour, selectedTime.minute),
                    style = MaterialTheme.typography.bodyLarge
                )

                NativePickers(
                    modifier = Modifier.padding(top = 24.dp),
                    selectedDate = selectedDate,
                    selectedTime = selectedTime,
                    onSelectedDateChange = { newDate ->
                        selectedDate = newDate
                        SelectedStartDateTime.persistDate(context, newDate)
                        DaysSinceWidgetProvider.requestUpdate(context)
                        DaysHoursMinutesSinceWidgetProvider.requestUpdate(context)
                    },
                    onSelectedTimeChange = { newTime ->
                        selectedTime = newTime
                        SelectedStartDateTime.persistTime(context, newTime)
                        DaysSinceWidgetProvider.requestUpdate(context)
                        DaysHoursMinutesSinceWidgetProvider.requestUpdate(context)
                    }
                )
            }
        }
    }
}
