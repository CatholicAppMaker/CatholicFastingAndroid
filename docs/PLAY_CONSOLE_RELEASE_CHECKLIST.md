# Play Console Release Checklist

Use this checklist in order for the first public Android release.

## Required console items

- App category and contact details
  - Suggested category: `Lifestyle`
  - Support website: `https://x.com/CatholicFasting`
- Privacy policy URL
  - `https://x.com/CatholicFasting/status/2026354531273945191`
- Data safety form aligned with the local-only/no-backup scope
- Content rating questionnaire
- App access declaration if needed
- Ads declaration
- Store listing copy and screenshots
- Feature graphic
- Production country availability

## Release checks

- Confirm Play App Signing is enabled for `com.kevpierce.catholicfastingapp`
- Confirm the upload key matches the locally configured release keystore
- Upload `app-release.aab`
- Verify release notes
- Run pre-launch report
- Review crashes, ANRs, accessibility, and large-screen warnings
- Confirm billing products resolve in the same listing
  - `com.kevpierce.catholicfasting.premium.yearly.v3`
  - `com.kevpierce.catholicfasting.premium.monthly.v3`
- Confirm testers can install the same package from Play
- Publish from the production release only after all review warnings are cleared or consciously accepted

## Publish gate

- Final local cleanup gate is green
- Final phone validation sweep is green
- No blocker remains in premium, reminders, widget, or onboarding
