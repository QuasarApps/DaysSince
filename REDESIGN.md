# DaysSince — UI/UX Redesign Plan

A full visual and UX redesign of the DaysSince app and its home-screen widgets.
This document is the design spec and phased build plan. It is intentionally
separate from [IMPROVEMENTS.md](IMPROVEMENTS.md) (bugs / tech-debt), though the
redesign incidentally resolves several UX items tracked there.

*Last updated: 2026-05-26*

---

## Locked decisions

These were agreed before writing the plan:

| Decision | Choice |
|----------|--------|
| Visual style | **Bold hero / gradient** |
| App scope | **Multi-counter** (a list of milestones, not a single date) |
| Theming | **Material You dynamic color** + curated custom fallback |
| Widget tech | **Migrate to Glance** (configurable, dynamic color, per-milestone) |
| Hero number font | **Custom downloadable display font** (e.g. Space Grotesk / Figtree) |

---

## 1. Design vision — "living gradient"

The signature idea: **the gradient is generated from the Material You color
scheme, not hardcoded.** Google's guidance pushes *surface layering* over flat
colored backgrounds because arbitrary gradients fight dynamic color and break
contrast. We sidestep that by deriving every gradient from tonal color roles
(`primary → tertiary`, or `primaryContainer → secondaryContainer`) plus a
legibility scrim. The result is bold and dramatic, but recolors with the user's
wallpaper and stays WCAG-AA legible on any of them.

Each milestone owns an **accent** (picked from a curated tonal set) that drives
its card gradient *and* its widget — so the multi-counter home reads as a
colorful, personal collection.

```
HOME (staggered cards)            DETAIL (bold hero)
╔════════════════════════╗        ╔════════════════════════╗
║  Days Since        ⚙   ║        ║░░░ accent gradient ░░░░║
║ ┌────────┐ ┌─────────┐ ║        ║  ‹ back            ⋮   ║
║ │▓ 365 ▓ │ │░  88  ░ │ ║        ║                        ║
║ │ Sober  │ │ Last gym│ ║        ║         365            ║ count-up
║ │ days   │ │ days    │ ║        ║         DAYS           ║
║ └────────┘ └─────────┘ ║        ║   ┌────┐  ┌────┐       ║
║ ┌─────────────────────┐║        ║   │ 8h │  │42m │       ║ glass
║ │▒  1,204  days     ▒ │║        ║   └────┘  └────┘       ║
║ │  Together           │║        ║   since 1 Jan 2026     ║
║ └─────────────────────┘║        ║   at 09:30             ║
║                    (＋) ║        ║   [ Edit ] [ + Widget ]║
╚════════════════════════╝        ╚════════════════════════╝
```

## 2. Color & theming

- **Dynamic color (Android 12+):** `dynamicDarkColorScheme(context)` /
  `dynamicLightColorScheme(context)`, guarded by `Build.VERSION.SDK_INT >= S`.
- **Custom fallback palette** for API 26–30 and Compose previews — a designed
  scheme so older devices and screenshots still look intentional (current code
  only sets primary/secondary/tertiary and leans on M3 defaults).
- **Gradient brushes** built from scheme roles in one place (`Gradients.kt`): a
  hero `verticalGradient` / `linearGradient` + a soft radial "glow" behind the
  number, always paired with an `onX` text role and a bottom scrim.
- **Follow system light/dark** (remove the hardcoded `darkTheme = true`), with a
  polished dark default.
- Per guidance, balance dynamic with brand identity: dynamic drives surfaces and
  gradients, but key CTAs stay on a consistent accent so the app doesn't
  dissolve on odd wallpapers.

## 3. Typography & motion

- Adopt the **M3 Expressive emphasized type scale**; hero number in an oversized
  display style with **tabular (lining) figures** so ticking digits don't reflow.
- **Custom display font** via downloadable Google Fonts
  (`androidx.compose…ui-text-google-fonts`), used for the hero number and titles
  only; body stays on the platform font. No bundled font asset = no APK bloat.
- **Motion (Expressive, spring-based):** count-up animation 0→value on open,
  `AnimatedContent` digit roll on the minute tick, container-transform when a
  card opens to detail, subtle gradient drift. All gated by the system
  **reduce-motion** setting.

## 4. Screens (multi-counter information architecture)

1. **Home / list** — collapsing large top bar, staggered grid of gradient
   milestone cards, Expressive FAB to add, and a friendly **empty state**
   ("Add your first milestone").
2. **Add / edit** (bottom sheet) — title field, **Material 3
   `DatePicker` / `TimePicker`** (retires the old `android.app` native dialogs in
   `NativePickers.kt`), accent swatch row, optional icon. `maxDate = now` so
   future dates are impossible (fixes the silent-clamp UX bug, UX-1).
3. **Detail** — the full-bleed gradient hero: giant count-up, d/h/m glass cards
   (translucent tonal surface + hairline border; real backdrop blur via
   `RenderEffect` on API 31+), "since … at …", edit/delete, and "+ Add widget".

## 5. Widgets (redesigned with Glance)

- **Migrate RemoteViews → Glance.** Current flat `#66000000` non-rounded
  rectangles get replaced with Compose-based widgets that gain dynamic color and
  the look launchers expect in 2026.
