# Keep the Glance widget receivers, their GlanceAppWidget subclasses, and the
# widget configuration activity — all referenced by name from AndroidManifest.xml
# and must survive R8 shrinking. A package wildcard is the most robust form
# because it survives future renames inside the widget layer.
-keep class com.quasarapps.dayssince.widget.** { *; }

# Keep MainActivity — it is the launcher activity declared in the manifest.
-keep class com.quasarapps.dayssince.MainActivity { *; }

# Preserve source file names and line numbers in stack traces for easier debugging.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
