# Figma Handoff Checklist

Catholic Fasting Android uses Figma for visual intent and component anatomy. Compose remains the implementation source of truth. No Figma-to-code generation.

## File

- Figma file: https://www.figma.com/design/IPhHkAUP0B63PnsNz31ip1
- File name: Catholic Fasting Android Design System
- Current Starter-plan structure:
  - `Cover`
  - `Foundations`
  - `Components + States + Handoff`
- Figma libraries:
  - Material 3 Design Kit as Android anatomy reference
  - Material Symbols for icon review
  - Material Theme Builder as color-role and contrast pressure test

## Required Foundations

- Material 3 baseline roles are referenced before app-specific deviations.
- Seasonal tokens match `core:ui` `SeasonTone` values exactly.
- Typography roles match `CatholicFastingTypography`:
  - `heroTitle`
  - `screenTitle`
  - `sectionTitle`
  - `body`
  - `supporting`
  - `utility`
- Spacing matches `CatholicFastingSpacing`:
  - `4dp`
  - `8dp`
  - `12dp`
  - `16dp`
  - `24dp`
  - `32dp`
- Shape documents current app-owned roles:
  - card radius `24dp`
  - control radius `12dp`
  - sheet radius `28dp`
- Elevation remains flat by default unless a Material component owns elevation.
- Motion documents state clarification only:
  - quick `120ms`
  - standard `220ms`
  - deliberate `320ms`

## Required Components

Document anatomy and states for these before screen redesigns are considered ready:

- Bottom navigation
- Section cards
- Companion cards
- Rule-source cards
- Chips
- Buttons
- Text fields
- Reminder controls
- Tracker controls
- Premium cards
- Dialogs
- Sheets

## Required States

Each reusable component should be checked against:

- Light theme
- Dark theme
- Ordinary Time
- Lent
- Advent
- Christmas
- Easter
- Loading
- Empty
- Error or retry
- Premium locked
- Premium unlocked
- Reminder permission denied
- Reminder permission granted
- Active fast
- Inactive fast
- Large font

## Accessibility Expectations

- Screen titles and section titles keep heading semantics in Compose.
- Selected chips expose selected state.
- Controls have readable labels or content descriptions.
- Touch targets follow Material expectations.
- Obligation, completion, warning, and premium state cannot rely on color alone.
- Dense controls must wrap at large font without overlap.
- Rule-source and billing copy remains readable in light and dark.

## Export Rules

- Do not export Figma imagery for app UI unless a screen explicitly needs a bitmap asset.
- Icons should be reviewed against Material Symbols and then implemented as Android vector drawables or Compose icons.
- Token names in Figma must keep Android/Compose code syntax notes when variables are created.
- Screenshot references should link back to approved screenshot baselines when available.

## Guardrails

- Figma does not own rule logic.
- Figma does not own Play Billing behavior.
- Figma does not own reminders, notifications, widgets, shortcuts, or deep links.
- Figma does not own localization behavior or local-first storage.
- Figma does not own analytics, accounts, cloud, backup, export, import, or household-share assumptions.
- No literal Apple glass copying.
- No iPad, Mac, App Store, SwiftUI, or local export scope.
- No generic wellness tracker polish.
- No shame-heavy streak mechanics.
- No decorative sacred imagery used as a substitute for clear guidance.

## Handoff Loop

1. Update Figma foundation or component anatomy.
2. Update `DESIGN.md` if a product design rule changed.
3. Update `core:ui` tokens or wrappers.
4. Add or update preview fixtures.
5. Add or update approved screenshot baselines when visual drift matters.
6. Add unit or Compose UI tests only when behavior, persistence, semantics, or accessibility changed.
7. Add Code Connect mappings only after Compose component APIs stabilize.

## Screenshot Commands

Visual regression is intentionally not wired right now. The previous screenshot tooling was removed because it did not provide a useful AGP 9.2.1 record/compare/verify gate. Adopt Paparazzi or another screenshot tool only when it gives stable baselines for Today companion, Track Fast inactive/active/recap, and onboarding intention.
