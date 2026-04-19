# Play Submission Runbook

This document is the operator-facing runbook for entering Google Play Console metadata and app-content answers for Catholic Fasting.

Use this alongside:

- [docs/FIRST_PLAY_SUBMISSION.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/FIRST_PLAY_SUBMISSION.md)
- [docs/PLAY_LISTING_COPY.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PLAY_LISTING_COPY.md)
- [docs/PLAY_ASSET_SPEC.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PLAY_ASSET_SPEC.md)
- [docs/PLAY_CONSOLE_RELEASE_CHECKLIST.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PLAY_CONSOLE_RELEASE_CHECKLIST.md)

## Non-Negotiable Rules

- Always verify the visible Google Play character limit before pasting copy into a field.
- Never paste draft text that exceeds the current field limit, even temporarily.
- If a field has a live counter, treat that counter as the source of truth.
- If a field limit changes from what earlier docs or memory suggest, follow the Console UI and update this runbook afterward.
- Keep all Play answers aligned with the current shipped Android app, not aspirational future scope.
- Do not over-claim supported audiences, health scope, access paths, or data handling.

## Current Submission Defaults

- App name: `Catholic Fasting`
- Package name: `com.kevpierce.catholicfastingapp`
- App type: `App`
- Category: `Lifestyle`
- Website: `https://x.com/CatholicFasting`
- Privacy policy: `https://x.com/CatholicFasting/status/2026354531273945191`
- Ads: `No`
- Data safety: `No`, for the current local-first build with no analytics SDK, ad SDK, or backend sync

## Store Listing Copy

Before pasting any of these, confirm the current field limits in Play Console.

### Title

```text
Catholic Fasting
```

Character count: `16 / 30`

### Short Description

```text
Track Catholic fasting, abstinence, reminders, and local-first daily guidance.
```

Character count: `78 / 80`

### Full Description

```text
Catholic Fasting helps you keep a steady Catholic rule of life with a simple daily dashboard, fasting-day calendar, intermittent fast tracker, reminders, and private planning tools.

Built for phone-first Android, the app stays local-first by default. Your observance history, Friday notes, schedules, and reflections stay on-device.

Core features:

- Today dashboard with today’s observance, next required day, setup progress, and seasonal guidance
- Fasting Days calendar with obligation details, rationale, and progress tracking
- Intermittent fast tracker with active notifications and recovery guidance
- Reminder center for required days, support reminders, and daily quote cadence
- Premium planning, analytics, and reflection journal
- Home-screen widget, deep links, and Android shortcuts for fast access

The app is an independent devotional aid and is not an official app of the Church, the USCCB, the Vatican, or any diocese or parish.
```

Character count: `947 / 4000`

## App Content Answers

Use these unless the shipped app changes.

### App Access

- Mark the app as having restricted functionality because premium surfaces are subscription-gated.
- Do not describe the app as login-restricted because it does not require account creation or credentials.
- If the child instruction modal asks whether any other information is required to access the app, use the option indicating no extra credentials or bypass steps are required unless Play explicitly demands a reviewer-access path.

### Content Ratings

- The app does not share precise physical location with other users.
- The app does allow users to purchase digital goods because premium subscriptions are sold in-app.
- The app does not include cash rewards, crypto rewards, or transferable digital assets.
- The app is not a web browser or search engine.
- The app is not primarily a news product.

### Target Audience

- Select only the age groups the shipped product is genuinely designed for.
- If the current release posture remains aligned with the iOS age-guidance approach, prefer `16-17` and `18 and over`.
- Do not include under-13 groups unless the product and policy posture are intentionally built for children.

### Health Apps

- Declare `Nutrition and weight management` for the current fasting/tracking scope.
- Do not claim medical diagnosis, treatment, or condition-management features.
- If Play requests health disclaimers, state clearly that the app is a devotional aid and not a medical device.

### Tags

Pick a small, high-confidence set only. Avoid loose or misleading tags.

Prefer tags that match actual store-listing behavior, such as:

- prayer-related tags if available
- activity or tracking tags if available
- calendar or observance-planning tags if available

Do not use `Religious text` unless the listing genuinely centers on reading religious texts.

## Character-Count Workflow

For every Play Console text field:

1. Read the current field limit shown in the UI.
2. Paste only copy already verified to fit.
3. If the field rejects the copy, shorten it immediately instead of saving an over-limit draft elsewhere.
4. After saving, update this runbook if the effective limit or accepted wording changed.

## Asset Checklist

Prepare and upload:

- app icon
- feature graphic
- Today dashboard screenshot
- Fasting Days calendar screenshot
- Track Fast screenshot
- Reminder center screenshot
- Premium workspace screenshot
- Privacy and Data screenshot

Asset rules:

- Use [docs/PLAY_ASSET_SPEC.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PLAY_ASSET_SPEC.md) as the source of truth for file specs, naming, screenshot order, prompt guardrails, and truthfulness rules.
- Play screenshots must come from the actual Android app.
- Generated art is allowed for the feature graphic, not as a substitute for product screenshots.

## Final Console Discipline

- Save after each section once the UI validates the entry.
- If Play blocks progress, fix the specific invalid field before moving on.
- Do not assume a prior answer was accepted until the section shows as complete in Play Console.
