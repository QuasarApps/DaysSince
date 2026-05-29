# DaysSince — Improvement plan (status as of 1.0.0)

This was the original pre-redesign punch list. Most items shipped during the
multi-counter / Glance redesign and the pre-publish cleanup; the remainder are
either superseded by the new architecture or deferred deliberately.

The table below is the source of truth. The original detail sections that
followed are kept verbatim further down for archival reasons.

| ID | Title | Status | Notes |
|----|-------|--------|-------|
| BUG-1 | `ACTION_TIME_TICK` missing from manifest | N/A | The old `BaseDaysSinceWidgetProvider` was replaced by Glance; the TIME_TICK code path no longer exists. Per-minute widget freshness is a separate open question (see "Deferred" below). |
| BUG-2 | Fresh-install fallback date/time mismatch | Resolved | `EditMilestoneScreen` now uses `LocalTime.now().withSecond(0).withNano(0)` for new milestones (commit e8b834a). Legacy `SelectedStartDateTime.load` is now only used during one-time migration and its midnight fallback there is correct behaviour. |
| PERF-1 | Tick timer not aligned to clock-minute | Resolved | `rememberElapsedDhm` sleeps until the next minute boundary (commit eca4bd0). |
| PERF-2 | Prefs I/O on main thread in widget update | Resolved (short-term) | `DaysSinceApplication.onCreate` warms the cache off the main thread. Milestone data itself now lives in DataStore, which is suspending. |
| PERF-3 | DST not handled in `sincePickedDhm` | Resolved | `DaysSince` uses `ZonedDateTime` and covers both transitions in tests (commit eca4bd0). |
| UX-1 | Date picker accepts future dates | Resolved | `EditMilestoneScreen` clamps picked date to `today` (commit 2f0265d). |
| UX-2 | Time display ignores locale | Resolved | All time formatting uses `DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)`. |
| UX-3 | Magic 112dp top padding | Resolved | Scaffolds use `WindowInsets.safeDrawing` / `systemBarsPadding()`. |
| UX-4 | XML theme mismatch | Resolved | Activity theme is `Theme.Material3.DayNight.NoActionBar`. |
| UX-5 | Widget a11y missing content descriptions | Resolved | `WidgetScaffold` sets `contentDescription` on both widget sizes. |
| TEST-1 | No `EnglishDateFormat` tests | Resolved | `EnglishDateFormatTest` covers ordinal-suffix and locale handling (this commit's branch). |
| TEST-2 | Widget tests use fragile reflection | N/A | Glance widgets are tested through their composable surface; the old reflection helper is gone. |
| TEST-3 | `SelectedStartDateTime` date-fallback test | N/A | Superseded — the legacy load path is now only the migration entry point. |
| TEST-4 | No `WidgetScheduler` / `WidgetBroadcasts` tests | N/A | Those classes were removed during the Glance migration. Glance has its own scheduling. |
| BUILD-1 | Kotlin/AGP outdated | **Deferred** | Kotlin 1.9.25 + AGP 8.5 + Compose Compiler 1.5.15 still build and ship. Upgrade is risky overnight; leaving for a targeted PR. |
| BUILD-2 | Release minification disabled | Resolved | R8 + resource shrinking enabled with explicit keep rules for the widget package. |
| BUILD-3 | Java target overly conservative | Resolved | `sourceCompatibility = VERSION_11`, `jvmTarget = "11"`. |
| SEC-1 | Backup rules are stale templates | Resolved | `backup_rules.xml` + `data_extraction_rules.xml` explicitly include `dayssince_prefs.xml` (legacy) and the DataStore file. |
| SEC-2 | Widget missing preview | Resolved | Both widgets declare `previewLayout` with realistic sample data. |

---

## Deferred — known limitations for 1.0.0

These are real, but were knowingly shipped:

- **DHM widget minutes lag up to ~30 min.** The platform minimum for
  `updatePeriodMillis` is 30 minutes and there is no manifest-registered
  TIME_TICK receiver (TIME_TICK is only delivered to runtime-registered
  receivers post-Oreo). Per-minute freshness would require either a
  foreground service or a recurring AlarmManager pump, both of which add
  battery cost. Tracking for a future release.
- **Kotlin / AGP / Compose BOM upgrade** (BUILD-1). Builds cleanly today;
  worth a dedicated PR rather than a pre-publish scramble.
- **Adaptive icon foreground is opaque** — the foreground PNG bakes in its
  own dark background, so the launcher can't separate fg/bg for parallax
  or Material You themed-icon tinting. Visually fine on every launcher,
  but a proper transparent foreground + monochrome variant would be nicer.
  Out of scope for an overnight code change because it needs real icon
  source assets.

---

The original per-item detail (problem / fix / test snippets) was removed from
this file along with the redesign — the git history at commit `3ad360b` and
its descendants has the full context if you need to dig further.
