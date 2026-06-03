# Keep the Glance widget receivers, their GlanceAppWidget subclasses, and the
# widget configuration activity — all referenced by name from AndroidManifest.xml
# and must survive R8 shrinking. A package wildcard is the most robust form
# because it survives future renames inside the widget layer.
-keep class com.quasarapps.dayssince.widget.** { *; }

# Keep MainActivity — it is the launcher activity declared in the manifest.
-keep class com.quasarapps.dayssince.MainActivity { *; }

# WorkManager's default WorkerFactory instantiates workers reflectively by class name, so the
# worker class and its (Context, WorkerParameters) constructor must survive R8. It also falls under
# the broad widget.** rule above, but declare it explicitly so narrowing that rule can't silently
# break the periodic widget refresh in release builds.
-keep class com.quasarapps.dayssince.widget.WidgetRefreshWorker { <init>(...); }

# Preserve source file names and line numbers in stack traces for easier debugging.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
