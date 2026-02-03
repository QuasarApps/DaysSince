package com.quasarapps.dayssince.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
                    dialog.show()
                }
            ) {
                Text("Pick Date")
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
                        true
                    )
                    dialog.show()
                }
            ) {
                Text("Pick Time")
            }
        }
    }
}
