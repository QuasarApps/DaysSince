package com.quasarapps.dayssince.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quasarapps.dayssince.ui.theme.DaysSinceTheme
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun NativePickers(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    onSelectedDateChange: (LocalDate) -> Unit,
    onSelectedTimeChange: (LocalTime) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val dialog = DatePickerDialog(
                        context,
                        { _, year, monthZeroBased, dayOfMonth ->
                            onSelectedDateChange(LocalDate.of(year, monthZeroBased + 1, dayOfMonth))
                        },
                        selectedDate.year,
                        selectedDate.monthValue - 1,
                        selectedDate.dayOfMonth
                    )
                    // Prevent picking future dates — the app clamps to 0 but shows no explanation
                    dialog.datePicker.maxDate = System.currentTimeMillis()
                    dialog.show()
                }
            ) {
                Text(
                    text = "Pick Date",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Button(
                onClick = {
                    val dialog = TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            onSelectedTimeChange(LocalTime.of(hourOfDay, minute))
                        },
                        selectedTime.hour,
                        selectedTime.minute,
                        DateFormat.is24HourFormat(context) // respect device locale
                    )
                    dialog.show()
                }
            ) {
                Text(
                    text = "Pick Time",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

@Preview(name = "NativePickers - Dark", showBackground = true)
@Composable
private fun PreviewNativePickersDark() {
    DaysSinceTheme(darkTheme = true) {
        NativePickers(
            selectedDate = LocalDate.of(2025, 1, 1),
            selectedTime = LocalTime.of(9, 30),
            onSelectedDateChange = {},
            onSelectedTimeChange = {}
        )
    }
}

@Preview(name = "NativePickers - Light", showBackground = true)
@Composable
private fun PreviewNativePickersLight() {
    DaysSinceTheme(darkTheme = false) {
        NativePickers(
            selectedDate = LocalDate.of(2025, 1, 1),
            selectedTime = LocalTime.of(9, 30),
            onSelectedDateChange = {},
            onSelectedTimeChange = {}
        )
    }
}
