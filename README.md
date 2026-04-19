# Catholic Fasting App for Android

Native Android app for Catholic fasting guidance and tracking.

## Repo Role

This Android project is intended to live in its own git repository, separate from the iOS app at:

- `/Users/kevpierce/Desktop/CatholicFastingApp`

The iOS app remains the behavioral reference for parity-sensitive logic, but Android should keep Android-native UX, integrations, and release workflows.

## Current Direction

- Native Android only
- Phone-first release target
- Local-first storage
- No backup, export, import, or household-share flows in the current Android scope
- Behavioral parity with iOS where it matters
- Android-native delivery for widgets, shortcuts, notifications, deep links, and Play billing

## Project Workflow

See [docs/PARITY_PROCESS.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PARITY_PROCESS.md) for the working agreement between the Android and iOS apps.

## Cleanup Gate

Run this before checkpoint commits and after substantial implementation passes:

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" \
./.local-tools/gradle-8.11.1/bin/gradle --no-daemon \
  -Dkotlin.compiler.execution.strategy=in-process \
  ktlintCheck detekt lint testDebugUnitTest
```

## Progress Tracking

The current implementation checklist lives in:

- `/Users/kevpierce/Desktop/CFAAnrdoid/PROJECT_CHECKLIST.md`

Phone validation status lives in:

- `/Users/kevpierce/Desktop/CFAAnrdoid/docs/PHONE_RELEASE_VALIDATION.md`

## Release Docs

For release build and store-ops work, use:

- [docs/ANDROID_RELEASE.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/ANDROID_RELEASE.md)
- [docs/ANDROID_DESIGN.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/ANDROID_DESIGN.md)
- [docs/ANDROID_UI_TOKENS.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/ANDROID_UI_TOKENS.md)
- [docs/PLAY_CONSOLE_RELEASE_CHECKLIST.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PLAY_CONSOLE_RELEASE_CHECKLIST.md)
- [docs/PLAY_LISTING_COPY.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PLAY_LISTING_COPY.md)
- [docs/PRIVACY_POLICY.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PRIVACY_POLICY.md)
