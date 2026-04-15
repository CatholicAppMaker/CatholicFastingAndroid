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
- [x] Active fast notification `End Fast` action ends the fast from the system notification
- [x] Active fast notification action clears the persistent notification after ending
- [ ] Widget tap routing validated for Today, Calendar, and Tracker
- [ ] Widget refresh stays in sync after tracker and observance state changes
- [x] Premium export UI keeps generated codes separate from paste/import fields
- [x] Encrypted export generates a usable on-device backup code
- [ ] Export/import round-trip validated with failure handling
- [ ] Storage diagnostics validated against backup/import state
- [ ] Premium locked/unlocked restore and manage-subscription flows validated
- [ ] Localization sweep completed for remaining Android-owned release surfaces
- [ ] Accessibility sweep completed for major phone workflows
- [ ] Final phone release validation sweep completed without blocker regressions

## Code Cleanup Gate
- [x] `ktlintCheck`
- [x] `detekt`
- [x] `lint`
- [x] `testDebugUnitTest`

## Working Rule
- [x] After each substantial pass, run the cleanup gate before moving on
