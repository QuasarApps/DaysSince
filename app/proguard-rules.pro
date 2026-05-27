# Keep widget provider classes — they are referenced by name in AndroidManifest.xml
# and must survive R8 shrinking.
-keep class com.quasarapps.dayssince.widget.DaysSinceWidgetProvider { *; }
-keep class com.quasarapps.dayssince.widget.DaysHoursMinutesSinceWidgetProvider { *; }

# Keep MainActivity — it is the launcher activity declared in the manifest.
-keep class com.quasarapps.dayssince.MainActivity { *; }

# Preserve source file names and line numbers in stack traces for easier debugging.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
