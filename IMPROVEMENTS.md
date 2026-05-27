# DaysSince — Improvement Plan

This document tracks all known bugs, performance issues, UX gaps, test coverage
holes, and build hygiene tasks for the DaysSince project. Items are grouped by
priority and include the exact files/lines affected plus a concrete fix plan.

---

## How to use this document

- Work items are ordered **High → Low** priority within each section.
- Each item has a checkbox so progress can be tracked via commits/PRs.
- When an item is resolved, check the box and note the PR number in parentheses.
- New findings should be added here before a fix branch is opened.

---

## 🔴 Priority 1 — Bugs (fix before next release)

### BUG-1 · `ACTION_TIME_TICK` missing from `AndroidManifest.xml`

**File:** `app/src/main/AndroidManifest.xml`

**Problem:**
`BaseDaysSinceWidgetProvider.onReceive` handles `Intent.ACTION_TIME_TICK` but
neither widget receiver declares it in the manifest intent-filter. On API 26+,
`ACTION_TIME_TICK` is a protected broadcast that is only delivered to receivers
with an explicit manifest declaration. As a result the per-minute tick is
silently swallowed and the widget never updates from it.

**Fix:**
Add `<action android:name="android.intent.action.TIME_TICK" />` to both
`<receiver>` intent-filters in `AndroidManifest.xml`.

```xml
<!-- example — add to both DaysSinceWidgetProvider and
     DaysHoursMinutesSinceWidgetProvider receivers -->
<intent-filter>
    ...
    <action android:name="android.intent.action.TIME_TICK" />
</intent-filter>
```

**Test:** Add a Robolectric test that sends `ACTION_TIME_TICK` to the receiver
and asserts `onReceive` is called and `updateAll` fires.

- [ ] Fix implemented
- [ ] Test added

---

### BUG-2 · Fresh-install fallback date/time mismatch

**File:** `app/src/main/java/com/quasarapps/dayssince/SelectedStartDateTime.kt`
**Lines:** 37–41

**Problem:**
When no date is stored in prefs, `load()` falls back to `LocalDate.now()` but
`LocalTime.MIDNIGHT`. This means a brand-new user sees "0 days, X hours, Y
minutes" (non-zero hours/minutes) immediately on first launch, before they have
chosen anything. The initial state in `DaysSinceApp.kt` (lines 41–42) also uses
`LocalDate.now()` + `LocalTime.now()`, creating a brief mismatch until the
`LaunchedEffect` finishes loading prefs.

**Fix:**
Change the time fallback from `LocalTime.MIDNIGHT` to
`LocalTime.now().withSecond(0).withNano(0)` so the default timestamp matches
"right now" on first install, giving a consistent "0d 0h 0m" display.

```kotlin
// SelectedStartDateTime.kt — load()
val time = prefs.getString(KEY_TIME, null)
    ?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
    ?: LocalTime.now().withSecond(0).withNano(0)   // was: LocalTime.MIDNIGHT
```

**Test:** Extend `SelectedStartDateTimeTest` to inject a fixed `Clock` into
`load()` and assert that when no prefs exist the returned time equals the clock
instant (truncated to minutes).

- [ ] Fix implemented
- [ ] Test added

---

## 🟠 Priority 2 — Performance & Correctness

### PERF-1 · Tick timer not aligned to the clock-minute boundary

**File:** `app/src/main/java/com/quasarapps/dayssince/ui/DaysSinceApp.kt`
**Lines:** 46–51

**Problem:**
The `LaunchedEffect(Unit)` coroutine delays a fixed 60 000 ms on every
iteration. If the composable enters composition at second 59 of a minute, the
timer fires at second 59 of every subsequent minute — always one second before
the display should change. The displayed minutes value can lag real time by up
to 59 seconds.

**Fix:**
On each iteration, calculate the milliseconds remaining until the next whole
minute and sleep only that long.

```kotlin
LaunchedEffect(Unit) {
    while (true) {
        val now = LocalTime.now()
        val msToNextMinute = ((60 - now.second) * 1_000L) - now.nano / 1_000_000L
        delay(msToNextMinute.coerceAtLeast(100))
        // recompute displayed values
    }
}
```

- [ ] Fix implemented

---

### PERF-2 · SharedPreferences I/O on the main thread during widget updates

**Files:**
- `app/src/main/java/com/quasarapps/dayssince/widget/DaysSinceWidgetProvider.kt:23`
- `app/src/main/java/com/quasarapps/dayssince/widget/DaysHoursMinutesSinceWidgetProvider.kt:27`

