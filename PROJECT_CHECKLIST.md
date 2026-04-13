# Android Parity Checklist

## Final-Mile Parity
- [x] Dedicated onboarding flow with persisted completion and setup state
- [x] Shared parity state for reminder center, storage diagnostics, seasonal hero, and premium journey scaffolding
- [x] Android-native routing upgrades for deep links, widget taps, shortcuts, and notification actions
- [ ] Billing and release polish for manage-subscription, pending purchases, and failure states
- [ ] Parity tests for onboarding, reminders, premium flows, routing, and storage behavior
- [ ] Fresh-install release validation on phone flows

## Code Cleanup Gate
- [x] `ktlintCheck`
- [x] `detekt`
- [x] `lint`
- [x] `testDebugUnitTest`

## Working Rule
- [x] After each substantial pass, run the cleanup gate before moving on
