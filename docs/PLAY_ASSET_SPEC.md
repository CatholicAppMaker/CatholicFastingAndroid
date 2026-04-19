# Play Asset Spec

This document is the source of truth for Google Play submission visuals and the follow-on Android devotional-art pass.

Use this alongside:

- [docs/PLAY_SUBMISSION_RUNBOOK.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PLAY_SUBMISSION_RUNBOOK.md)
- [docs/FIRST_PLAY_SUBMISSION.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/FIRST_PLAY_SUBMISSION.md)
- [docs/ANDROID_DESIGN.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/ANDROID_DESIGN.md)

## Non-Negotiable Asset Rules

- Play screenshots must come from the actual Android build.
- Do not fabricate product states, premium states, reminders, or data.
- Do not show features that are not present in the shipped Android app.
- Do not imply medical, weight-loss, or official-Church endorsement claims.
- Do not use generated art in place of truthful app screenshots.
- Do not use generated art that imitates copyrighted devotional works too closely.
- Always verify file size, aspect ratio, and pixel dimensions before upload.

## Visual Direction

The shared visual direction for generated marketing art and the later in-app devotional-art pass is:

- sacred editorial
- reverent and calm
- painterly instead of glossy stock-photo
- liturgically aware without becoming costume drama
- uncluttered, warm, and modern
- Android-native rather than iOS-mirrored

Avoid:

- kitsch
- fantasy-game aesthetics
- meme energy
- over-saturated neon palettes
- fake UI collages that imply screens or states we do not ship

## Source Of Truth By Asset Type

### Must Come From The Real App

- phone screenshots for the Play listing
- any future screenshots used in release docs
- any UI crops that imply actual product behavior

### May Use Codex Image Generation

- `1024x500` feature graphic
- optional screenshot framing backgrounds that do not alter the underlying real UI capture
- later devotional art that is actually integrated into the shipped Android app

### Must Stay Consistent With Shipped Identity

- Play icon export
- launcher-derived brand colors
- store listing name and premium messaging

## Asset Inventory

Store the working files under:

- `release/play-assets/generated`
- `release/play-assets/screenshots/raw`
- `release/play-assets/screenshots/final`
- `release/play-assets/prompts`

### Required Play Assets

| Asset | Source | Output | Notes |
| --- | --- | --- | --- |
| Play icon | existing launcher identity | `512x512 PNG` | Flattened export, no redesign unless scale testing proves a real problem |
| Feature graphic | Codex image generation | `1024x500 PNG` | Sacred editorial, no fake review badges or deceptive claims |
| Today screenshot | real Android capture | PNG/JPEG per Play | Must show actual Today dashboard |
| Fasting Days screenshot | real Android capture | PNG/JPEG per Play | Must show real calendar/observance content |
| Track Fast screenshot | real Android capture | PNG/JPEG per Play | Prefer an active or meaningful tracker state |
| Reminder Center screenshot | real Android capture | PNG/JPEG per Play | Must show truthful reminder controls |
| Premium workspace screenshot | real Android capture | PNG/JPEG per Play | Show actual premium screen state |
| Privacy & Data screenshot | real Android capture | PNG/JPEG per Play | Must reflect current local-only scope |

## Naming Conventions

Use lowercase kebab-case names:

- `play-icon-512.png`
- `feature-graphic-sacred-editorial-v1.png`
- `screenshot-01-today.png`
- `screenshot-02-fasting-days.png`
- `screenshot-03-track-fast.png`
- `screenshot-04-reminder-center.png`
- `screenshot-05-premium.png`
- `screenshot-06-privacy-data.png`

For raw captures, use:

- `raw-today-1080x2400.png`
- `raw-fasting-days-1080x2400.png`

## Screenshot Capture Spec

Use one phone profile for the submission set so the listing feels coherent.

Recommended current capture baseline:

