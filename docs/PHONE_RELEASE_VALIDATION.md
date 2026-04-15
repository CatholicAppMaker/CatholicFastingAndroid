# Phone Release Validation

Use this checklist for Android phone release passes. A validation pass is only complete when every item below is explicitly checked on a phone-sized emulator or device.

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

- [ ] Confirm reminder tier changes refresh the local reminder strategy state
- [ ] Confirm required-day reminders keep their local scheduling intent
- [ ] Confirm morning and evening support reminders can be enabled and disabled cleanly
- [ ] Confirm active-fast notifications update while a fast is running
- [ ] Confirm notification actions route into the intended destination

## Routing And Widget

- [x] Confirm deep links open Today, Calendar, Tracker, and More subsections correctly
- [ ] Confirm app shortcuts route correctly
- [ ] Confirm the widget reflects the current snapshot after app-state changes
- [ ] Confirm widget taps route to the intended screen

## Data, Premium, And Accessibility

- [x] Confirm encrypted export creates a usable backup code
- [ ] Confirm encrypted import restores the intended local data
- [ ] Confirm household share import/export entry points behave correctly
- [ ] Confirm premium refresh, restore, and manage-subscription actions behave correctly
- [ ] Confirm English and Spanish both render correctly on phone layouts
- [ ] Confirm TalkBack labels are understandable for primary cards, actions, chips, and navigation
