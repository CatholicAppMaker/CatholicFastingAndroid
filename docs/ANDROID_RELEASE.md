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
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/gradle --no-daemon app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kevpierce.catholicfastingapp.ReleaseRoutingInstrumentationTest,com.kevpierce.catholicfastingapp.PrivacyLocalizationInstrumentationTest`
- Expanded connected parity gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/gradle --no-daemon app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kevpierce.catholicfastingapp.ExpandedReleaseUiInstrumentationTest,com.kevpierce.catholicfastingapp.LocalizationResourcesInstrumentationTest`
- Full connected gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/gradle --no-daemon app:connectedDebugAndroidTest`
- Signed Play bundle:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/gradle --no-daemon :app:bundleRelease`

## Local-only 9/10 confidence gate

Use this gate when validating the Android app locally before Play Console or
external-device work. It specifically covers billing contracts, premium UI
states, Android-native manifest/package configuration, notification receivers,
widgets, shortcuts, deep links, cold launch/recreate, and persisted app state.

- Warning-fail cleanup gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/gradle --no-daemon --warning-mode fail ktlintCheck detekt lint testDebugUnitTest`
- Targeted local-only 9/10 connected gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/gradle --no-daemon --warning-mode fail app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kevpierce.catholicfastingapp.PremiumBillingStateInstrumentationTest,com.kevpierce.catholicfastingapp.PersistenceRecreateInstrumentationTest,com.kevpierce.catholicfastingapp.ReleaseRoutingInstrumentationTest,com.kevpierce.catholicfastingapp.LocalizationResourcesInstrumentationTest`
- Full warning-fail connected gate:
  - `ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/gradle --no-daemon --warning-mode fail app:connectedDebugAndroidTest`
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
