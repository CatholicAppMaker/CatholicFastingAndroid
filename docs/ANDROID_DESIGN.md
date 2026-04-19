# Android Design Source Of Truth

This document defines how design decisions should be made for the Android app and what information should be pulled from Figma versus code or the iOS app.

## Purpose

Use this doc to keep Android visually coherent without slipping into either:

- blind SwiftUI mirroring
- disconnected one-off Android screens
- vague "match Figma" requests with no stable source of truth

## Design Priority Order

When there is tension between sources, use this order:

1. user-visible product behavior already proven in the iOS app
2. Android-native UX conventions for navigation, sheets, menus, widgets, shortcuts, and notifications
3. Android repo string/resources/theme/component constraints
4. Figma visual and layout guidance

Figma should support implementation, not silently override shipped product behavior.

## What Figma Should Be Used For

Pull from Figma when it contains stable decisions for:

- screen hierarchy and section ordering
- spacing rhythm and card composition
- typography scale and emphasis
- icon usage and illustration direction
- color tokens and seasonal theme accents
- premium upsell layout and content grouping
- screenshot composition for Play listing assets

Figma is especially helpful for:

- Today dashboard card hierarchy
- More hub information architecture
- premium workspace layout consistency
- onboarding step framing
- screenshot/storyboard planning for release marketing

## What Should Not Depend On Figma

Do not make Figma the source of truth for:

- observance/rule logic
- reminder behavior
- premium entitlement rules
- billing states
- widget routing
- notification actions
- deep-link behavior
- accessibility labels
- localization behavior

Those should stay owned by Android code and parity docs.

## Android-Specific Design Rules

Android should intentionally diverge from iOS when it improves the same outcome:

- bottom navigation instead of iOS-specific tab metaphors where needed
- Material cards, sheets, and settings patterns
- notification-first flows for active fasts and reminders
- widget-first glanceable summaries
- shortcut and deep-link entry points
- Play Billing account-management affordances

Do not copy iOS layout literally when Android has a better system pattern.

## Typography Direction

Typography should follow the same product intent as iOS without mirroring Apple typography literally.

The goal is Android-equivalent tone, not font parity.

That means:

- clear and native for controls, forms, lists, and dense content
- more expressive for hero moments, section headers, and devotional or premium surfaces
- unmistakably Android in implementation

## Typography Source Of Truth

Use this priority order for typography decisions:

1. Android readability and Material-native usability
2. the emotional role of the text in the Catholic Fasting product
3. consistency across Android screens
4. iOS tone as a loose emotional reference, not a font-spec reference

Do not try to replicate Apple system fonts or Apple font behavior on Android.

## Android Typography Strategy

Use a native Android sans foundation for most of the app:

- body text
- labels
- buttons
- chips
- settings rows
- calendar details
- tracker controls
- diagnostics

Use a restrained serif accent only where warmth, emphasis, or devotional tone helps:

- display headings
- seasonal hero sections
- devotional callouts
- premium summary cards
- select section titles where a stronger editorial tone is useful

Serif should be intentional and sparse. It should not become the default app font.

## Typography Rules By Surface

Keep these areas utilitarian and highly legible:

- tracker controls and milestones
- calendar obligation details
- reminders and permissions
- settings and privacy/data
- billing and diagnostics states

Allow more character in these areas:

- Today hero and seasonal summary
- premium workspace headers
- guidance/devotional highlights
- launch and store-marketing compositions

## Implementation Guidance

On Android, typography should be implemented as:

- Material typography as the base
- app-level typography roles layered on top
- a small number of named text roles used consistently across screens

Preferred Android text-role buckets:

- display
- section title
- body
- supporting/meta
- utility/data

Avoid ad hoc per-screen font decisions.

## iOS Relationship

The iOS app currently communicates tone through:

- a rounded system feel for most UI
- a serif feel for some featured headings

Android should match that intent by category, not by font family.

The correct Android response is:

- native sans for most UI
- selective serif for featured editorial moments

That is equivalent product design, not mirroring.

## Current Repo Recommendation

The Android app should eventually have a dedicated typography layer that:

- keeps Material/Android-native sans as the default
- introduces serif display styles in a limited, explicit way
- maps those styles to reusable app roles rather than direct one-off usage

The first surfaces to receive that typography treatment should be:

- Today
- Premium
- Guidance
- Calendar section headers

## Minimum Figma Handoff We Actually Need

If Figma exists, the most valuable items to pull are:

- page list with named phone screens
- reusable component inventory
- text styles
- color styles or variables
- spacing/token decisions
- state variants for locked, unlocked, loading, and error states
- any artwork or visual assets used in onboarding, premium, or store screenshots

If those are missing, code should proceed from Android-native patterns plus the iOS product reference.

## Best Android Doc Set

The repo already has:

- parity/process docs
- release docs
- Play store docs
- privacy docs

The missing design-facing source of truth is this file.

If design work grows, add only these two follow-on docs:

- `docs/FIGMA_HANDOFF_CHECKLIST.md`
- `docs/ANDROID_UI_TOKENS.md`

Do not create a large documentation tree unless the design workflow becomes materially more complex.

## Practical Recommendation For This Project

Yes, we should keep an Android design doc.

But it should be a lightweight implementation doc, not a big product-spec rewrite.

For this app, Figma should be treated as:

- a visual reference for layout and polish
- a source for assets and token decisions
- a release-marketing aid for screenshots and store materials

It should not replace:

- `/Users/kevpierce/Desktop/CFAAnrdoid/docs/PARITY_PROCESS.md`
- `/Users/kevpierce/Desktop/CFAAnrdoid/PROJECT_CHECKLIST.md`
- the Kotlin models/rules/tests

## Current Recommendation

Right now the best workflow is:

1. keep iOS behavior as the feature spec
2. keep Android code and tests as the implementation truth
3. use Figma only for visual alignment, component consistency, and marketing assets
4. document intentional Android visual divergences here when they are meaningful