- **Configurable** (required by multi-counter): on placement, a config activity
  (`android:configure`) lets the user pick *which* milestone the widget tracks;
  bind `appWidgetId → milestoneId` in DataStore. Show a zero-state until
  configured.
- **Visual:** `GlanceTheme` dynamic colors, `GlanceTheme.colors.widgetBackground`,
  system corner radius (`system_app_widget_background_radius`), the milestone's
  accent gradient, big number.
- **Quality bar:** `targetCellWidth/Height` + `minWidth/Height`, `resizeMode`,
  **`previewLayout`** (no more blank grey square in the picker),
  `contentDescription` for TalkBack (fixes UX-5), one-tap → that milestone's
  detail.
- **Sizes:** keep 1×1 (days) and a wide d/h/m; add a medium "title + number" card.

## 6. Architecture & data (needed to support multiple counters)

The current app is a composable plus `SharedPreferences` with no architecture.
Multi-counter warrants:

- **Room**: `Milestone(id, title, startEpoch, accent, icon, createdAt)` + DAO —
  the right tool for a CRUD list (vs DataStore, which suits single values).
- **DataStore**: widget binding (`appWidgetId → milestoneId`) + app prefs.
- **Repository + ViewModel (StateFlow)** + **Navigation-Compose**.
- **One-time migration**: fold the existing stored date/time into a first
  `Milestone` row so current users keep their counter.

## 7. Toolchain prerequisites (Phase 0)

Needed for Glance, latest Material 3, and Expressive APIs. Current stack is
Kotlin 1.9 / AGP 8.5 / Compose BOM 2024.06.

- Kotlin **2.2.x** + the **Compose Compiler Gradle plugin** (replaces the manual
  `kotlinCompilerExtensionVersion`), AGP **8.7+ / 9.0.1**, compileSdk/targetSdk
  **36**, Compose BOM **2026.04.01 / 2026.05.00**.
- Add: `glance-appwidget`, `navigation-compose`, `room` (+ KSP),
  `datastore-preferences`, `material-icons-extended`, `ui-text-google-fonts`.
- Fix XML theme → `Theme.Material3.DayNight.NoActionBar` (kills the launch
  action-bar flash, UX-4), `enableEdgeToEdge()` + `systemBarsPadding()` (replaces
  the magic `112.dp`, UX-3).
- M3 Expressive components / shape-morph are partly alpha — adopt the **stable
  subset**; treat fancy shape morphing as enhancement, not a dependency.

## 8. Accessibility & quality bar

WCAG-AA contrast on every gradient (tonal pairing + scrim), honor font scaling
and reduce-motion, TalkBack descriptions on app and widgets, tabular figures,
48dp touch targets.

## 9. Phased delivery

| Phase | Outcome | Risk |
|-------|---------|------|
| **0 · Foundation** | Toolchain bump, dynamic-color theme + fallback, type/shape/gradient system, theme + edge-to-edge fixes | Low |
| **1 · Data** | Room + DataStore + ViewModel + repository, migrate existing counter | Low |
| **2 · App UI** | Navigation, home grid, add/edit (M3 pickers), detail hero, motion, empty state | Med |
| **3 · Widgets** | Glance migration, configurable per-milestone, dynamic color, previews, a11y | **Highest** |
| **4 · Polish** | Contrast/a11y pass, app-icon refresh, widget preview art, screenshot tests, on-device run | Low |

## 10. Key tradeoffs

- **Bold gradient vs. dynamic color** → solved by scheme-derived gradients +
  scrims; very saturated looks still need a per-wallpaper contrast check.
- **Multi-counter is a genuine build** (Room, nav, Glance config), not a reskin —
  hence the phasing.
- **Glance is the biggest lift** but was chosen for longevity over restyling the
  existing RemoteViews.

## 11. UX items from IMPROVEMENTS.md resolved as a side effect

- **UX-1** future dates → Material 3 `DatePicker` with `maxDate = now`.
- **UX-2** locale time format → localized formatter in the new summary.
- **UX-3** magic 112dp padding → `enableEdgeToEdge()` + `systemBarsPadding()`.
- **UX-4** XML theme mismatch → `Theme.Material3.DayNight.NoActionBar`.
- **UX-5** widget a11y → `contentDescription` on Glance widgets.

## References

- [Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Compose Material 3 releases](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [Build UI with Glance](https://developer.android.com/develop/ui/compose/glance/build-ui)
- [Implement a Glance theme](https://developer.android.com/develop/ui/compose/glance/theme)
- [Glance releases](https://developer.android.com/jetpack/androidx/releases/glance)
- [Widget quality guidelines](https://developer.android.com/docs/quality-guidelines/widget-quality)
- [App widgets overview](https://developer.android.com/develop/ui/views/appwidgets/overview)
- [Use a Compose Bill of Materials](https://developer.android.com/develop/ui/compose/bom)
- [What's new in the Jetpack Compose April '26 release](https://developer.android.com/blog/posts/whats-new-in-the-jetpack-compose-april-26-release)
- [Android Gradle plugin 9.0 release notes](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
