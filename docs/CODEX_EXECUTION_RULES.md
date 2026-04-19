# Codex Execution Rules

This document is the standing operating agreement for work in the Android repo. It exists to keep execution consistent across long multi-pass sessions.

## Default Execution Mode

- Proceed without re-asking for approval when the user has already approved the plan or said to keep going.
- Do not ask repeated permission or confirmation questions once blanket approval has been given.
- Keep momentum. Continue through implementation, validation, cleanup, and checklist updates unless there is a real blocker that cannot be resolved locally.
- Treat "keep going", "finish it", "do the passes", and similar instructions as authorization to continue end-to-end.

## Communication Rules

- Do not ask unnecessary questions.
- Do not interrupt with approval-style prompts when work can continue directly.
- Use short progress updates while working.
- When a blocker is unavoidable, explain it plainly and briefly, with the smallest possible interruption.

## Console Entry Rules

- Always verify the visible character limit before entering text into Play Console or any other submission UI.
- Never leave over-limit copy in a field and plan to trim it later.
- Treat the live UI counter as the source of truth when a field limit conflicts with memory or older docs.
- If a field limit or accepted answer changes, update the repo submission docs after the current step is resolved.

## Checklist Discipline

- Keep [PROJECT_CHECKLIST.md](/Users/kevpierce/Desktop/CFAAnrdoid/PROJECT_CHECKLIST.md) updated as work progresses.
- Do not mark items complete aspirationally. Only check them off after the implementation and validation for that item are actually done.
- Keep release-validation work aligned with [docs/PHONE_RELEASE_VALIDATION.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PHONE_RELEASE_VALIDATION.md).
- Use [docs/PARITY_PROCESS.md](/Users/kevpierce/Desktop/CFAAnrdoid/docs/PARITY_PROCESS.md) as the parity/rerating reference when running final-mile passes.

## Cleanup Gate

Always do code cleanup after each substantial implementation pass, not only at the end of a larger milestone.

After every substantial pass, run the cleanup gate before counting the pass as complete:

- `ktlintCheck`
- `detekt`
- `lint`
- `testDebugUnitTest`

If cleanup fails:

- fix cleanup immediately
- do not count the pass as complete
- do not rerate upward until the gate is green

## Working Priorities

Prefer the highest rating-moving work first:

1. real user workflow blockers
2. parity gaps affecting daily use
3. release reliability issues
4. Android-native integration polish
5. lower-value cosmetic cleanup

## Platform Direction

- Target is the native Android phone app.
- Favor behavior parity with the iOS app over pixel parity.
- Prefer Android-native delivery where it improves the same user outcome, including notifications, shortcuts, widgets, deep links, and billing/account flows.
- Tablet optimization remains out of scope unless it falls out naturally.

## Failure Prevention

- Do not claim something is done unless it has actually been implemented and validated.
- Do not drift into planning-only mode when the request is to implement.
- Do not stop after partial progress if there is a clear next step available.
- If a previous commitment was missed, correct course in execution rather than restating intentions.

## Current Standing User Instruction

The current standing instruction for this repo is:

- proceed without asking more permission questions
- keep going through the passes
- update the checklist as work is completed
- run cleanup and tests as you go
- focus on getting Android to strong release-ready parity with the iOS phone app
