package com.quasarapps.pulsar.widget.glance

import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the widget's font-size step functions (Glance has no text auto-size, so these
 * shrink the number as the day-string gets longer to keep it fitting the small footprints).
 * Pure String -> TextUnit, so no Compose runtime / emulator is needed.
 */
class WidgetUiTest {

    @Test
    fun daysWidgetFontSize_shrinksAsTheDayStringGetsLonger() {
        assertEquals(30.sp, daysWidgetFontSize("7"))     // 1 digit
        assertEquals(30.sp, daysWidgetFontSize("42"))    // 2 digits
        assertEquals(26.sp, daysWidgetFontSize("365"))   // 3 digits
        assertEquals(20.sp, daysWidgetFontSize("1234"))  // 4 digits
        assertEquals(16.sp, daysWidgetFontSize("9999+")) // capped
    }

    @Test
    fun dhmStatFontSize_shrinksAsTheDayStringGetsLonger() {
        assertEquals(22.sp, dhmStatFontSize("7"))
        assertEquals(22.sp, dhmStatFontSize("42"))
        assertEquals(20.sp, dhmStatFontSize("365"))
        assertEquals(17.sp, dhmStatFontSize("1234"))
        assertEquals(14.sp, dhmStatFontSize("9999+"))
    }
}
