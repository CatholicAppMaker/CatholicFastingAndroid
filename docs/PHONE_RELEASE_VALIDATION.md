# Phone Release Validation

Use this checklist for Android phone release passes. A validation pass is only complete when every item below is explicitly checked on a phone-sized emulator or device.

Validation note:
- The April 15, 2026 pass used a clean emulator install plus automated unit/instrumentation coverage for reminder routing, widget routing, billing state handling, Spanish resource resolution, and local storage diagnostics.
- The April 17, 2026 pass expanded the automated release suite to 14 connected Android tests, adding cold-start/recreation coverage, public deep-link launch coverage, navigation pending-intent coverage, and notification-action coverage.

## Fresh Install

- [x] Uninstall the app and start from a clean install
- [x] Launch into onboarding instead of landing on a stale saved destination
- [x] Confirm the independent-app notice can be reviewed and acknowledged
- [x] Confirm the default region selection and manual region changes keep setup progress coherent
- [x] Confirm the default reminder tier and manual tier changes keep setup progress coherent
- [x] Confirm the onboarding finish action stays blocked until the core setup steps are done
- [x] Confirm the notification-permission action appears when reminders are enabled and permission is still missing
- [x] Confirm the first Today load appears immediately after finishing onboarding

## Reminders And Notifications

- [x] Confirm reminder tier changes refresh the local reminder strategy state
- [x] Confirm required-day reminders keep their local scheduling intent
- [x] Confirm morning and evening support reminders can be enabled and disabled cleanly
- [x] Confirm active-fast notifications update while a fast is running
- [x] Confirm notification actions route into the intended destination

## Routing And Widget

- [x] Confirm deep links open Today, Calendar, Tracker, and More subsections correctly
- [x] Confirm app shortcuts route correctly
- [x] Confirm a cold app launch and activity recreation complete without crashing
- [x] Confirm navigation pending intents open the intended destination
- [x] Confirm the widget reflects the current snapshot after app-state changes
- [x] Confirm widget taps route to the intended screen

## Data, Premium, And Accessibility

- [x] Confirm Android release scope stays local-only without backup, export, import, household-share, or Android system-backup flows
- [x] Confirm premium refresh, restore, and manage-subscription actions behave correctly
- [x] Confirm notification actions only mutate state for the supported active-fast action
- [x] Confirm English and Spanish both render correctly on phone layouts
- [x] Confirm TalkBack labels are understandable for primary cards, actions, chips, and navigation
