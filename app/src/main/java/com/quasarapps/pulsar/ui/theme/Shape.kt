package com.quasarapps.pulsar.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Pulsar shape scale (design system): M3 radii nudged rounder. Cards land on `large` (24dp);
 * fully-rounded controls (FAB, counters, chips) use a pill shape at the call site.
 */
val PulsarShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
