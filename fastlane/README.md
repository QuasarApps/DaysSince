# Play Store listing metadata

This directory holds the Google Play store listing for Pulsar in the layout
[fastlane supply](https://docs.fastlane.tools/actions/supply/) expects, so the
listing lives in version control instead of only in the Play Console. You can
upload it by hand (copy/paste into the Console) or, later, automate it with
`fastlane supply`.

```
fastlane/metadata/android/<locale>/
  title.txt               App name on the listing (max 30 chars)
  short_description.txt   One-liner under the title (max 80 chars)
  full_description.txt    The long description (max 4000 chars)
  changelogs/
    default.txt           "What's new" notes, used for any versionCode
                          without a specific <versionCode>.txt (max 500 chars)
```

To attach notes to a specific build instead of all of them, add
`changelogs/<versionCode>.txt` (e.g. `changelogs/10000.txt` for `1.0.0` — see
[RELEASING.md](../RELEASING.md) for the versionCode scheme).

## Locales

Play uses BCP-47-ish listing codes that differ from the app's `res/values-*`
qualifiers. The mapping for the locales Pulsar ships:

| App resource (`res/`) | Language   | Play listing locale |
| --------------------- | ---------- | ------------------- |
| `values/` (default)   | English    | `en-US`             |
| `values-de/`          | German     | `de-DE`             |
| `values-es/`          | Spanish    | `es-ES`             |
| `values-fr/`          | French     | `fr-FR`             |
| `values-it/`          | Italian    | `it-IT`             |
| `values-iw/`          | Hebrew     | `iw-IL`             |
| `values-pl/`          | Polish     | `pl-PL`             |
| `values-pt/`          | Portuguese | `pt-PT`             |
| `values-ru/`          | Russian    | `ru-RU`             |
| `values-ar/`          | Arabic     | `ar`                |

> If the Play Console rejects any locale code, confirm it against the exact list
> in **Store presence → Main store listing → Manage translations**; a couple of
> codes (notably Hebrew and Arabic) vary by tooling version.

## Translation status

⚠️ **Only `en-US` is written in its native language.** Every other locale
currently contains the **English** text as a placeholder so the listing is
complete and uploadable from day one. `title.txt` is the brand name `Pulsar`
and needs no translation; the three description/changelog files do.

To localize a listing, translate these files in the locale's directory:

- [ ] `de-DE` — short_description, full_description, changelogs/default
- [ ] `es-ES` — short_description, full_description, changelogs/default
- [ ] `fr-FR` — short_description, full_description, changelogs/default
- [ ] `it-IT` — short_description, full_description, changelogs/default
- [ ] `iw-IL` — short_description, full_description, changelogs/default
- [ ] `pl-PL` — short_description, full_description, changelogs/default
- [ ] `pt-PT` — short_description, full_description, changelogs/default
- [ ] `ru-RU` — short_description, full_description, changelogs/default
- [ ] `ar`    — short_description, full_description, changelogs/default

Keep within the limits noted above (80 / 4000 / 500 chars).

### Planned: a keyword-rich title (ASO)

The listing `title.txt` is currently the bare brand name **Pulsar** in every
locale (safe, and needs no translation). A keyword-rich title such as
**"Pulsar: Days Since Tracker"** (≤ 30 chars) would help store-search ranking and
is a wanted change. Adopting it means the descriptor part then needs a natural
per-locale translation (the brand "Pulsar" stays), so it is folded into the
translation pass above rather than shipped English-only across all locales.

## Graphic assets (not in git)

These are required/recommended by Play and are **not** committed (binary, and
they change independently of code). Drop them under each locale's `images/`
directory if you want supply to manage them, or just upload them in the Console.

| Asset             | Spec                                        | Required |
| ----------------- | ------------------------------------------- | -------- |
| App icon          | 512×512 PNG, 32-bit                         | Yes      |
| Feature graphic   | 1024×500 PNG/JPG                            | Yes      |
| Phone screenshots | 2–8, PNG/JPG, 16:9 or 9:16, ≥320px short side | Yes (≥2) |
| 7" tablet shots   | up to 8                                      | Optional |
| 10" tablet shots  | up to 8                                      | Optional |

supply layout for images (per locale):
`<locale>/images/icon.png`, `<locale>/images/featureGraphic.png`,
`<locale>/images/phoneScreenshots/1.png` …

## Data safety form

Pulsar collects and shares **nothing**, so the Console's Data safety section is
short. Recommended answers:

- **Does your app collect or share any of the required user data types?** → **No.**
- **Is all of the user data encrypted in transit?** → N/A — no personal or user
  data is sent over the network. (The app's only network activity is the optional
  Android downloadable-fonts fetch, which sends just a font-family name, never
  user data.)
- **Do you provide a way for users to request that their data be deleted?** →
  Users delete milestones in-app; uninstalling removes everything. No server-side
  data exists.

Notes for the reviewer questionnaire:

- No personal or sensitive data is collected, transmitted, or shared.
- No ads, no analytics, no third-party SDKs.
- The app uses Android's downloadable Google Fonts: on a Play-Services device the
  font name is fetched from Google's provider (governed by Google's Privacy
  Policy); bundled fallback fonts cover the offline path. No user data is involved.
- The optional Android Auto Backup is performed by the OS / the user's Google
  account, not by the app — it is not developer data collection. See
  [`docs/privacy-policy.md`](../docs/privacy-policy.md).

## Other listing fields (set in the Console)

- **Privacy policy URL:** hosted copy of
  [`docs/privacy-policy.md`](../docs/privacy-policy.md).
- **App category:** Tools (or Lifestyle).
- **Tags / content rating:** complete the IARC questionnaire — Pulsar has no ads,
  no purchases, no user-generated content, and no objectionable material.
- **Contact email:** martin@quasarapps.com.
