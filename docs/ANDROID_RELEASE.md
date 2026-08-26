# Android Release Build

Use this repo flow for a Play-ready signed Android bundle.

## Local signing setup

1. Copy `keystore.properties.example` to `keystore.properties`.
2. Generate or place the upload keystore at the `storeFile` path from `keystore.properties`.
3. Keep `keystore.properties` and the keystore file local only. They are gitignored.

## Build commands

- Cleanup gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/gradle --no-daemon ktlintCheck detekt lint testDebugUnitTest`
- Targeted connected release gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" python3 scripts/run_with_watchdog.py 1800 ./scripts/gradle --no-daemon app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kevpierce.catholicfastingapp.ReleaseRoutingInstrumentationTest,com.kevpierce.catholicfastingapp.PrivacyLocalizationInstrumentationTest`
- Expanded connected parity gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" python3 scripts/run_with_watchdog.py 1800 ./scripts/gradle --no-daemon app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kevpierce.catholicfastingapp.ExpandedReleaseUiInstrumentationTest,com.kevpierce.catholicfastingapp.LocalizationResourcesInstrumentationTest`
- Full connected gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/run_full_connected_gate.sh [--serial SERIAL] [--timeout-seconds 3600] [--expected-count 119]`
- Responsive matrix evidence gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/run_responsive_matrix.sh [--serial SERIAL] [--timeout-seconds 1200]`
- Signed Play bundle:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/gradle --no-daemon :app:bundleRelease`

## Responsive matrix evidence

Run `scripts/run_responsive_matrix.sh` on exactly one ready
`Medium_Phone_API_36.1` emulator. The instrumentation suite exercises the
complete nine-profile matrix:

| Width × height (dp) | Font scale 1.0 | Font scale 1.4 | Font scale 2.0 |
| --- | --- | --- | --- |
| 320 × 720 | required | required | required |
| 411 × 891 | required | required | required |
| 600 × 960 | required | required | required |

Each profile covers all six release routes:

1. Today
2. Fasting Days
3. Active Track Fast
4. Reminder Center
5. Guided Premium journey
6. Privacy & Data

That produces 54 automated route scenarios. The suite must complete with zero
failures and zero skipped, ignored, or assumption-bypassed scenarios. The
capture test writes six transient physical-device context captures during the suite;
the runner pulls and validates them after the suite:

- `320x720-font200-light-today.png`
- `320x720-font200-dark-track-fast.png`
- `411x891-font140-light-reminder-center.png`
- `411x891-font140-dark-premium.png`
- `600x960-font200-light-fasting-days.png`
- `600x960-font200-dark-privacy-data.png`

The 54 automated semantics/bounds scenarios are the authoritative full-layout
evidence. The PNGs are physical 1080 × 2400 screencaps: 320 dp is letterboxed
and the forced 600 dp surface is wider than the physical 420 dpi display, so
they provide focal/theme context rather than complete forced-viewport edge
coverage. Captures and device metadata are written only to the generated output path
`app/build/outputs/responsive-evidence`. Each PNG must be nonempty, exactly
1080 × 2400 pixels, and have a distinct SHA-256 hash. The runner records the
serial, AVD, SDK, Android release, locale, timezone, physical size, density,
night mode, global night mode, and animation scales in `metadata.txt` beside
the captures. Before starting Gradle, it rejects any device that is not
`en-US`, `America/New_York`, globally light, and at zero animation scales. It
also rejects a missing or non-exact ten-test JUnit result for
`ResponsiveMatrixInstrumentationTest`, or any failure, error, skipped, or
ignored result. The final signed AAB must remain at or below 12,000,000 bytes.

The responsive runner has a 1,200-second host watchdog by default. The full
connected command above has a 3,600-second watchdog. Each connected Gradle
process runs in its own process group; on timeout the watchdog sends `TERM`,
waits 15 seconds, sends `KILL` only to that group if needed, and exits `124`.
Responsive timeouts preserve available crash-buffer and device metadata in
`app/build/outputs/responsive-evidence/timeout-diagnostics.txt`. Override the
responsive limit with `--timeout-seconds` or `RESPONSIVE_TIMEOUT_SECONDS` only
when a deliberately slower host requires it; never run a release connected
gate without a finite watchdog.

For this release, the full connected runner clears prior connected JUnit XML
before Gradle and requires exactly 119 fresh testcases with zero failures,
errors, skipped, ignored, corrupt, or negative-time results. Update the explicit
expected count when the source test inventory intentionally changes.

## Local-only 9/10 confidence gate

Use this gate when validating the Android app locally before Play Console or
external-device work. It specifically covers billing contracts, premium UI
states, Android-native manifest/package configuration, notification receivers,
widgets, shortcuts, deep links, cold launch/recreate, and persisted app state.

- Warning-fail cleanup gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/gradle --no-daemon --warning-mode fail ktlintCheck detekt lint testDebugUnitTest`
- Targeted local-only 9/10 connected gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" python3 scripts/run_with_watchdog.py 1800 ./scripts/gradle --no-daemon --warning-mode fail app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kevpierce.catholicfastingapp.PremiumBillingStateInstrumentationTest,com.kevpierce.catholicfastingapp.PersistenceRecreateInstrumentationTest,com.kevpierce.catholicfastingapp.ReleaseRoutingInstrumentationTest,com.kevpierce.catholicfastingapp.LocalizationResourcesInstrumentationTest`
- Full warning-fail connected gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" python3 scripts/run_with_watchdog.py 3600 ./scripts/gradle --no-daemon --warning-mode fail app:connectedDebugAndroidTest`
- Dead-test scan:
  - `find app/src/androidTest app/src/test core feature -path '*/build' -prune -o -name '*.kt' -print | xargs rg -n '@Ignore|@Disabled|Assume\.|assumeTrue|assumeFalse'`

Do not count skipped, ignored, or assumption-bypassed tests toward local release
confidence. If any targeted or full gate fails, triage it as an app bug, test
defect, or environment blocker, then fix and rerun the affected command before
rating the release upward.

## Release artifact

- Bundle path:
  - `app/build/outputs/bundle/release/app-release.aab`
- R8 deobfuscation mapping:
  - `app/build/outputs/mapping/release/mapping.txt`
- Native debug symbols, if Gradle generates a native symbol archive for the build:
  - `app/build/outputs/native-debug-symbols/release/native-debug-symbols.zip`

Upload `mapping.txt` in Play Console when the release review page asks for a
deobfuscation file. Upload native debug symbols only if the
`native-debug-symbols.zip` file exists for the current build. These files make
crash and ANR reports more readable without changing user-facing behavior.

## Pre-upload checks

- Confirm the final checklist in `PROJECT_CHECKLIST.md` is fully checked.
- Confirm `docs/ANDROID_IOS_45_PARITY_MATRIX.md` still matches the approved iOS
  4.5 phone scope and Android closed-test decisions.
- Confirm `docs/PHONE_RELEASE_VALIDATION.md` is fully checked.
- Confirm the Play listing and privacy policy still match the current local-only scope with no backup/export/import flows.
- Confirm support tips remain excluded from the Android 1.0 Play catalog unless
  a later release explicitly reintroduces them.
- Bump `versionCode` and `versionName` before the final release candidate build.
- Verify the release bundle is signed with the upload key, not the debug key.
