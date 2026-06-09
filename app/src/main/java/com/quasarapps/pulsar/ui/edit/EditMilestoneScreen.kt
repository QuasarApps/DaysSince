@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.quasarapps.pulsar.ui.edit

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quasarapps.pulsar.ElapsedTime
import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.ui.components.Starburst
import com.quasarapps.pulsar.ui.components.rememberElapsedDhm
import com.quasarapps.pulsar.ui.theme.MilestoneAccents
import com.quasarapps.pulsar.ui.theme.NewBeginningBrush
import com.quasarapps.pulsar.ui.theme.accentBrush
import com.quasarapps.pulsar.ui.theme.accentOrDefault
import com.quasarapps.pulsar.util.LocalizedDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// LocalDate/LocalTime aren't Parcelable, so persist them as primitives (epoch day / nano-of-day).
private val LocalDateSaver = Saver<LocalDate, Long>(
    save = { it.toEpochDay() },
    restore = { LocalDate.ofEpochDay(it) },
)
private val LocalTimeSaver = Saver<LocalTime, Long>(
    save = { it.toNanoOfDay() },
    restore = { LocalTime.ofNanoOfDay(it) },
)

@Composable
fun EditMilestoneScreen(
    existing: Milestone?,
    onSave: (title: String, date: LocalDate, time: LocalTime, accent: Int) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current

    // rememberSaveable so rotation doesn't wipe entered text (LocalDate/LocalTime via the Savers above).
    // Keyed on existing?.id: in the edit route `existing` is Flow-backed and null on first composition,
    // so keying re-initializes the fields once it loads (and when switching ids) while still surviving
    // rotation for the same milestone.
    var title by rememberSaveable(existing?.id) { mutableStateOf(existing?.title ?: "") }
    var date by rememberSaveable(existing?.id, stateSaver = LocalDateSaver) {
        mutableStateOf(existing?.date ?: LocalDate.now())
    }
    var time by rememberSaveable(existing?.id, stateSaver = LocalTimeSaver) {
        mutableStateOf(existing?.time ?: LocalTime.now().withSecond(0).withNano(0))
    }
    var accent by rememberSaveable(existing?.id) { mutableIntStateOf(existing?.accent ?: 0) }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val locale = LocalConfiguration.current.locales[0]
    val timeFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (existing == null) R.string.edit_title_new else R.string.edit_title_edit,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_cancel),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onSave(title, date, time, accent) }) {
                        Text(stringResource(R.string.action_save))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            PreviewStrip(title = title, date = date, time = time, accent = accent)

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.edit_title_field_label)) },
                placeholder = { Text(stringResource(R.string.edit_title_field_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            FieldRow(
                label = stringResource(R.string.edit_field_date),
                value = LocalizedDateFormat.formatLongDate(date, locale),
                icon = Icons.Filled.DateRange,
                onClick = { showDatePicker = true },
            )
            Spacer(Modifier.height(12.dp))
            FieldRow(
                label = stringResource(R.string.edit_field_time),
                value = time.format(timeFormatter),
                icon = Icons.Filled.Schedule,
                onClick = { showTimePicker = true },
            )

            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.edit_accent_header),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            AccentPicker(selected = accent, onSelect = { accent = it })

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        val initialMillis = remember(date) {
            date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        val today = LocalDate.now()
                        // Guard against future dates (would otherwise clamp to 0 with no explanation).
                        date = if (picked.isAfter(today)) today else picked
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = DateFormat.is24HourFormat(context),
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val picked = LocalTime.of(state.hour, state.minute)
                    // If the date is today, a future time would clamp the count to 0 (sincePickedDhm
                    // treats a future instant as 0), so snap back to the current minute.
                    val now = LocalTime.now()
                    time = if (date == LocalDate.now() && picked.isAfter(now)) {
                        now.withSecond(0).withNano(0)
                    } else {
                        picked
                    }
                    showTimePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            text = { TimePicker(state = state) },
        )
    }
}

@Composable
private fun PreviewStrip(title: String, date: LocalDate, time: LocalTime, accent: Int) {
    val dhm = rememberElapsedDhm(date, time)
    val isNew = ElapsedTime.isNewBeginning(dhm.days, date, time)
    val onColor = if (isNew) Color.White else accentOrDefault(accent).onAccent
    val brush = if (isNew) NewBeginningBrush else accentBrush(accent)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
            .clip(MaterialTheme.shapes.large)
            .background(brush),
    ) {
        if (isNew) {
            Starburst(
                color = onColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(48.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = dhm.days.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold,
                    color = onColor,
                )
                Text(
                    text = stringResource(
                        if (isNew) R.string.card_new_beginning_label else R.string.card_days_since_label,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 2.sp,
                    color = onColor.copy(alpha = 0.88f),
                )
            }
            Text(
                text = title.ifBlank { stringResource(R.string.milestone_default_title) },
                style = MaterialTheme.typography.titleMedium,
                color = onColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FieldRow(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AccentPicker(selected: Int, onSelect: (Int) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MilestoneAccents.forEachIndexed { index, accent ->
            val isSelected = index == selected
            val label = stringResource(accent.labelRes)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentBrush(index))
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                        shape = CircleShape,
                    )
                    // Label every swatch so TalkBack announces each as a named, selectable radio option.
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(index) })
                    .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = accent.onAccent)
                }
            }
        }
    }
}
