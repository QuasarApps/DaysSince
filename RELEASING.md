# Releasing Pulsar

How to cut a Pulsar release and get it onto Google Play. This is the source of
truth for versioning, signing, and the release workflow.

---

## Versioning

Pulsar uses [Semantic Versioning](https://semver.org): `MAJOR.MINOR.PATCH`.

- `versionName` is the tag without the leading `v` (tag `v1.2.3` → `1.2.3`).
- `versionCode` is derived from the version so it is monotonic and deterministic:

  ```
  versionCode = MAJOR * 10000 + MINOR * 100 + PATCH
  ```

  So `1.0.0` → `10000`, `1.2.3` → `10203`, `2.0.0` → `20000`.

  **Constraint:** `MINOR` and `PATCH` must each stay below `100`. That is plenty
  for normal use; if you ever need a 100th patch, ship the next minor instead.

Both values are read from the `PULSAR_VERSION_NAME` / `PULSAR_VERSION_CODE`
environment variables in [`app/build.gradle.kts`](app/build.gradle.kts). With no
env vars set (a plain local build) they fall back to `1.0.0` / `1` — fine for
smoke tests, never for a Play upload.

Play rejects re-uploading an already-used `versionCode`, so **every Play upload
needs a new tag.** To re-cut a broken build, bump the patch (`v1.0.1`).

---

## One-time setup

### 1. Create the upload keystore

If you don't already have one, generate an upload key (keep it safe and backed
up — losing it means you can no longer update the app unless you enrolled in Play
App Signing's key-reset flow):

```bash
keytool -genkeypair -v \
  -keystore upload-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```

Store the real `keystore.properties` locally (it is gitignored) by copying
[`keystore.properties.template`](keystore.properties.template) and filling in the
values. That is only needed for **local** signed builds; CI uses secrets instead.

### 2. Add the GitHub Actions secrets

The [`release.yml`](.github/workflows/release.yml) workflow signs the bundle from
repository secrets. Under **Settings → Secrets and variables → Actions**, add:

| Secret | Value |
| --- | --- |
| `PULSAR_KEYSTORE_BASE64` | The keystore file, base64-encoded (see below) |
| `PULSAR_KEYSTORE_PASSWORD` | The keystore password |
| `PULSAR_KEY_ALIAS` | The key alias (e.g. `upload`) |
| `PULSAR_KEY_PASSWORD` | The key password |

Encode the keystore as a single line of base64:

```bash
# Linux
base64 -w0 upload-keystore.jks > keystore.base64.txt
# macOS
base64 -i upload-keystore.jks -o keystore.base64.txt
```

```powershell
# Windows / PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("upload-keystore.jks")) | Set-Content -NoNewline keystore.base64.txt
```

Paste the contents of `keystore.base64.txt` as the `PULSAR_KEYSTORE_BASE64`
secret value, then delete the file. **Never commit the keystore or these values.**

### 3. Create the Play Console listing

First release only. In the [Play Console](https://play.google.com/console):

1. Create the app and complete the store listing (title, descriptions,
   screenshots, feature graphic). Source copy lives under
   [`fastlane/metadata/android/`](fastlane/metadata/android) — see
   [`fastlane/README.md`](fastlane/README.md).
2. Set the **privacy policy URL** to the hosted copy of
   [`docs/privacy-policy.md`](docs/privacy-policy.md) (e.g. enable GitHub Pages
   for `docs/` → `https://quasarapps.github.io/Pulsar/privacy-policy`).
3. Complete the **Data safety** form — recommended answers are in
   [`fastlane/README.md`](fastlane/README.md) (short version: *no data collected,
   no data shared*).
4. Complete the content-rating questionnaire and set the app category.
5. Enroll in **Play App Signing** (recommended): you upload an AAB signed with
   the *upload* key above, and Google re-signs with the managed *app signing* key.

---

## Cutting a release

1. Make sure `develop` is green and merged to `main` (or release from whichever
   branch you publish from), and that [`CHANGELOG.md`](CHANGELOG.md) has an entry
   for the new version.
2. Tag and push:

   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

3. The [`Release` workflow](.github/workflows/release.yml) runs automatically and:
   - derives `versionName` / `versionCode` from the tag,
   - decodes the keystore and builds a **signed** `:app:bundleRelease`,
   - verifies the bundle is signed,
   - uploads `pulsar-<version>.aab` as a workflow artifact, and
   - publishes a GitHub Release for the tag with the AAB attached.

4. Download the `.aab` from the GitHub Release (or the workflow artifact).
5. In the Play Console, create a new release on the desired track (internal →
   closed → production), upload the `.aab`, paste the release notes (the matching
   [`fastlane/metadata/android/<locale>/changelogs/`](fastlane/metadata/android)
   entry), and roll out.

> The workflow stops at producing a signed, published AAB. It deliberately does
> **not** push to Play automatically. Wiring up `fastlane supply` / the Play
> Developer API for automated track deployment is a possible future step.

---

## Dry-running the workflow (optional)

Before adding the signing secrets or cutting your first tag, you can shake out
the workflow plumbing from the **Actions** tab with no side effects:

1. Go to **Actions → Release → Run workflow**.
2. Optionally set a fake version (default `0.0.1`), then run it.

This manual (`workflow_dispatch`) run exercises checkout, the JBR/Gradle
toolchain, the version parsing and its validation, and an **unsigned**
`:app:bundleRelease`, then uploads the resulting AAB as a workflow artifact. It
**skips** the signing-secret check, the keystore decode, signature verification,
and the GitHub Release publish — so it needs no secrets and publishes nothing.

That validates everything *except* signing. Once it's green, add the secrets
([above](#2-add-the-github-actions-secrets)) and cut a throwaway `v0.0.1` tag to
exercise the signing + publish path before the real `v1.0.0`.

> The "Run workflow" button only appears once this workflow file is on the
> repository's default branch.

---

## Building a signed bundle locally (optional)

With a real `keystore.properties` in place:

```powershell
# Windows / PowerShell
$env:PULSAR_VERSION_NAME = "1.0.0"
$env:PULSAR_VERSION_CODE = "10000"
.\gradlew.bat :app:bundleRelease
```

```bash
# macOS / Linux
PULSAR_VERSION_NAME=1.0.0 PULSAR_VERSION_CODE=10000 ./gradlew :app:bundleRelease
```

The bundle lands at `app/build/outputs/bundle/release/app-release.aab`.

---

## Pre-release checklist

- [ ] `CHANGELOG.md` updated with the new version and date.
- [ ] Play `changelogs/<versionCode>.txt` (or `default.txt`) written for each
      locale you ship notes in.
- [ ] CI green on the commit you're tagging (build, tests, lint, instrumentation).
- [ ] Store listing assets current (screenshots reflect the shipping UI).
- [ ] Privacy policy URL reachable and current.
- [ ] Tag is `vMAJOR.MINOR.PATCH` with `MINOR`/`PATCH` < 100.
- [ ] Signing secrets present in the repo (first release / after any key change).
