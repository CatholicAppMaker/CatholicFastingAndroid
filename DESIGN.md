---
name: Catholic Fasting Android
description: Native Android Catholic fasting guidance with restrained seasonal tone, Material clarity, and local-first beta polish.
colors:
  canvas-light: "#FBF9F1"
  surface-light: "#FFFDF7"
  ink-light: "#171411"
  antique-brass: "#7D5E1E"
  lent-light-container: "#F7F1F5"
  lent-light-content: "#4D315E"
  lent-light-border: "#C8B9C9"
  lent-light-accent: "#6B4776"
  advent-light-container: "#F5F1F6"
  advent-light-content: "#4F3866"
  advent-light-border: "#C5B8CC"
  advent-light-accent: "#674F77"
  christmas-light-container: "#FBF5E8"
  christmas-light-content: "#6B292B"
  christmas-light-border: "#D8C6A1"
  christmas-light-accent: "#8B5D25"
  easter-light-container: "#FBF7E9"
  easter-light-content: "#6E4F1A"
  easter-light-border: "#D5C9A6"
  easter-light-accent: "#8B6B24"
  ordinary-light-container: "#F5F5E8"
  ordinary-light-content: "#2E573B"
  ordinary-light-border: "#B4C2AA"
  ordinary-light-accent: "#496F51"
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

The Android app should feel calm, reverent, practical, native, and trustworthy. It should not feel like generic wellness software or a loud productivity tracker. Use a warm vellum canvas, ink-first typography, restrained seasonal color, close-at-hand citations, readable Material structure, and editorial warmth only where it helps prayerful attention. The Apple app is the family resemblance reference for tone and hierarchy; Android keeps Material navigation, controls, dark theme support, and platform behavior.

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