**Problem:**
`buildRemoteViews` calls `SelectedStartDateTime.load(context)` which reads
`SharedPreferences` synchronously. `AppWidgetProvider.onUpdate` / `onReceive`
run on the main thread. The first read causes disk I/O on the main thread;
subsequent reads hit the in-memory cache but that cache is not guaranteed across
process restarts.

**Fix (short-term):** Call `Prefs.get(context)` once in
`Application.onCreate()` to warm the SharedPreferences cache before any widget
callback fires.

**Fix (long-term):** Migrate `Prefs` to Jetpack `DataStore` and read inside a
`GoAsync` / coroutine scope in the widget provider so I/O never blocks the main
thread.

- [ ] Short-term warmup added
- [ ] Long-term DataStore migration tracked (separate issue)

---

### PERF-3 · DST transition not handled in `sincePickedDhm`

**File:** `app/src/main/java/com/quasarapps/dayssince/DaysSince.kt`
**Related test file:** `app/src/test/java/com/quasarapps/dayssince/DaysSinceTest.kt`

**Problem:**
`sincePickedDhm` uses `ChronoUnit.MINUTES.between(pickedDateTime, now)` on
`LocalDateTime` values. `LocalDateTime` has no timezone, so when a DST
transition occurs (e.g., clocks spring forward 1 hour) a 23-hour wall-clock day
is counted as a full 24-hour day or vice-versa — the displayed days, hours, and
minutes can be off by one.

**Fix:**
Convert `pickedDateTime` and `now` to `ZonedDateTime` using the device
time-zone before computing the difference, or use
`Duration.between(pickedZdt, nowZdt).toMinutes()` so the JVM's DST-aware
arithmetic is used.

**Test to add:**
```
// Spring-forward (America/New_York, March 2025):
// picked = 2025-03-08T01:30, now = 2025-03-09T02:30 (wall clock 25h but
// only 24h of real elapsed time)
// Expected: 1 day, 0 hours, 0 minutes
```

- [ ] Fix implemented
- [ ] DST test cases added

---

## 🟡 Priority 3 — UX / UI

### UX-1 · `DatePickerDialog` accepts future dates silently

**File:** `app/src/main/java/com/quasarapps/dayssince/ui/NativePickers.kt`
**Lines:** 41–50

**Problem:**
No `maxDate` is set on the `DatePickerDialog`. A user can pick tomorrow, the app
clamps to 0, and the user sees "0 days 0 hours 0 minutes" with no explanation.

**Fix:**
Set `dialog.datePicker.maxDate = System.currentTimeMillis()` immediately after
the dialog is created so future dates cannot be selected.

```kotlin
DatePickerDialog(context, { _, y, m, d ->
    onDateSelected(LocalDate.of(y, m + 1, d))
}, initial.year, initial.monthValue - 1, initial.dayOfMonth)
    .also { it.datePicker.maxDate = System.currentTimeMillis() }
    .show()
```

- [ ] Fix implemented

---

### UX-2 · Time display ignores device locale (always 24-hour)

**File:** `app/src/main/java/com/quasarapps/dayssince/ui/DaysSinceApp.kt`
**Line:** 226

**Problem:**
`"%02d:%02d".format(time.hour, time.minute)` always produces a 24-hour string.
Users on US/UK/Australian locale devices expect a 12-hour format.

**Fix:**
Use `DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)` which automatically
respects the device locale:

```kotlin
val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
// ...
Text(text = time.format(timeFormatter))
```

- [ ] Fix implemented

---

### UX-3 · Magic 112 dp top padding is not inset-aware

**File:** `app/src/main/java/com/quasarapps/dayssince/ui/DaysSinceApp.kt`
**Line:** 72

**Problem:**
`padding(top = 112.dp)` is a hard-coded magic number. With edge-to-edge mode
enabled (`WindowCompat.setDecorFitsSystemWindows(window, false)` in
`Theme.kt:36`), this can be too small on devices with tall status bars or
notches, and wasteful on compact devices.

**Fix:**
Replace the magic padding with `Modifier.systemBarsPadding()` combined with
a semantic content padding:

```kotlin
Modifier
    .systemBarsPadding()
    .padding(horizontal = 24.dp, vertical = 32.dp)
```

- [ ] Fix implemented

---

### UX-4 · XML activity theme mismatch (Material Components vs Material 3)

**File:** `app/src/main/res/values/themes.xml`
**Line:** 3

**Problem:**
The activity XML theme parent is `Theme.MaterialComponents.DayNight.DarkActionBar`.
The Compose UI uses Material 3 (`MaterialTheme` from `androidx.compose.material3`).
The `DarkActionBar` parent adds a native action bar that briefly flashes during
activity launch before Compose renders.

