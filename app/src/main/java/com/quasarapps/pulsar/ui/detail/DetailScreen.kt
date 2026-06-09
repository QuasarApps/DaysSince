package com.quasarapps.pulsar.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.ui.components.CountUpNumber
import com.quasarapps.pulsar.ui.components.rememberElapsedDhm
import com.quasarapps.pulsar.ui.components.rememberElapsedDhms
import com.quasarapps.pulsar.ui.theme.accentStops
import com.quasarapps.pulsar.util.LocalizedDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val SpaceEdge = Color(0xFF060309)

@Composable
fun DetailScreen(
    milestone: Milestone?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    showUnits: Boolean = true,
) {
    if (milestone == null) {
        MilestoneMissing(onBack)
        return
    }

    // Only the H/M/S row needs per-second updates; when units are hidden the day count needs only
    // per-minute resolution, so use the cheaper minute ticker.
    val dhms = if (showUnits) {
        rememberElapsedDhms(milestone.date, milestone.time)
    } else {
        rememberElapsedDhm(milestone.date, milestone.time)
    }
    val (accentStart, accentEnd) = accentStops(milestone.accent)
    var menuOpen by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    val locale = LocalConfiguration.current.locales[0]
    val timeText = remember(milestone.time, locale) {
        milestone.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
    }
    val dateText = remember(milestone.date, locale) {
        LocalizedDateFormat.formatLongDate(milestone.date, locale)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Radial accent hero blooming from the upper-left out to the cosmic edge.
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colorStops = arrayOf(0f to accentStart, 0.7f to accentEnd, 1f to SpaceEdge),
                        center = Offset(size.width * 0.3f, size.height * 0.2f),
                        radius = size.maxDimension * 1.15f,
                    ),
                )
            },
    ) {
        // Legibility scrim so white text/icons meet WCAG AA over every accent. The lightest (Solar)
        // is worst case — bare white is only ~1.6:1. ~0.30 top → ~0.50 bottom lifts white-on-Solar to
        // ~3.2:1 at the top (AA-Large for title/icons) and ~5.7:1 lower (AA for body); darker accents
        // clear AA comfortably.
        Box(
            Modifier
                .matchParentSize()
                .background(Brush.verticalGradient(listOf(Color(0x4D000000), Color(0x80000000)))),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.detail_back_content_description),
                        tint = Color.White,
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.detail_more_options_content_description),
                            tint = Color.White,
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_edit)) },
                            onClick = { menuOpen = false; onEdit() },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.detail_action_reset)) },
                            onClick = { menuOpen = false; confirmReset = true },
                            leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                        )
                        DropdownMenuItem(
                            // Delete immediately; the caller shows an Undo snackbar (no confirm dialog).
                            text = { Text(stringResource(R.string.action_delete)) },
                            onClick = { menuOpen = false; onDelete() },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        )
                    }
                }
            }

            // Milestone name, pinned near the top.
            Spacer(Modifier.height(12.dp))
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.weight(1f))

            // Hero
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CountUpNumber(
                    target = dhms.days,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 100.sp,
                        lineHeight = 100.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.detail_days_label),
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 3.sp,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Spacer(Modifier.height(28.dp))
                // The H/M/S breakdown is opt-out via Settings; the day count and since-line always stay.
                if (showUnits) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        GlassStat(value = dhms.hours, label = stringResource(R.string.detail_hours_label))
                        GlassStat(value = dhms.minutes, label = stringResource(R.string.detail_minutes_label))
                        GlassStat(value = dhms.seconds, label = stringResource(R.string.detail_seconds_label))
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Text(
                    text = stringResource(R.string.detail_since_date_at_time, dateText, timeText),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))

            // Footer
            Text(
                text = stringResource(R.string.detail_widget_hint),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.detail_reset_dialog_title)) },
            text = { Text(stringResource(R.string.detail_reset_dialog_message)) },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onReset() }) {
                    Text(stringResource(R.string.action_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun RowScope.GlassStat(value: Long, label: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), MaterialTheme.shapes.medium)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.5.sp,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}

@Composable
private fun MilestoneMissing(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.detail_missing),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onBack) { Text(stringResource(R.string.detail_go_back)) }
        }
    }
}
