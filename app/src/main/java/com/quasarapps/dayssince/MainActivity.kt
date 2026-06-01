package com.quasarapps.dayssince

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.quasarapps.dayssince.ui.DaysSinceApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val initialId = intent?.getStringExtra(EXTRA_MILESTONE_ID)
        setContent {
            DaysSinceApp(initialMilestoneId = initialId)
        }
    }

    companion object {
        const val EXTRA_MILESTONE_ID = "com.quasarapps.dayssince.MILESTONE_ID"
    }
}