**Fix:**
Change the parent theme to `Theme.Material3.DayNight.NoActionBar` (or
`Theme.AppCompat.DayNight.NoActionBar` if the Material3 XML theme artifact is
not already a dependency).

```xml
<style name="Theme.DaysSince" parent="Theme.Material3.DayNight.NoActionBar">
```

- [ ] Fix implemented

---

### UX-5 · Widget layouts missing accessibility content descriptions

**Files:**
- `app/src/main/res/layout/widget_days_since_1x1.xml`
- `app/src/main/res/layout/widget_days_hours_minutes_since_1x3.xml`

**Problem:**
The numeric `TextViews` have no `contentDescription`. A TalkBack user hears
only "42" instead of "42 days since January 1st 2026". The "DAYS", "HOURS",
"MIN" labels are visual only and not linked to their value views.

**Fix:**
Set `android:contentDescription` on the value text views at runtime in
`buildRemoteViews`, after the value is computed:

```kotlin
views.setContentDescription(
    R.id.widget_day_number,
    "$days days since ${EnglishDateFormat.formatOrdinalDate(pickedDate)}"
)
```

- [ ] Fix implemented

---

## 🟢 Priority 4 — Tests

### TEST-1 · No unit tests for `EnglishDateFormat`

**File:** `app/src/main/java/com/quasarapps/dayssince/EnglishDateFormat.kt`

**Problem:**
The ordinal-suffix logic (`1st`, `2nd`, `3rd`, `11th`, `12th`, `13th`, `21st`,
etc.) has several branches with well-known edge cases and no test coverage.

**Tests to add** (in a new `EnglishDateFormatTest.kt`):

| Input day | Expected suffix |
|-----------|----------------|
| 1         | st             |
| 2         | nd             |
| 3         | rd             |
| 4–10      | th             |
| 11        | th (exception) |
| 12        | th (exception) |
| 13        | th (exception) |
| 21        | st             |
| 22        | nd             |
| 23        | rd             |
| 31        | st             |

Also test `formatOrdinalDate` for a full formatted string round-trip.

- [ ] Tests added

---

### TEST-2 · Widget tests use fragile reflection

**Files:**
- `app/src/test/java/com/quasarapps/dayssince/DaysSinceWidgetTest.kt:22-25`
- `app/src/test/java/com/quasarapps/dayssince/DaysHoursMinutesSinceWidgetTest.kt:20-25`

**Problem:**
Tests access `buildRemoteViews` via `getDeclaredMethod(...).apply { isAccessible = true }`.
A rename refactor silently breaks all widget tests with a `NoSuchMethodException`
at runtime rather than a compile error.

**Fix:**
Mark `buildRemoteViews` as `internal` (with `@VisibleForTesting`) so it can be
called directly from tests in the same module without reflection. Alternatively,
test through `onUpdate` end-to-end using `ShadowAppWidgetManager`.

- [ ] Fix implemented

---

### TEST-3 · `SelectedStartDateTimeTest` does not verify the date fallback

**File:** `app/src/test/java/com/quasarapps/dayssince/SelectedStartDateTimeTest.kt`
**Line:** 21

**Problem:**
The test comment notes it is "hard to assert exact date without a clock." The
fix for BUG-2 above injects a `Clock` parameter into `load()`, which makes this
trivially testable.

**Test to add:**
Once BUG-2 is fixed, add a test that passes a fixed `Clock` to `load()` with
empty prefs and asserts the returned `LocalDate` and `LocalTime` match the
clock's instant (truncated to minutes).

- [ ] Tests added (depends on BUG-2 fix)

---

### TEST-4 · No tests for `WidgetScheduler` or `WidgetBroadcasts`

**Files:**
- `app/src/main/java/com/quasarapps/dayssince/widget/WidgetScheduler.kt`
- `app/src/main/java/com/quasarapps/dayssince/widget/WidgetBroadcasts.kt`

**Problem:**
Zero test coverage on the scheduling and broadcast helper classes.

**Tests to add** using Robolectric's `ShadowAlarmManager`:

- `scheduleInexactRepeating` registers exactly one alarm.
- Calling it twice does not register a second alarm (idempotent).
- `cancelRepeating` removes the alarm.
- `WidgetBroadcasts.requestUpdate` sends an explicit broadcast to both
  widget receiver classes.

- [ ] Tests added

---

## 🔵 Priority 5 — Build & Dependencies

### BUILD-1 · Kotlin and AGP are significantly outdated

**File:** `libs.versions.toml`
**Lines:** 2–5, 14

