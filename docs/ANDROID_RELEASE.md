# Android Release Build

Use this repo flow for a Play-ready signed Android bundle.

## Local signing setup

1. Copy `keystore.properties.example` to `keystore.properties`.
2. Generate or place the upload keystore at the `storeFile` path from `keystore.properties`.
3. Keep `keystore.properties` and the keystore file local only. They are gitignored.

## Build commands

- Cleanup gate:
  - `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./.local-tools/gradle-8.11.1/bin/gradle --no-daemon ktlintCheck detekt lint testDebugUnitTest`
- Targeted phone validation gate:
  - `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./.local-tools/gradle-8.11.1/bin/gradle --no-daemon app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kevpierce.catholicfastingapp.ReleaseRoutingInstrumentationTest,com.kevpierce.catholicfastingapp.PrivacyLocalizationInstrumentationTest`
- Signed Play bundle:
  - `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./.local-tools/gradle-8.11.1/bin/gradle --no-daemon :app:bundleRelease`

## Release artifact

- Bundle path:
  - `app/build/outputs/bundle/release/app-release.aab`

## Pre-upload checks

- Confirm the final checklist in `PROJECT_CHECKLIST.md` is fully checked.
- Confirm `docs/PHONE_RELEASE_VALIDATION.md` is fully checked.
- Confirm the Play listing and privacy policy still match the current local-only scope with no backup/export/import flows.
- Bump `versionCode` and `versionName` before the final release candidate build.
- Verify the release bundle is signed with the upload key, not the debug key.
