# Repo instructions for Claude

## Workflow

- **Always raise a PR for changes.** Never commit code changes directly to `develop` (or `main`). Create a fresh branch off the latest `origin/develop`, commit there, push, and open a PR against `develop` with `gh pr create`. This applies to every change, no matter how small.

## Permission allowlist

- The `.claude/settings.json` allowlist intentionally trusts the project's own Gradle wrapper (`./gradlew` / `.\gradlew.bat`) for **any** task. Any Gradle invocation runs `build.gradle.kts`/test code, so per-task scoping isn't a real security boundary — this is a de-noising convenience scoped to the project's own build, not a grant for arbitrary external tools.