**Current versions:**
- Kotlin: `1.9.25` → upgrade to `2.1.x`
- AGP: `8.5.0` → upgrade to `8.9.x`
- Compose BOM: `2024.06.00` → upgrade to `2024.12.01` or later
- `activityCompose`: `1.9.0` → upgrade to `1.10.x`

**Migration note for Kotlin 2.x:**
Remove the manual `kotlinCompilerExtensionVersion = "1.5.15"` from
`app/build.gradle.kts` line 43. Kotlin 2.x uses the bundled Compose compiler
Gradle plugin instead — add it to `app/build.gradle.kts`:

```kotlin
plugins {
    ...
    alias(libs.plugins.compose.compiler)  // replaces manual extension version
}
```

And add to `libs.versions.toml`:
```toml
[plugins]
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] Kotlin upgraded to 2.1.x
- [ ] AGP upgraded to 8.9.x
- [ ] Compose BOM updated
- [ ] `activityCompose` updated
- [ ] Robolectric updated to 4.14.x

---

### BUILD-2 · Release build has minification disabled

**File:** `app/build.gradle.kts`
**Line:** 22

**Problem:**
`isMinifyEnabled = false` in the `release` build type. This bloats the release
APK and makes the code trivially reverse-engineered.

**Fix:**
Enable R8 and add a `proguard-rules.pro` with keep rules for the widget
providers and any reflection-accessed classes:

```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

Minimum `proguard-rules.pro` entries:
```
-keep class com.quasarapps.dayssince.widget.** { *; }
-keep class com.quasarapps.dayssince.MainActivity { *; }
```

- [ ] Minification enabled
- [ ] ProGuard rules verified (no runtime crashes in release build)

---

### BUILD-3 · Java source/target compatibility is overly conservative

**File:** `app/build.gradle.kts`
**Lines:** 30–34

**Problem:**
`sourceCompatibility = JavaVersion.VERSION_1_8` and `jvmTarget = "1.8"`. The
app targets `minSdk = 26` (Android 8.0) so Java 11 language features are
available without desugaring.

**Fix:**
```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlinOptions {
    jvmTarget = "11"
}
```

- [ ] Java 11 target set

---

## ⚪ Priority 6 — Security & Housekeeping

### SEC-1 · Backup configuration files are uncommitted templates

**Files:**
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`

**Problem:**
Both files are the auto-generated Android Studio templates with all rules
commented out. `AndroidManifest.xml` has `allowBackup="true"`, so by default
all `SharedPreferences` are included in Auto Backup and ADB backups. This is
acceptable for this app's data (just a date/time), but the files should be
explicitly configured rather than left as stale placeholders.

**Fix:**
Explicitly include only `dayssince_prefs` in backup:

```xml
<!-- backup_rules.xml -->
<full-backup-content>
    <include domain="sharedpref" path="dayssince_prefs.xml" />
</full-backup-content>
```

```xml
<!-- data_extraction_rules.xml -->
<data-extraction-rules>
    <cloud-backup>
        <include domain="sharedpref" path="dayssince_prefs.xml" />
    </cloud-backup>
    <device-transfer>
        <include domain="sharedpref" path="dayssince_prefs.xml" />
    </device-transfer>
</data-extraction-rules>
```

- [ ] Backup rules configured

---

### SEC-2 · Widget missing `previewImage` / `previewLayout`

**Files:**
- `app/src/main/res/xml/days_since_widget_info.xml`
- `app/src/main/res/xml/days_hours_minutes_since_widget_info.xml`

**Problem:**
Neither widget info file declares `android:previewImage` or
`android:previewLayout` (API 31+). The widget picker shows a blank grey square
on most launchers, giving a poor first impression.

**Fix:**
1. Create a dedicated preview drawable (or reuse the existing widget layout as
   the preview layout for API 31+):
   ```xml
   android:previewLayout="@layout/widget_days_since_1x1"
   ```
2. For older APIs, add a static `@drawable/widget_preview` PNG/XML.

- [ ] Preview layout declared (API 31+)
- [ ] Fallback preview drawable created

---

## Future / Stretch Goals

These items are from the README "future improvements" section. They are not bugs
but are tracked here for completeness.

| ID | Feature | Notes |
|----|---------|-------|
| FEAT-1 | Options screen (12/24 h, show h/m breakdown) | Requires new settings Activity/screen |
| FEAT-2 | WorkManager instead of AlarmManager | Simpler on Android 12+, respects battery policies |
| FEAT-3 | Encrypted storage (AndroidX Security Crypto) | Low sensitivity data; low urgency |
| FEAT-4 | Dynamic colors / Material You widget theming | Use `android:theme` on widget layout + `system_accent1_*` colors |

---

*Last updated: 2026-05-26*
