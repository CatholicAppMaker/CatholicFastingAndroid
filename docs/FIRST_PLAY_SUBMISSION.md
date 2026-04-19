# First Google Play Submission

Use this flow if you already have the verified Google Play developer account but have only shipped on iOS before.

## 1. Create or verify the Play app

1. Open Play Console.
2. Create the app with package name `com.kevpierce.catholicfastingapp` if it does not already exist.
3. Set the app name to `Catholic Fasting`.
4. Choose `App` and `Paid` or `Free` to match the listing strategy already planned.
   - Because billing is in-app subscription billing, the app itself can still be free to install.

## 2. Finish the Store Presence fields

1. Use the copy from `docs/PLAY_LISTING_COPY.md`.
2. Set the support website to `https://x.com/CatholicFasting`.
3. Set the privacy policy URL to `https://x.com/CatholicFasting/status/2026354531273945191`.
4. Choose `Lifestyle` unless Play presents a better-fitting religion/devotional category at submission time.
5. Upload phone screenshots for:
   - Today dashboard
   - Fasting Days calendar
   - Track Fast
   - Reminder center
   - Premium workspace
   - Privacy & Data
6. Upload a feature graphic before the production release is finalized.

## 3. Finish Policy and App Content

1. Ads: choose `No`.
2. App access: choose `No special access needed` unless Play flags a specific flow.
3. Data safety: describe the app as local-first, with no ad tracking, no backup/export/import, and no household-share flows.
4. Content rating: complete honestly as a devotional/religious utility app.

## 4. Enable Signing and Billing

1. Enable Play App Signing.
2. Register the upload key that matches the local release keystore.
3. Create these subscriptions in the same Play app:
   - `com.kevpierce.catholicfasting.premium.yearly.v3`
   - `com.kevpierce.catholicfasting.premium.monthly.v3`
4. Do not create or market support-tip products for this first release.
5. Add your Google account as a license tester.

## 5. Upload and Review the Release

1. Build the signed bundle locally:
   - `app/build/outputs/bundle/release/app-release.aab`
2. Create a new production release.
3. Upload the `.aab`.
4. Add concise first-release notes.
5. Wait for Play to process the artifact.
6. Review:
   - pre-launch report
   - crashes / ANRs
   - billing product resolution
   - accessibility warnings
   - large-screen warnings
7. Publish only after the local validation gate and Play warnings are both in acceptable shape.

## 6. Final local gate before publish

Run the local release checks before you trust the bundle:

- `ktlintCheck`
- `detekt`
- `lint`
- `testDebugUnitTest`
- the connected Android test suites already documented in `docs/ANDROID_RELEASE.md`
- the manual phone checklist in `docs/PHONE_RELEASE_VALIDATION.md`
