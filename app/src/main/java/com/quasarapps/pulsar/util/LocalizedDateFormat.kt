package com.quasarapps.pulsar.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Small, composable-independent date formatting helpers. */
object LocalizedDateFormat {

    /**
     * Formats a date in the long, locale-appropriate style — its own word order, month name,
     * separators and numerals (e.g. `January 1, 2026` en, `1 de enero de 2026` es, `١ يناير ٢٠٢٦` ar).
     * Pass the active locale (from the resource Configuration) so per-app language is honored.
     */
    fun formatLongDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
}
