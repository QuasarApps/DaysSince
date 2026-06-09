package com.quasarapps.pulsar.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.quasarapps.pulsar.ElapsedTime
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

/**
 * Elapsed days/hours/minutes since [date]/[time], recomputed each time the wall clock crosses a
 * minute boundary (aligned to the boundary so the displayed minute never lags by up to 59s).
 *
 * The ticker only runs while the owning lifecycle is RESUMED — off-screen (backgrounded, or a
 * covering screen) it stops, so it doesn't burn wake-ups updating a value nobody can see. On every
 * resume it recomputes immediately so the count is never stale on return.
 */
@Composable
fun rememberElapsedDhm(date: LocalDate, time: LocalTime): ElapsedTime.ElapsedDhm {
    var nowTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            nowTick = System.currentTimeMillis()
            while (true) {
                val lt = LocalTime.now()
                val msToNextMinute = ((60 - lt.second) * 1_000L) - (lt.nano / 1_000_000L)
                delay(msToNextMinute.coerceIn(1_000L, 60_000L))
                nowTick = System.currentTimeMillis()
            }
        }
    }
    return remember(date, time, nowTick) { ElapsedTime.sincePickedDhm(date, time) }
}

/**
 * Elapsed days/hours/minutes/seconds since [date]/[time], recomputed every second (aligned to the
 * second boundary) for the live detail screen. Like [rememberElapsedDhm], the per-second loop only
 * runs while RESUMED (so it doesn't tick in the background) and recomputes immediately on resume.
 */
@Composable
fun rememberElapsedDhms(date: LocalDate, time: LocalTime): ElapsedTime.ElapsedDhm {
    var nowTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            nowTick = System.currentTimeMillis()
            while (true) {
                val now = System.currentTimeMillis()
                delay((1_000L - (now % 1_000L)).coerceIn(1L, 1_000L))
                nowTick = System.currentTimeMillis()
            }
        }
    }
    return remember(date, time, nowTick) { ElapsedTime.sincePickedDhms(date, time) }
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
