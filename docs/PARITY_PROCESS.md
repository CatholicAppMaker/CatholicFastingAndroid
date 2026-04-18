# Android and iOS Parity Process

## Source of Truth

- iOS app location: `/Users/kevpierce/Desktop/CatholicFastingApp`
- Android app location: `/Users/kevpierce/Desktop/CFAAnrdoid`

The iOS app is the product and behavior reference for:

- observance and rule logic
- onboarding/setup intent
- premium workflow depth
- reminder behavior
- local-data semantics
- seasonal and devotional content expectations

Android is free to use Android-native UX and system integrations as long as user-visible outcomes stay aligned.

## Repo Strategy

- Keep Android in its own git repository.
- Keep iOS in its own git repository.
- Do not mix Android and Apple build files, release steps, or unrelated commits.
- Use docs and parity notes to stay aligned instead of forcing a monorepo.

## When to Match iOS Exactly

Match iOS behavior closely for:

- rules calculations
- observance status behavior
- reminder scheduling intent
- premium entitlement behavior
- local storage semantics where user-visible behavior matters

## When Android Should Diverge

Prefer Android-native behavior for:

- widgets
- shortcuts
- notifications and actions
- deep-link routing
- Material and navigation patterns
- Play billing account management

## Working Cadence

1. Check iOS behavior before porting parity-sensitive work.
2. Implement the Android-native version of that behavior.
3. Run the Android cleanup gate.
4. Update `/Users/kevpierce/Desktop/CFAAnrdoid/PROJECT_CHECKLIST.md`.
5. Note any intentional Android divergence in commit messages or PR descriptions.

## Definition of a Good Android Checkpoint

A checkpoint is strong when:

- the feature compiles
- `ktlintCheck`, `detekt`, `lint`, and `testDebugUnitTest` pass
- the checklist reflects the real state
- parity assumptions are documented if behavior intentionally differs from iOS

## Shared Discipline

Use separate repos, but shared product discipline:

- treat iOS as the behavioral reference
- keep Android release-quality on its own terms
- prefer explicit parity notes over silent drift
