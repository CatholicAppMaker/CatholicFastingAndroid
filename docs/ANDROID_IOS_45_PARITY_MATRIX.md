# Android / iOS 4.5 Closed-Test Parity Matrix

This matrix compares the Android closed-test target against the approved iOS
4.5 phone app in `/Users/kevpierce/Desktop/CatholicFastingApp`.

## Release Baselines

| Platform | Version | Build | Scope |
| --- | --- | --- | --- |
| iOS | `4.5` | `10` | Approved iPhone app; iPad and Mac are reference only for Android |
| Android | `1.0.0` | `10003` | First Android phone release candidate for Google Play closed testing |

## Phone Parity

| Area | iOS 4.5 reference | Android closed-test status |
| --- | --- | --- |
| Onboarding | Tight first-run completion gate and required setup before landing in the app | Matched; onboarding/setup persistence and fresh-install flows are covered by unit and connected tests |
| Today | Daily obligation, pastoral notice, seasonal quote/formation content, and local progress state | Matched; Android keeps Material phone layout and shared seasonal content |
| Calendar / Fasting Days | Planning view for required and upcoming observances | Matched; Android uses native phone navigation and calendar filters |
| Guidance | U.S./Canada rule guidance, citations, regional caveats, and devotional context | Matched for phone release; Android keeps local rule engines and regional tests |
| Track Fast | Intermittent fast presets, active fast state, and completion history | Matched with Android-native persistent notification actions |
| Premium | Subscription-gated planning, analytics, reflection, recovery, and restore/manage flows | Matched for subscriptions; support tips are intentionally not shipped or marketed in Android 1.0 closed testing |
| History of Fasting | Reference content surfaced from the app shell | Matched with localized Android history articles and routed More destination |
| Reminders | Required-day and support reminder behavior | Matched through WorkManager scheduling, notification channels, and routing tests |
| Widgets / shortcuts / deep links | Native system entry points | Matched with Android-native Glance widget, shortcuts, notification pending intents, and URI routes |
| Localization | English, Spanish, and French Canadian iOS resources | Android release markets English and Spanish phone support; French Canadian content support exists where implemented but full UI localization is deferred |
| Privacy / data | Local-only storage and delete/reset expectations | Matched for Android first release; backup/export/import/household-share flows remain out of scope |
| Accessibility | Release polish and stable identifiers | Matched for major phone workflows through Compose UI and connected accessibility coverage |
| Store screenshots | iOS 4.5 screenshot-ready Today, Calendar, and Premium states | Android has real Play screenshots captured from the current build; feature graphic approval/upload remains manual |

## Intentional Android Divergences

- Android keeps its Play version independent as `1.0.0` instead of mirroring
  iOS marketing version `4.5`.
- Android uses Play Billing subscription products only for this closed-test
  pass: `cfa_premium_yearly_v3` and `cfa_premium_monthly_v3`.
- Support-tip products from iOS are deferred on Android. The app does not query
  tip product IDs and hides the support-tip section unless future tip offers are
  deliberately configured.
- iPad and Mac layout work from iOS 4.5 is treated as design reference only;
  Android remains phone-first for the closed-test release.

## Remaining Play-Console-Only Work

- Create and activate the two subscription products in the Play app listing.
- Add licensed testers and closed-test accounts.
- Upload the signed AAB, generated feature graphic, and listing screenshots.
- Validate Play-served billing from the closed track and review the pre-launch
  report before widening release.
