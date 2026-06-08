package com.quasarapps.pulsar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.quasarapps.pulsar.ui.DeepLinkTarget
import com.quasarapps.pulsar.ui.PulsarApp
import com.quasarapps.pulsar.widget.WidgetRefreshScheduler

class MainActivity : ComponentActivity() {

    // The most recent deep-link target delivered by a launch intent, held as Compose state so the
    // content re-navigates when a new one arrives. Because the activity is `singleTop`, a widget tap
    // while the app is already running comes in via onNewIntent (not a fresh onCreate), so updating
    // this is what carries the user to the tapped milestone without tearing down their in-app state.
    private var deepLink by mutableStateOf<DeepLinkTarget?>(null)

    // Monotonic per-delivery token so successive taps are distinct values — even of the *same*
    // milestone — and therefore re-trigger navigation rather than being coalesced as "no change".
    private var deliveryCount = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show the system splash (brand background + launcher mark) during cold start, then hand off
        // to the app theme. The richer wordmark + tagline moment is the in-app Compose splash.
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Only consume the launch intent on a genuine fresh start. On a configuration-change
        // recreation (e.g. rotation) the NavController restores the back stack itself, so
        // re-delivering here would wrongly snap back to the milestone's detail.
        if (savedInstanceState == null) deliverDeepLink(intent)

        // Re-arm the periodic widget refresh if any widget is placed (no-op otherwise). With the
        // widgets' updatePeriodMillis=0 there's no platform alarm, so this is the recovery path if
        // the persisted WorkManager job was ever lost.
        WidgetRefreshScheduler.ensureScheduledIfWidgetsPlaced(this)

        setContent {
            PulsarApp(deepLink = deepLink)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Keep getIntent() current (so a later recreation re-reads the right intent) and route the
        // newly-tapped milestone to the running composition.
        setIntent(intent)
        deliverDeepLink(intent)
    }

    private fun deliverDeepLink(intent: Intent?) {
        val id = intent?.getStringExtra(EXTRA_MILESTONE_ID) ?: return
        deepLink = DeepLinkTarget(milestoneId = id, token = deliveryCount++)
    }

    companion object {
        const val EXTRA_MILESTONE_ID = "com.quasarapps.pulsar.MILESTONE_ID"
    }
}
