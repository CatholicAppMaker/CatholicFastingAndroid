# Play Asset Manifest

This manifest records the current Google Play asset files prepared in-repo.

## Generated Assets

- `generated/play-icon-512.png`
  - source: exported from the shipped launcher identity
  - size: `512x512`
- `generated/feature-graphic-sacred-editorial-v1.png`
  - source: Codex image generation, cropped for Play upload
  - size: `1024x500`
- `generated/feature-graphic-sacred-editorial-v1-source.png`
  - source: original generated concept
  - size: `1536x1024`

## Raw Screenshot Captures

- `screenshots/raw/raw-today-1080x2400.png`
- `screenshots/raw/raw-fasting-days-1080x2400.png`
- `screenshots/raw/raw-track-fast-1080x2400.png`
- `screenshots/raw/raw-reminder-center-1080x2400.png`
- `screenshots/raw/raw-premium-1080x2400.png`
- `screenshots/raw/raw-privacy-data-1080x2400.png`

All raw screenshot captures are `1080x2400`.

## Screenshot Capture Provenance

The release set is generated from the real Android app by `PlayScreenshotInstrumentationTest` and `scripts/regenerate_play_screenshots.sh`.

- AVD: `Medium_Phone_API_36.1`
- image: Android 36.1 Play Store ARM64
- device: portrait `1080x2400`
- locale: `en-US`
- timezone: `America/New_York`
- theme: light
- system animations: disabled
- fixture date: `2026-02-18`
- fixture state: onboarding complete, US region, balanced reminders, 08:00 quote reminder, prayer intention, and a 16-hour fast started one hour earlier
- privacy timestamp: the visible local-write timestamp is the real repository fixture-seeding time; only presentation time is fixed, and persistence time is not overridden
- premium: real locked billing state, with no injected entitlement
- capture policy: no retouching, compositing, generated UI, or simulated product state

The instrumentation test waits for the selected route and focal content before capturing. The host script validates all six fresh files at `1080x2400`, rejects duplicate SHA-256 values, and changes tracked final screenshots only after the complete set passes.

### Current checked-in SHA-256 inventory

These values describe the fresh, validated capture set regenerated on the documented AVD.

| file | SHA-256 |
| --- | --- |
| `screenshots/raw/raw-today-1080x2400.png` | `58901f452321a4be77bb33c0c58bbc5825c17a3a78f241f9fcdaf25f6d7e70ae` |
| `screenshots/raw/raw-fasting-days-1080x2400.png` | `b1592497d3591163ba09dbb05c94e86641c0cc0ec7802a81ea33f18bf256ca1c` |
| `screenshots/raw/raw-track-fast-1080x2400.png` | `4d8eb00fc706231315f35ca16f99863e1f447f6484a48733d092ef9a18a02ae3` |
| `screenshots/raw/raw-reminder-center-1080x2400.png` | `17130d54767c11375fa648879ee492d9277a5035a03416df8280c2bcc9d526eb` |
| `screenshots/raw/raw-premium-1080x2400.png` | `8e50cacadb207729f82b39c8520e29ea1422200eca7e8755d336ddf31989a5ed` |
| `screenshots/raw/raw-privacy-data-1080x2400.png` | `64e09952fb6cc29ee2c3aa8bfbaff1d439e20049d0b14bd64290331f68abc939` |
| `screenshots/final/screenshot-01-today.png` | `58901f452321a4be77bb33c0c58bbc5825c17a3a78f241f9fcdaf25f6d7e70ae` |
| `screenshots/final/screenshot-02-fasting-days.png` | `b1592497d3591163ba09dbb05c94e86641c0cc0ec7802a81ea33f18bf256ca1c` |
| `screenshots/final/screenshot-03-track-fast.png` | `4d8eb00fc706231315f35ca16f99863e1f447f6484a48733d092ef9a18a02ae3` |
| `screenshots/final/screenshot-04-reminder-center.png` | `17130d54767c11375fa648879ee492d9277a5035a03416df8280c2bcc9d526eb` |
| `screenshots/final/screenshot-05-premium.png` | `8e50cacadb207729f82b39c8520e29ea1422200eca7e8755d336ddf31989a5ed` |
| `screenshots/final/screenshot-06-privacy-data.png` | `64e09952fb6cc29ee2c3aa8bfbaff1d439e20049d0b14bd64290331f68abc939` |

## Final Screenshot Set

1. `screenshots/final/screenshot-01-today.png`
2. `screenshots/final/screenshot-02-fasting-days.png`
3. `screenshots/final/screenshot-03-track-fast.png`
4. `screenshots/final/screenshot-04-reminder-center.png`
5. `screenshots/final/screenshot-05-premium.png`
6. `screenshots/final/screenshot-06-privacy-data.png`

All final screenshots are `1080x2400`.

## Notes

- The screenshot set uses the actual Android app running in the emulator.
- The tracker screenshot was updated to show a truthful active-fast state.
- The premium screenshot reflects the current shipped locked/inactive subscription state.
- The privacy screenshot reflects the current local-only, no-backup scope.
