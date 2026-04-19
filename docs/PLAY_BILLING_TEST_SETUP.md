# Google Play Billing Test Setup

Use this checklist before validating premium purchase and restore behavior.

## Play Console

- Create the subscription products used by the Android app:
  - `com.kevpierce.catholicfasting.premium.yearly.v3`
  - `com.kevpierce.catholicfasting.premium.monthly.v3`
- Create the support-tip in-app products if they will ship in v1.
- Support-tip product ids:
  - `com.kevpierce.catholicfasting.tip.small`
  - `com.kevpierce.catholicfasting.tip.medium`
  - `com.kevpierce.catholicfasting.tip.large`
- Activate products in the same app listing that matches `com.kevpierce.catholicfastingapp`.
- Enable Play App Signing and keep the upload key details stored locally.

## Test accounts

- Add the release Google account as a license tester.
- Add any additional closed-test accounts that need to validate restore and pending-purchase behavior.
- Install the app from a Play testing track before judging billing behavior.

## Validation expectations

- Premium catalog loads real products.
- Purchase flow opens Play checkout successfully.
- Restore/refresh reflects the Play account state.
- Pending purchases stay locked with clear messaging.
- Manage-subscription opens the Play subscriptions page.
