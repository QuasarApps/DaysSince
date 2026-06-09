# Privacy Policy for Pulsar

**Effective date:** 8 June 2026
**App:** Pulsar (`com.quasarapps.pulsar`)
**Developer:** Quasar Apps
**Contact:** martin@quasarapps.com

Pulsar is designed to be private by default. The short version: **Pulsar does not
collect, transmit, or share any personal data.** Everything you enter stays on your
device.

This policy explains that in full, as required for the Google Play listing.

## What data Pulsar handles

Pulsar stores only the milestones you create. Each milestone consists of:

- a title you type (e.g. "Quit smoking"),
- a date and time you pick,
- a colour accent, and
- the moment the milestone was created.

It also stores your in-app preferences (theme, whether to show hours/minutes,
the backup toggle, and which milestone each placed widget is bound to).

All of this is held in **app-private storage** (an Android DataStore) that no
other app can read.

## What Pulsar does **not** do

- **No data collection.** Pulsar does not collect personal or sensitive user
  data of any kind.
- **No personal data leaves your device.** Pulsar has no servers and no backend.
  It never sends your milestones, settings, or any personal data over the
  network. (See *Fonts* below for the one piece of non-personal, optional network
  activity.)
- **No accounts.** There is no sign-in, registration, or user profile.
- **No permissions.** Pulsar requests no runtime permissions (no location, no
  contacts, no camera, no storage access).
- **No analytics, ads, or tracking.** Pulsar contains no analytics SDKs, no
  advertising SDKs, no crash-reporting services, and no third-party trackers.
- **No data sharing.** Because nothing is collected, nothing is shared with any
  third party.

## Fonts (the only network activity)

Pulsar uses Android's [downloadable
fonts](https://developer.android.com/develop/ui/views/text-and-emoji/downloadable-fonts)
for its two brand typefaces. On a device with Google Play Services, the system
may fetch these fonts from Google's fonts provider. In that request **only the
name of the font is sent** — never your milestones, settings, or any personal or
identifying data — and it is handled by Google under [Google's Privacy
Policy](https://policies.google.com/privacy). The app ships with bundled fallback
fonts, so it renders and works fully even with no network or no Play Services.

This is the only network activity Pulsar triggers, and it involves no personal
data.

## Android Auto Backup

Pulsar includes an optional **"Back up milestones"** setting (found under
*Settings → Backup & privacy*). It is on by default and you can turn it off at any
time.

When it is **on**, your milestones are included in [Android Auto
Backup](https://developer.android.com/identity/data/autobackup) — the same
operating-system feature that backs up most apps. This means:

- The backup is performed by **Android and your Google account**, not by Pulsar
  or Quasar Apps. We never see, receive, or have access to it.
- Google stores it encrypted and governs it under
  [Google's Privacy Policy](https://policies.google.com/privacy). On modern
  Android versions the backup is additionally encrypted with your device's
  lock-screen secret.
- It exists so your milestones can be restored when you set up a new device or
  reinstall the app on the same Google account.

When the setting is **off**, your milestones are excluded from Auto Backup and
remain only on the current device.

## Children's privacy

Pulsar does not collect any data from anyone, including children. It contains no
ads and no in-app purchases.

## Changes to this policy

If this policy changes, the updated version will be published at this same
location and the "Effective date" above will be revised. Material changes will be
reflected in the app's release notes.

## Contact

Questions about this policy or about privacy in Pulsar can be sent to
**martin@quasarapps.com**.
