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
import com.quasarapps.dayssince.Prefs
import com.quasarapps.dayssince.ui.theme.DaysSinceTheme
import com.quasarapps.dayssince.util.EnglishDateFormat
import com.quasarapps.dayssince.widget.DayOfMonthAppWidgetProvider
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

private const val PREF_SELECTED_DATE = "selected_date"
private const val PREF_SELECTED_TIME = "selected_time"

@Composable
fun DaysSinceApp(darkTheme: Boolean = true) {
    val context = LocalContext.current
    val prefs = remember(context) { Prefs.get(context) }

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
    LaunchedEffect(prefs) {
        prefs.getString(PREF_SELECTED_DATE, null)
            ?.runCatching(LocalDate::parse)
            ?.getOrNull()
            ?.let { selectedDate = it }

        prefs.getString(PREF_SELECTED_TIME, null)
            ?.runCatching(LocalTime::parse)
            ?.getOrNull()
            ?.let { selectedTime = it }
    }

    val daysSincePicked by remember(selectedDate, selectedTime, nowTick) {
        derivedStateOf { DaysSince.sincePicked(selectedDate, selectedTime) }
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
                        prefs.edit().putString(PREF_SELECTED_DATE, newDate.toString()).apply()
                        DayOfMonthAppWidgetProvider.requestUpdate(context)
                    },
                    onSelectedTimeChange = { newTime ->
                        selectedTime = newTime
                        prefs.edit().putString(PREF_SELECTED_TIME, newTime.toString()).apply()
                        DayOfMonthAppWidgetProvider.requestUpdate(context)
                    }
                )
            }
        }
    }
}
