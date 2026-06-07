package com.quasarapps.pulsar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.quasarapps.pulsar.ui.PulsarApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val initialId = intent?.getStringExtra(EXTRA_MILESTONE_ID)
        setContent {
            PulsarApp(initialMilestoneId = initialId)
        }
    }

    companion object {
        const val EXTRA_MILESTONE_ID = "com.quasarapps.pulsar.MILESTONE_ID"
    }
}
