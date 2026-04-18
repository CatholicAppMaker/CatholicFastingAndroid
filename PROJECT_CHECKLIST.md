# Android Parity Checklist

## Final-Mile Parity
- [x] Dedicated onboarding flow with persisted completion and setup state
- [x] Shared parity state for reminder center, storage diagnostics, seasonal hero, and premium journey scaffolding
- [x] Android-native routing upgrades for deep links, widget taps, shortcuts, and notification actions
- [x] Billing and release polish for manage-subscription, pending purchases, and failure states
- [x] Parity tests for onboarding, reminders, premium flows, routing, and storage behavior
- [x] Fresh-install release validation on phone flows

## Current Working Passes
- [x] Fresh install reaches onboarding instead of stale app state
- [x] Independent app notice acknowledgement is persisted
- [x] Onboarding completion stays blocked until required setup is complete
- [x] First-run reminder permission CTA is shown when needed
- [x] Finishing onboarding lands on Today
- [x] Region and reminder tier changes keep setup progress coherent
- [x] Reminder notifications route to the correct top-level destination
- [x] Reminder notification copy is resource-backed in English and Spanish
- [x] Deep-link and widget snapshot route contracts are covered by unit tests
- [x] Deep links open Today, Calendar, Tracker, and More subsections correctly
- [x] App shortcuts are registered with the intended deep-link destinations
- [x] Active fast notification `End Fast` action ends the fast from the system notification
- [x] Active fast notification action clears the persistent notification after ending
- [x] Widget tap routing validated for Today, Calendar, and Tracker
- [x] Widget refresh stays in sync after tracker and observance state changes
- [x] Obsolete backup, export, import, and household-share flows removed to match the current iOS product scope
- [x] Android system backup disabled to match the no-backup privacy scope
- [x] Storage diagnostics validated against current local-only state
- [x] Premium locked/unlocked restore and manage-subscription flows validated
- [x] Localization sweep completed for remaining Android-owned release surfaces
- [x] Accessibility sweep completed for major phone workflows
- [x] Final phone release validation sweep completed without blocker regressions

## Code Cleanup Gate
- [x] `ktlintCheck`
- [x] `detekt`
- [x] `lint`
- [x] `testDebugUnitTest`

## Working Rule
- [x] After each substantial pass, run the cleanup gate before moving on
