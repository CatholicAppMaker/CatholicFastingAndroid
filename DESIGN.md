---
name: Catholic Fasting Android
description: Native Android Catholic fasting guidance with restrained seasonal tone, Material clarity, and local-first beta polish.
colors:
  lent-light-container: "#F4EFF9"
  lent-light-content: "#2F2340"
  lent-light-border: "#C6B5DA"
  lent-light-accent: "#6F528C"
  advent-light-container: "#EEF3FA"
  advent-light-content: "#243149"
  advent-light-border: "#BCCBE0"
  advent-light-accent: "#49688E"
  christmas-light-container: "#FFF7E9"
  christmas-light-content: "#47371A"
  christmas-light-border: "#E1CFA5"
  christmas-light-accent: "#9A772A"
  easter-light-container: "#FFF1EA"
  easter-light-content: "#4A2D21"
  easter-light-border: "#E6C1AE"
  easter-light-accent: "#B66B45"
  ordinary-light-container: "#EEF8F0"
  ordinary-light-content: "#1F3A27"
  ordinary-light-border: "#BAD7C0"
  ordinary-light-accent: "#4D7D57"
  lent-dark-container: "#3C3248"
  lent-dark-content: "#F1E9F9"
  lent-dark-border: "#8D72A8"
  lent-dark-accent: "#C7A4E4"
  advent-dark-container: "#2D3648"
  advent-dark-content: "#E7EEFB"
  advent-dark-border: "#6F87A8"
  advent-dark-accent: "#AEC6E8"
  christmas-dark-container: "#3E3525"
  christmas-dark-content: "#FAF0D8"
  christmas-dark-border: "#B09760"
  christmas-dark-accent: "#E6C67A"
  easter-dark-container: "#3D322A"
  easter-dark-content: "#FBEEE4"
  easter-dark-border: "#C48E6A"
  easter-dark-accent: "#F2B28D"
  ordinary-dark-container: "#22392A"
  ordinary-dark-content: "#E3F4E8"
  ordinary-dark-border: "#5D9A70"
  ordinary-dark-accent: "#A9D7B5"
typography:
  heroTitle: "Serif display accent for seasonal hero and select devotional moments"
  screenTitle: "Material-native sans for top-level screen headings"
  sectionTitle: "Sans title role for cards and meaningful subsections"
  body: "Sans readable content"
  supporting: "Sans secondary context, recap, summaries, bullets"
  utility: "Sans citations, diagnostics, metadata, supporting notes"
spacing:
  scale: [4, 8, 12, 16, 24, 32]
rounded:
  card: "Material card defaults unless a shared component defines a tighter radius"
---

# Design

## Register

product

## Design Priority

Use this order when sources disagree:

1. user-visible behavior already proven in the iOS app
2. Android-native UX conventions for navigation, sheets, menus, widgets, shortcuts, notifications, and Play Billing
3. Android repo strings, resources, theme, and shared component constraints
4. Figma visual and layout guidance

Figma supports implementation; it does not override shipped product behavior, rule logic, reminders, entitlement rules, widget routing, notification actions, deep links, accessibility labels, or localization behavior.

## Visual Direction

The Android app should feel calm, reverent, practical, native, and trustworthy. It should not feel like generic wellness software or a loud productivity tracker. Use restrained seasonal color, close-at-hand citations, readable Material structure, and editorial warmth only where it helps prayerful attention.

## Typography

Use native Android sans for body text, labels, buttons, chips, settings rows, calendar details, tracker controls, diagnostics, and utility-heavy flows. Use a restrained serif accent only for featured editorial moments: Today hero content, seasonal/devotional callouts, selected premium summary surfaces, and onboarding preview content.

Do not use serif for tracker controls, reminders and permissions, calendar detail rows, diagnostics, privacy/data surfaces, billing state, or error copy.

## Spacing

The shared spacing scale is 4, 8, 12, 16, 24, and 32. Prefer 16 for screen edge padding, 12 for stacked section spacing on dense working screens, 8 for card internal spacing, 4 for tightly related supporting content, and 24 or 32 only when larger featured breathing room is truly needed.

## Seasonal Accent Rules

Seasonal accents come from liturgical season, not arbitrary per-screen styling. They are allowed on onboarding premium previews, Today seasonal formation, Today season-plan emphasis, premium summary, and premium season-plan surfaces. They are not for tracker controls, reminder-center controls, billing-state messages, privacy/data diagnostics, calendar observance rows, or guidance audit metadata.

## Shared UI Layer

The token layer lives in `core/ui`. Key shared concepts include `catholicFastingTheme`, `CatholicFastingTypography`, `CatholicFastingSpacing`, `CatholicFastingCardDefaults`, and `SeasonTone`. Review UI code by preferring shared token roles over raw Material typography, shared spacing over one-off dp values, restrained seasonal accents, and Android-native readability for utility-heavy workflows.

## First-Class Review Targets

Prioritize design/style review for Today Companion, Track Fast inactive and active states, Track Fast recap, onboarding intention setup, Premium planning, Guidance citations, More settings/privacy/history, and release screenshots.
