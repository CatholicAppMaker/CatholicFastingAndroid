# Play Assets Workspace

This directory holds generated marketing assets, raw screenshot captures, final screenshot exports, and prompts used for the Google Play listing.

## Structure

- `generated/`: generated marketing outputs and launcher-derived Play exports
- `screenshots/raw/`: direct device or emulator captures
- `screenshots/final/`: final submission-ready screenshot files
- `prompts/`: prompt source files for Codex image generation

## Rules

- Screenshots must come from the actual Android app.
- Generated art is allowed for the feature graphic and later in-app devotional art only when it follows the rules in [PLAY_ASSET_SPEC.md](../../docs/PLAY_ASSET_SPEC.md).
- Keep names deterministic and lowercase kebab-case.

## Truthful screenshot regeneration

The six listing screenshots are captured by `PlayScreenshotInstrumentationTest`, which resets the app container and seeds the production repository for February 18, 2026. The test waits for the selected route and focal content before taking each screenshot. It keeps Premium in the real locked state, with no injected entitlement.

Run the host workflow from the repository root after starting the capture emulator:

```sh
scripts/regenerate_play_screenshots.sh
```

The workflow runs only `PlayScreenshotInstrumentationTest`, collects its six app-specific external files, and validates every PNG as exactly `1080x2400`. It rejects duplicate SHA-256 values and promotes the final files only after all six fresh captures pass. Use `scripts/regenerate_play_screenshots.sh --dry-run` to validate the tracked raw set without running Gradle or changing files.

Capture contract:

- AVD: `Medium_Phone_API_36.1`, Android 36.1 Play Store ARM64 image
- device: portrait `1080x2400`
- locale: `en-US`
- timezone: `America/New_York`
- theme: light
- system animations: disabled
- fixture date: `2026-02-18`, with a one-hour-old 16-hour fast
- source: real app UI only, with no retouching, compositing, or simulated entitlement

The exact provenance and current checked-in SHA-256 inventory are recorded in [MANIFEST.md](MANIFEST.md). A capture is release-ready only when the regeneration command exits successfully.
