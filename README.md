# DaysSince

DaysSince is a small Android app + home screen widget that displays the **number of whole days since
a user-picked date and time**.

- The **app** lets the user pick a date/time using the platform’s native `DatePickerDialog`
  and `TimePickerDialog`.
- The **widget** shows the same “days since” value on the launcher, independent of any activity.

> “Whole days” means the value increments every 24 hours since the picked timestamp. For example,
> 23h 59m since the picked date/time still shows **0**.

---

## Features

- **Compose UI** (Material 3) for the main screen.
- **Native date/time pickers** using Android dialogs.
- **Home screen widget** (AppWidget) that:
    - displays the same computed value as the app
    - updates immediately when the user updates the picked date/time
    - updates periodically in the background (roughly hourly)
    - refreshes on device time/timezone changes
    - launches the app when tapped
- **Persistence** across app restarts via `SharedPreferences` (`MODE_PRIVATE`).

---

## How the “days since” number is calculated

The computation lives in:

- `app/src/main/java/com/quasarapps/dayssince/DaysSince.kt`

Contract:

- Input: `pickedDate: LocalDate`, `pickedTime: LocalTime`
- Output: `Long` (whole days)
- Behavior:
    - Uses the device time zone (or a provided zone in tests)
    - `days = floor( (now - pickedDateTime) / 24h )`
    - If the picked timestamp is in the future, the displayed value clamps to `0`

---

## Persistence (storage)

The selected date/time are stored as ISO strings in app-private prefs:

- File: `SharedPreferences("dayssince_prefs")`
- Keys:
    - `selected_date` (example: `2026-01-01`)
    - `selected_time` (example: `00:00`)

Storage wrapper:

- `app/src/main/java/com/quasarapps/dayssince/Prefs.kt`

This is “minimal and secure” in the sense that it is:

- **app-private** (`MODE_PRIVATE` in the app sandbox)
- simple and reliable

If you need encryption at rest later, you can swap this to AndroidX Security Crypto.

---

## Widget behavior

### What it displays

The widget displays the same computed whole-days-since value as the app.

### When it updates

The widget updates from multiple event sources (best effort, per Android power management):

1. **Immediately** when the user changes date/time in the app.
    - The app persists to prefs then broadcasts an update request.
2. **On system time changes**
    - Time set / timezone changed broadcasts trigger a refresh.
3. **Background periodic refresh**
    - An **inexact repeating** alarm is scheduled to refresh roughly once per hour.

Implementation:

- Provider: `app/src/main/java/com/quasarapps/dayssince/widget/DayOfMonthAppWidgetProvider.kt`
- Widget info: `app/src/main/res/xml/day_of_month_widget_info.xml`
- Layout: `app/src/main/res/layout/widget_day_of_month.xml`

### Click behavior

Tapping the widget launches `MainActivity`.

---

## Project structure

Key files:

- App entry:
  - `app/src/main/java/com/quasarapps/dayssince/MainActivity.kt`

- Compose UI:
  - `app/src/main/java/com/quasarapps/dayssince/ui/DaysSinceApp.kt`
  - `app/src/main/java/com/quasarapps/dayssince/ui/DaysSinceWidget.kt`
  - `app/src/main/java/com/quasarapps/dayssince/ui/NativePickers.kt`

- Core logic:
  - `app/src/main/java/com/quasarapps/dayssince/DaysSince.kt`

- Widget:
  - `app/src/main/java/com/quasarapps/dayssince/widget/DayOfMonthAppWidgetProvider.kt`

---

## Build & run

### Android Studio

1. Open the project folder (`DaysSince`) in Android Studio.
2. Allow Gradle sync.
3. Run the `app` configuration on an emulator or device.

### Command line (Windows / PowerShell)

From repo root:

```powershell
cd C:\Users\quasa\StudioProjects\DaysSince
.\gradlew.bat :app:assembleDebug
```

Install on a connected device/emulator:

```powershell
.\gradlew.bat :app:installDebug
```

---

## Add the widget (launcher)

1. Install and run the app once (helps some launchers discover widgets).
2. Long-press the home screen.
3. Choose **Widgets**.
4. Find the **DaysSince** widget and add it.

The widget is configured to request a **1x1** footprint via `minWidth`/`minHeight`.
Actual sizing can vary slightly by launcher.

---

## Testing

### Unit tests (JVM)

This project uses JUnit 4 and Robolectric for JVM tests.

Run:

```powershell
cd C:\Users\quasa\StudioProjects\DaysSince
.\gradlew.bat :app:testDebugUnitTest
```

Notable tests:

- `DaysSinceTest` covers date/time boundary cases (24h boundaries, leap day, etc.)
- `PrefsTest` covers persistence basics (overwrite, clear/remove, unicode)
- `DayOfMonthAppWidgetProviderTest` verifies widget RemoteViews render numeric output and handle
  invalid/missing prefs

> Note: Robolectric has limitations around inflating `<appwidget-provider>` XML directly; widget
> tests avoid those paths and instead validate `RemoteViews` generation/rendering.

---

## Troubleshooting

### The widget doesn’t update immediately

- Some launchers cache widget views.
- Try removing the widget and adding it again.
- Ensure battery optimizations aren’t extremely restrictive.

### Time changes don’t reflect instantly

- The app UI updates while visible (it “ticks” periodically).
- The widget listens to time/timezone change broadcasts, but delivery varies by Android version and
  OEM. The hourly refresh also helps keep it up to date.

---

## Notes / future improvements

- Add an options screen (choose 12/24 hour display, show hours/minutes since, etc.).
- Use WorkManager instead of AlarmManager (simpler scheduling semantics, but its own constraints).
- Add encrypted local storage for the picked date/time.
- Add better widget theming (rounded corners, dynamic colors, etc.).

