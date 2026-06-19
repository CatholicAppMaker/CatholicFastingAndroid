# Android Parity Checklist

## Final-Mile Parity
- [x] Dedicated onboarding flow with persisted completion and setup state
- [x] Shared parity state for reminder center, storage diagnostics, seasonal hero, and premium journey scaffolding
- [x] Android-native routing upgrades for deep links, widget taps, shortcuts, and notification actions
- [x] Billing and release polish for manage-subscription, pending purchases, and failure states
- [x] Parity tests for onboarding, reminders, premium flows, routing, and storage behavior
- [x] Fresh-install release validation on phone flows

## Current Working Passes
- [x] Android/iOS 5.0 Companion parity matrix created for phone outcomes
- [x] Companion snapshot model and rule engine added for Today rule, live fast, formation, recovery, and next action
- [x] Tracker intention continuity added for active and completed intermittent fast sessions
- [x] Today renders Companion-first hierarchy before existing Android dashboard content
- [x] Track Fast supports intention selection, active intention display, review note, and latest recap
- [x] Onboarding includes first fasting intention before setup can finish
- [x] Compose UI tests cover Today companion action routing and Track Fast intention/recap interaction
- [x] Connected fresh-install test covers reminder tier plus first-intention onboarding payoff
- [ ] Visual-regression tool selected only if it provides a real record/verify gate; previous screenshot wiring removed as churn
- [ ] Screenshot baselines recorded for Today companion, Track Fast states, and onboarding intention step after tool selection
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
- [x] MainActivity cold start and recreation are covered by on-device tests
- [x] Public deep links resolve and launch MainActivity without crashing
- [x] Navigation pending intents launch the expected in-app destination
- [x] Active fast notification `End Fast` action ends the fast from the system notification
- [x] Active fast notification action clears the persistent notification after ending
- [x] Unknown notification actions leave the active fast state untouched
- [x] Widget tap routing validated for Today, Calendar, and Tracker
- [x] Widget refresh stays in sync after tracker and observance state changes
- [x] Obsolete backup, export, import, and household-share flows removed to match the current iOS product scope
- [x] Android system backup disabled to match the no-backup privacy scope
- [x] Storage diagnostics validated against current local-only state
- [x] Premium locked/unlocked restore and manage-subscription flows validated
- [x] Localization sweep completed for remaining Android-owned release surfaces
- [x] Accessibility sweep completed for major phone workflows
- [x] Direct parity coverage expanded for growth contracts, liturgical seasons, required-day planning, and intermittent-fast edge cases
- [x] Android testing overhaul expanded the suite to 260+ tests with 75+ connected release-flow tests
- [x] Deterministic repository reset and seeded UI coverage added for onboarding, routing, localization, premium, tracker, guidance, settings, privacy, and history flows
- [x] Local-only 9/10 release-confidence coverage added for billing contracts, premium states, widgets, notifications, shortcuts, deep links, backup/package config, and persistence/recreate flows
- [x] Shared `core:ui` token layer added for typography, spacing, cards, and seasonal tones
- [x] Design-doc pass applied to onboarding, Today, Premium, Guidance, Calendar headers, and the More hub
- [x] Design-system UI tests now cover onboarding, Today, Premium, Guidance, Calendar, Tracker, Settings, and the More hub on device
- [x] Android UI token decisions documented alongside the higher-level design doc
- [x] Final phone release validation sweep completed without blocker regressions
- [x] iOS 4.5 phone parity matrix documented for Android closed-test scope
- [x] Support-tip products excluded from Android 1.0 closed-test catalog and UI

## Code Cleanup Gate
- [x] `ktlintCheck`
- [x] `detekt`
- [x] `lint`
- [x] `testDebugUnitTest`
- [x] targeted connected release gate
- [x] expanded connected parity gate
- [x] `app:connectedDebugAndroidTest`
- [x] local-only 9/10 warning-fail cleanup and connected gates
- [x] dead-test scan has no ignored, disabled, or assumption-bypassed tests

## Play Release Prep
- [x] Public support website selected for Play contact details
- [x] Public privacy-policy URL selected for Play Console
- [x] Release-candidate version set in Gradle
- [x] Local release signing config present and gitignored
- [x] Play asset spec and submission asset rules documented in-repo
- [x] Play icon export and screenshot-capture workflow added to the repo
- [ ] Play Console products created and activated in the production listing
- [ ] Licensed testers and closed-test accounts verified in Play Console
- [x] Final signed `app-release.aab` built successfully
- [ ] Final Play-served billing validation completed from a test track
- [x] Real Play screenshots captured from the Android build
- [ ] Generated feature graphic approved and exported for upload
- [ ] Play listing screenshots and feature graphic uploaded
- [ ] Production rollout started from a reviewed Play release

## Working Rule
- [x] After each substantial pass, run the cleanup gate before moving on
