# Pulsar — Improvement Roadmap

A sequenced plan for the improvements surfaced by the comprehensive code review. Items are
grouped into phases that each map to one or two focused PRs off `develop`. Phases are ordered
by value and risk: correctness first, then polish, then internal health, then features.

**Effort:** S = a few hours · M = ~1 day · L = multi-day
**Risk:** 🟢 low · 🟡 medium · 🔴 high

---

## Phase 0 — Correctness & quick wins ✅ (in progress)

Ship-blocking fixes for the next patch release. All low-risk, all with test coverage.

| # | Item | Effort | Risk | Status |
|---|------|--------|------|--------|
| 1 | Blank-title default → `R.string.milestone_default_title` (stop persisting the hardcoded English "Milestone") | S | 🟢 | this PR |
| 2 | Prune widget bindings when a widget is removed (`onDeleted` → `unbindWidget`) | S | 🟢 | this PR |
| 3 | `edit/{badId}` must not silently create a new milestone | S | 🟢 | this PR |
| 4 | Guard a future *time* on today's date (mirror the date picker's clamp) | S | 🟢 | this PR |
| 25 | Test: blank title resolves to the *localized* resource (en + de) | S | 🟢 | this PR |
| 26 | Test: removing a widget unbinds it | S | 🟢 | this PR |

**Acceptance:** non-English blank titles persist the translated default; the widget-bindings map
stays bounded across add/remove cycles; editing a deleted milestone dismisses instead of creating;
a future time on today no longer silently reads "0"; unit + instrumented suites green.

---

## Phase 1 — Widget refresh consolidation & deep-link robustness

One coherent, battery-aware refresh strategy; make the widget→app deep link survive an already-running task.

| # | Item | Effort | Risk |
|---|------|--------|------|
| 6 | Reconcile `updatePeriodMillis` (alarm) vs. WorkManager periodic — pick one | M | 🟡 |
| 7 | Gate `refreshNow` on `hasPlacedWidgets` (don't enqueue work for users with no widgets) | S | 🟢 |
| 8 | Remove the double redraw (in-process `refreshAll` + WorkManager backstop) | S | 🟡 |
| 9 | Call `ensureScheduled` on app start as a safety net | S | 🟢 |
| 5 | `MainActivity` deep-link robustness: `singleTop` + `onNewIntent` rewiring (moved from Phase 0 — needs intent→recompose plumbing, latent-only today) | M | 🟡 |

**Acceptance:** a single documented source of refresh truth; no WorkManager enqueue with zero widgets;
widget still updates within seconds of an edit and rolls over daily; re-tapping a widget while the app
runs deep-links correctly without losing in-app state.

---

## Phase 2 — Localization & accessibility polish

User-facing quality across all 10 locales and assistive tech.

| # | Item | Effort | Risk |
|---|------|--------|------|
| 10 | Lint rule / audit for hardcoded user-facing strings | S | 🟢 |
| 11 | Localize the `9999+` widget cap format | S | 🟢 |
| 12 | Review abbreviated unit labels (DAYS/HRS/MIN) per locale | M | 🟢 |
| 13 | `Role.Button` + merged semantics on `MilestoneCard` | S | 🟢 |
| 14 | Card content description summarizing days + title | S | 🟢 |
| 15 | Verify the detail hero under large font scale | S | 🟡 |
| 16 | Verify WCAG AA contrast on the Solar accent + scrim | S | 🟢 |
| 27 | Snapshot tests for accent gradients & the "new beginning" state | M | 🟢 |

**Acceptance:** lint fails on new hardcoded strings; TalkBack announces each card as one labeled button;
contrast verified with a tool; snapshot baselines committed.

---

## Phase 3 — Performance & internal refactors

Reduce recomposition churn and centralize wiring. Internal-only, no behavior change.

| # | Item | Effort | Risk |
|---|------|--------|------|
| 17 | Stable per-item keys in the home two-column layout | M | 🟡 |
| 18 | Verify `CountUpNumber` has no jank at large counts | S | 🟢 |
| 19 | `remember` the per-row day count in the widget config list | S | 🟢 |
| 20 | Centralize repository creation (singleton / service locator) | M | 🟡 |
| 21 | `ViewModelProvider.Factory` for the VM test seams | M | 🟢 |
| 22 | Extract the shared `isNew` ("0-day, not future") helper | S | 🟢 |
| 23 | Consider type-safe Compose navigation | M | 🟡 |
| 24 | Document/justify the two-DataStore split | S | 🟢 |
| 28 | Unit-test the `WidgetUi` font-size / cap pure functions | S | 🟢 |

**Acceptance:** no recomposition regressions; repositories created in one place; nav refactor (if done)
keeps all instrumented nav tests green.

---

## Phase 4 — Build, CI & tooling hardening

Keep the project healthy and current. Mostly infra; can run in parallel with Phases 2–3.

| # | Item | Effort | Risk |
|---|------|--------|------|
| 31 | Kover coverage threshold gate in CI | S | 🟢 |
| 32 | Renovate/Dependabot + version-catalog update automation | S | 🟢 |
| 30 | Dependency bump pass (Compose BOM, navigation, etc.) | M | 🟡 |
| 29 | Resolve the `MonochromeLauncherIcon` TODO (needs vector icon source) | M | 🟡 |
| 33 | Plan the compileSdk/targetSdk 36 migration | M | 🟡 |

**Acceptance:** CI enforces a coverage floor; a bot opens dependency-update PRs; build green on bumped versions.

---

## Phase 5 — UX features & privacy (product backlog)

Net-new product value. Larger, spec-worthy items — schedule per product priority.

| # | Item | Effort | Risk |
|---|------|--------|------|
| 36 | Undo (snackbar) for delete | M | 🟢 |
| 39 | Lifecycle-aware pause of the per-second detail ticker | S | 🟡 |
| 38 | Empty widget-config deep-links straight to "add milestone" | M | 🟢 |
| 37 | Sort / reorder options on Home | L | 🟡 |
| 34 | Option to exclude milestone titles from cloud backup | M | 🟡 |
| 35 | Privacy note in About / docs | S | 🟢 |

---

## Sequencing at a glance

```
Phase 0  Correctness            → next patch release   (this PR)
Phase 1  Widget refresh + deep link
Phase 2  i18n + a11y polish
Phase 3  Perf + refactor
Phase 4  CI / tooling           (parallel, continuous)
Phase 5  UX + privacy           (product backlog)
```

## Release mapping

- **patch:** Phase 0 — bug fixes only.
- **next minor:** Phases 1–3 — refresh, polish, internal health.
- **later minor/feature:** Phase 5 — undo, reorder, privacy.
- **Phase 4** lands continuously, not tied to a single release.
