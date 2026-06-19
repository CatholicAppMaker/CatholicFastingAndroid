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

See [docs/PARITY_PROCESS.md](docs/PARITY_PROCESS.md) for the working agreement between the Android and iOS apps.

## Local Tooling

This workspace has a repo-local Gradle wrapper for command-line runs that are
sensitive to local macOS runtime differences:

```bash
./scripts/gradle --no-daemon --version
```

`./scripts/gradle` uses Homebrew OpenJDK 21 when available, keeps Gradle state
in this repo's ignored `.gradle-codex/` directory, and disables Gradle native
services/file watching for macOS 27 compatibility.

Design and style passes use the repo-local Impeccable context loader:

```bash
./scripts/run-impeccable-context.sh
```

`PRODUCT.md` and `DESIGN.md` are committed at the repo root so Impeccable can
load Android-specific product, brand, typography, spacing, and seasonal-tone
rules before a design review.

## Cleanup Gate

Run this before checkpoint commits and after substantial implementation passes:

```bash
ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" \
./scripts/gradle --no-daemon \
  -Dkotlin.compiler.execution.strategy=in-process \
  ktlintCheck detekt lint testDebugUnitTest
```

For release-candidate validation, also run the connected Android gates from
`docs/ANDROID_RELEASE.md`, including the expanded parity suite and full
`app:connectedDebugAndroidTest` pass.

## Progress Tracking

The current implementation checklist lives in:

- `PROJECT_CHECKLIST.md`

Phone validation status lives in:

- `docs/PHONE_RELEASE_VALIDATION.md`

## Release Docs

For release build and store-ops work, use:

- [docs/ANDROID_RELEASE.md](docs/ANDROID_RELEASE.md)
- [docs/ANDROID_DESIGN.md](docs/ANDROID_DESIGN.md)
- [docs/ANDROID_UI_TOKENS.md](docs/ANDROID_UI_TOKENS.md)
- [docs/PLAY_CONSOLE_RELEASE_CHECKLIST.md](docs/PLAY_CONSOLE_RELEASE_CHECKLIST.md)
- [docs/PLAY_LISTING_COPY.md](docs/PLAY_LISTING_COPY.md)
- [docs/PRIVACY_POLICY.md](docs/PRIVACY_POLICY.md)