- device class: medium Android phone
- orientation: portrait
- language: English (`en-US`) for the first asset set
- theme: the currently shipped app theme
- status bar: keep clean and non-distracting

### Screenshot Order

1. Today dashboard
2. Fasting Days
3. Track Fast
4. Reminder Center
5. Premium workspace
6. Privacy & Data

### Capture Requirements By Screen

#### 1. Today dashboard

- Route: `catholicfasting://open/today`
- Required state: onboarding complete, meaningful observance/day data present
- Premium state: unlocked if that improves truthfully visible dashboard depth, otherwise use current default
- Framing: keep the main observance card and seasonal/devotional area visible

#### 2. Fasting Days

- Route: `catholicfasting://open/calendar`
- Required state: populated observance list with rationale/metadata visible
- Premium state: not required
- Framing: keep the title and the first meaningful observance cluster in frame

#### 3. Track Fast

- Route: `catholicfasting://open/tracker`
- Required state: active or meaningful tracker session preferred
- Premium state: whichever reflects the actual tracker flow being shown
- Framing: show the primary tracker state, not an empty shell if avoidable

#### 4. Reminder Center

- Route: `catholicfasting://open/more/setup`
- Required state: reminder controls visible and truthful
- Premium state: use whatever the current shipped controls actually require
- Framing: focus on reminder-tier and scheduling controls rather than the whole settings stack

#### 5. Premium workspace

- Route: `catholicfasting://open/more/premium`
- Required state: actual premium screen with current subscription messaging
- Premium state: capture either unlocked or locked intentionally, but do not fake entitlement
- Framing: keep summary/workspace sections readable at Play preview size

#### 6. Privacy & Data

- Route: `catholicfasting://open/more/privacy`
- Required state: local-only/privacy messaging visible
- Premium state: not relevant
- Framing: show the privacy and data-health area that supports the local-first story

## Screenshot Truth Policy

- Do not paint fake notification badges, counters, or progress.
- Do not show debug banners, fake toolbars, or impossible data combinations.
- Do not composite multiple screens into one fake screenshot.
- Light framing outside the screenshot is allowed only if the screenshot itself remains unaltered and clearly primary.

## Generated Feature Graphic Policy

The feature graphic may use generated art, but it must:

- keep one clear focal point
- avoid fake app screenshots unless the real UI is clearly secondary and truthful
- avoid badges, ranking claims, review stars, or “best app” language
- avoid medical before/after implications
- avoid official-Church implication

Recommended direction:

- soft parchment and stone neutrals
- restrained liturgical reds and golds
- painterly sacred atmosphere
- negative space that keeps the listing calm and readable

## Prompt Guardrails For Codex Generation

Every prompt should reinforce:

- sacred editorial
- reverent, uncluttered composition
- liturgical color restraint
- calm modern Android-adjacent polish
- no kitsch
- no fantasy game cover look
- no stock-photo collage look
- no direct imitation of famous devotional paintings

## Export And Validation Rules

Before upload:

- verify icon is `512x512`
- verify feature graphic is `1024x500`
- verify screenshots meet current Play accepted dimensions and formats
- verify screenshots remain readable at reduced preview size
- verify generated marketing art does not imply unsupported features

## Follow-On In-App Devotional Art Pass

After the Play asset pack is complete, use the same visual direction for actual in-app Catholic art integrated into Android.

Use the existing Android structures rather than inventing parallel models:

- `SeasonalHeroState`
- `SeasonalContentPack`
- `SacredImageryItem`
- `SacredImageryCatalog`

Priority in-app surfaces:

1. Today seasonal hero
2. Today devotional gallery
3. onboarding devotional preview
4. premium seasonal summary surfaces
5. guidance/devotional highlight surfaces

This second pass must keep:

- current quotes and formation lines
- liturgical-season logic
- Android-native presentation patterns

It must not reuse generated art in the listing as if it were already shipped in the app unless that art has actually been integrated.
