package com.quasarapps.pulsar.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Small, composable-independent date formatting helpers.
 */
object LocalizedDateFormat {

    /**
     * Formats a date in the long, locale-appropriate style for [locale].
     *
     * Delegates to [DateTimeFormatter.ofLocalizedDate] with [FormatStyle.LONG], so each locale gets
     * its own word order, month name, separators and numerals — e.g. `January 1, 2026` (en),
     * `1 de enero de 2026` (es), `1 января 2026 г.` (ru), `١ يناير ٢٠٢٦` (ar). Callers must pass the
     * active locale (from the resource [android.content.res.Configuration]) so per-app language
     * selections are honored.
     */
    fun formatLongDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
}
