# Android UI Tokens

This document is the concrete token companion to:

- `/Users/kevpierce/Desktop/CFAAnrdoid/docs/ANDROID_DESIGN.md`

Use it when implementing or reviewing Android UI styling decisions.

## Typography Roles

The shared Compose UI layer defines these roles:

- `heroTitle`
  - serif
  - only for featured editorial moments
  - allowed on seasonal hero cards and selected premium summary surfaces
- `screenTitle`
  - sans
  - top-level screen headings
- `sectionTitle`
  - sans
  - card titles and meaningful subsection headers
- `body`
  - sans
  - primary readable content
- `supporting`
  - sans
  - secondary context, recap, summaries, bullets
- `utility`
  - sans
  - citations, diagnostics, metadata, supporting notes

## Serif Usage Rules

Serif is intentionally limited.

Allowed:

- seasonal hero content
- onboarding premium/seasonal preview
- Today featured seasonal card
- premium planning/export summary when featured

Not allowed:

- tracker controls
- reminders and permissions
- calendar detail rows
- diagnostics and privacy/data surfaces
- billing state and error copy
- dense settings forms

## Spacing Scale

The shared spacing scale is fixed at:

- `4`
- `8`
- `12`
- `16`
- `24`
- `32`

Preferred usage:

- `16` for default screen edge padding
- `12` for stacked section spacing on dense working screens
- `8` for standard card internal spacing
- `4` for tightly related supporting content
- `24` and `32` only for larger featured breathing room when truly needed

## Seasonal Accent Rules

Seasonal accents come from liturgical season, not arbitrary per-screen styling.

Accents are allowed on:

- onboarding premium preview
- Today seasonal formation card
- Today season-plan emphasis card
- premium summary card
- premium season-plan card

Accents are not allowed on:

- tracker control surfaces
- reminder-center controls
- billing-state messages
- privacy/data diagnostics
- calendar observance rows
- guidance audit metadata

## Current Shared UI Layer

The token layer currently lives in:

- `/Users/kevpierce/Desktop/CFAAnrdoid/core/ui`

Key types:

- `catholicFastingTheme`
- `CatholicFastingTypography`
- `CatholicFastingSpacing`
- `CatholicFastingCardDefaults`
- `SeasonTone`

Key shared composables:

- `catholicFastingScreenTitle`
- `catholicFastingSectionCard`

## First-Class Screen Targets

The first surfaces intentionally migrated to the shared token layer are:

- onboarding
- Today
- Premium
- Guidance
- Calendar headers and analytics summary
- More section header and shared cards

## Review Rule

When reviewing Android UI code:

1. prefer shared token roles over raw `MaterialTheme.typography.*`
2. prefer shared spacing tokens over one-off dp values
3. keep seasonal accents restrained and intentional
4. keep utility-heavy workflows readable and Android-native
