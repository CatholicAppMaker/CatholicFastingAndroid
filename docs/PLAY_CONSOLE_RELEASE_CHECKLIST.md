# Play Console Release Checklist

## Required console items

- App category and contact details
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

- Upload `app-release.aab`
- Verify release notes
- Run pre-launch report
- Review crashes, ANRs, accessibility, and large-screen warnings
- Confirm billing products resolve in the same listing
  - `com.kevpierce.catholicfasting.premium.yearly.v3`
  - `com.kevpierce.catholicfasting.premium.monthly.v3`
  - optional tips if shipping:
    - `com.kevpierce.catholicfasting.tip.small`
    - `com.kevpierce.catholicfasting.tip.medium`
    - `com.kevpierce.catholicfasting.tip.large`
- Confirm testers can install the same package from Play

## Publish gate

- Final local cleanup gate is green
- Final phone validation sweep is green
- No blocker remains in premium, reminders, widget, or onboarding
