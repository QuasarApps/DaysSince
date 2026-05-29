# Days Since

A small Android app for tracking the days, hours, and minutes since the
milestones that matter to you — sober time, last gym session, an anniversary,
whatever. Each milestone gets a bold gradient card in the app and an optional
home-screen widget.

- Minimum Android version: **8.0 (API 26)**
- Targets Android: **15 (API 35)**
- Fully offline. No accounts, no network calls, no permissions.

---

## Features

- **Multiple milestones.** Add as many as you like; each gets its own colour
  accent (or "Dynamic" to inherit your Material You scheme).
- **Detail screen** with a full-bleed gradient hero, count-up animation, and
  a days / hours / minutes breakdown.
- **Two home-screen widgets:**
    - *Days Since* — 1×1 widget showing whole days.
    - *Days · Hours · Minutes* — wide widget with the full breakdown.
- **Per-widget configuration** — when you place a widget, a picker lets you
  bind it to a specific milestone and optionally render it with a transparent
  background (just the number, floating on your wallpaper).
- **Material You** dynamic colour on Android 12+, with a curated brand
  fallback on older devices. Follows the system light/dark setting.
- **Accessibility:** TalkBack descriptions on widget content, respects the
  system reduce-motion setting, tabular figures so digits don't jump.
- **No network, no permissions.** Data is stored in app-private DataStore
  preferences and included in Auto Backup so it survives a reinstall.

---

## How the elapsed time is calculated

The math lives in
[`app/src/main/java/com/quasarapps/dayssince/DaysSince.kt`](app/src/main/java/com/quasarapps/dayssince/DaysSince.kt).

- Computes `ZonedDateTime.now() - picked` in the device time zone, so DST
  transitions (spring-forward / fall-back) are accounted for rather than
  always assuming a 24-hour day.
- "Whole days" means the value increments every 24 elapsed hours since the
  picked timestamp. 23h 59m still shows as **0 days**.
- If the picked timestamp is in the future the value clamps to `0d 0h 0m`
  (and the date picker enforces `max = today` to make this hard to trigger).

The behaviour is unit-tested in `DaysSinceTest`, including UTC, non-UTC
zones (Pacific/Kiritimati), and both DST transition directions in
America/New_York.

---

## Architecture

```
ui/                         Compose screens + theme
  home/HomeScreen           staggered grid of milestone cards
  detail/DetailScreen       gradient hero + d/h/m breakdown
  edit/EditMilestoneScreen  Material 3 date + time pickers, accent picker
  components/               CountUpNumber, rememberElapsedDhm tick
  theme/                    Material You + brand fallback + accent gradients

data/
  Milestone                 id, title, date, time, accent, createdAt
  MilestoneJson             JSON (de)serialisation for the milestone list
  MilestonesRepository      DataStore-backed CRUD + widget bindings

widget/
  WidgetConfigActivity      milestone picker shown when a widget is placed
  glance/DaysWidget         1×1 Glance widget
  glance/DaysHoursMinutes…  wide Glance widget
  glance/WidgetUi           shared Glance scaffold, colours, and click target
  MilestoneWidgets          updateAll() helper called after milestone changes

util/EnglishDateFormat      "1st of January 2026" formatting
```

Persistence is a single [Preferences DataStore](app/src/main/java/com/quasarapps/dayssince/data/MilestonesRepository.kt)
named `dayssince_store`, with two keys:

- `milestones_json` — JSON array of `Milestone` objects.
- `widget_bindings_json` — JSON map of `appWidgetId → {milestoneId, transparent}`.

There is a one-time migration that folds the old single-counter
SharedPreferences (`dayssince_prefs`) into a first `Milestone` row, so users
of an earlier internal build keep their counter on upgrade. It runs from
`DaysSinceApplication.onCreate` on every cold start (idempotent — a
`legacy_migrated` flag in DataStore short-circuits subsequent calls), so
the migrated state is in place before either the launcher activity or the
widget config picker reads it.

---

## Widgets

Both widgets are written with [Jetpack Glance](https://developer.android.com/develop/ui/compose/glance).
They share:

- The milestone's accent gradient as the widget background, or a fully
  transparent background if the user opts in during configuration.
- A `previewLayout` so the widget picker shows a real-looking preview
  instead of a grey square on Android 12+.
- A `contentDescription` so TalkBack reads e.g.
  *"365 days since Sober, 1st of January 2026"*.
- A tap target that opens `MainActivity` deep-linked to the bound
  milestone's detail screen.

Update cadence is driven by `appwidget-provider` `updatePeriodMillis`
(6 h for the days-only widget, 30 min for the d/h/m widget — the platform
minimum) plus an explicit `updateAll()` after every milestone or binding
change. The minutes value on the wide widget will lag real time by up to
30 minutes between system-driven updates; the app itself ticks once per
minute aligned to the minute boundary while it's in the foreground.

---

## Build & run

### Android Studio

1. Open the project root in Android Studio Iguana or newer.
2. Allow Gradle sync.
3. Run the `app` configuration on an emulator or device.

### Command line (Windows / PowerShell)

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

On macOS / Linux use the wrapper script instead, e.g. `./gradlew :app:assembleDebug`.

### Tests (Windows / PowerShell)

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

On macOS / Linux: `./gradlew :app:testDebugUnitTest`.

Notable test files:

- `DaysSinceTest` — date math, future-clamp, DST transitions.
- `EnglishDateFormatTest` — ordinal-suffix edge cases (11/12/13, 21st, 31st).
- `MilestoneJsonTest` — round-trip and defensive decoding of stored JSON.
- `MilestonesRepositoryBindingsTest` — widget binding serialisation,
  including the legacy bare-string format from before the transparent flag
  was added.
- `SelectedStartDateTimeTest` — legacy single-counter fallback handling.
- `PrefsTest` — SharedPreferences round-trip.

CI assembles the debug variant and runs the JVM tests on every push and
pull request — see [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

---

## Placing a widget

1. Long-press an empty spot on your launcher.
2. Choose **Widgets** and find **Days Since**.
3. Drag the widget onto the home screen.
4. A picker opens — choose which milestone the widget should track, and
   optionally enable the transparent background.
5. Tap the milestone to confirm.

The same flow works for both widget sizes. Each placement can track a
different milestone.

---

## Privacy

Days Since stores all data locally in the app's private storage. It does
not request any runtime permissions, makes no network requests, and
includes no analytics or crash reporting. The stored milestones are
included in Android Auto Backup so they survive an uninstall/reinstall
on the same Google account.
