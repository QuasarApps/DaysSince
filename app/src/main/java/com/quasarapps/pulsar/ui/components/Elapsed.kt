package com.quasarapps.pulsar.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.quasarapps.pulsar.ElapsedTime
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

/**
 * Elapsed days/hours/minutes since [date]/[time], recomputed each time the wall clock crosses a
 * minute boundary (aligned to the boundary so the displayed minute never lags by up to 59s).
 */
@Composable
fun rememberElapsedDhm(date: LocalDate, time: LocalTime): ElapsedTime.ElapsedDhm {
    var nowTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            val lt = LocalTime.now()
            val msToNextMinute = ((60 - lt.second) * 1_000L) - (lt.nano / 1_000_000L)
            delay(msToNextMinute.coerceIn(1_000L, 60_000L))
            nowTick = System.currentTimeMillis()
        }
    }
    return remember(date, time, nowTick) { ElapsedTime.sincePickedDhm(date, time) }
}

/** True when the user has disabled animations system-wide (Settings > Accessibility / Developer). */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
}
