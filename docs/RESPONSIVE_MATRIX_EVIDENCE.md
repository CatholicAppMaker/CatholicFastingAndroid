# Responsive Matrix Evidence

Status: **PASS**. The responsive instrumentation and final release gates were
observed on the approved emulator on August 22, 2026. Generated evidence is
under `app/build/outputs`; no transient capture is treated as a tracked Play
asset.

## Fixture contract

The instrumentation fixture must use the real app shell and production rules:

- Fixed presentation clock: February 18, 2026.
- Reset `AppContainer` before seeding state.
- Call `repository.updateYear(2026)` so observances come from production rules.
- Onboarding is complete and the region is United States.
- Reminders are balanced, with the quote reminder at 08:00.
- A prayer intention is present.
- A 16-hour fast began one hour before the fixed presentation time.
- Billing remains truthfully locked; no premium entitlement is injected.
- Route and focal-content assertions run after Compose/device idle.
- Navigation uses semantics or UI-tree-derived bounds; no screenshot-coordinate
  interaction is permitted.

## Device metadata

The runner must use exactly one ready `Medium_Phone_API_36.1` emulator with a
physical display of 1080 × 2400 pixels. The values below are populated from
`app/build/outputs/responsive-evidence/metadata.txt` after the run.

| Field | Expected or observed value | Status |
| --- | --- | --- |
| Serial | `emulator-5554` | PASS |
| AVD | `Medium_Phone_API_36.1` | PASS |
| Android SDK | `36` | PASS |
| Android release | `16` | PASS |
| Locale | `en-US` | PASS |
| Timezone | `America/New_York` | PASS |
| Physical size | `1080x2400` | PASS |
| Density | `420 dpi` | PASS |
| Night mode | `no` | PASS |
| Global night mode | `1` (light) | PASS |
| Window animation scale | `0.0` | PASS |
| Transition animation scale | `0.0` | PASS |
| Animator duration scale | `0.0` | PASS |

## Instrumentation method contract

`ResponsiveMatrixInstrumentationTest` must expose exactly these ten `@Test`
method names. The first nine methods each exercise six release routes, yielding
54 automated route scenarios. The tenth method captures the six transient
stress-profile PNGs during the instrumentation suite.

1. `compact320x720At100PercentFont` — 320 × 720 dp at 1.0 font scale.
2. `compact320x720At140PercentFont` — 320 × 720 dp at 1.4 font scale.
3. `compact320x720At200PercentFont` — 320 × 720 dp at 2.0 font scale.
4. `reference411x891At100PercentFont` — 411 × 891 dp at 1.0 font scale.
5. `reference411x891At140PercentFont` — 411 × 891 dp at 1.4 font scale.
6. `reference411x891At200PercentFont` — 411 × 891 dp at 2.0 font scale.
7. `tablet600x960At100PercentFont` — 600 × 960 dp at 1.0 font scale.
8. `tablet600x960At140PercentFont` — 600 × 960 dp at 1.4 font scale.
9. `tablet600x960At200PercentFont` — 600 × 960 dp at 2.0 font scale.
10. `captureResponsiveStressEvidence` — capture six transient route images.

Each route checks representative focal content and controls for horizontal
containment and vertical reachability. It must fail on a crash, missing focal
content, representative-node clipping, unusable Material control, duplicate or
missing progress semantics, or a skipped/assumption-bypassed scenario. It does
not claim pixel inspection of every descendant node.

## Nine-profile route matrix

Each row represents one viewport/font profile. Each route cell is one automated
scenario; the table therefore contains 9 × 6 = 54 scenarios.

| Profile | Viewport (dp) | Font scale | Today | Fasting Days | Active Track Fast | Reminder Center | Guided Premium journey | Privacy & Data |
| --- | --- | ---: | --- | --- | --- | --- | --- | --- |
| M01 | 320 × 720 | 1.0 | R01 · PASS | R02 · PASS | R03 · PASS | R04 · PASS | R05 · PASS | R06 · PASS |
| M02 | 320 × 720 | 1.4 | R07 · PASS | R08 · PASS | R09 · PASS | R10 · PASS | R11 · PASS | R12 · PASS |
| M03 | 320 × 720 | 2.0 | R13 · PASS | R14 · PASS | R15 · PASS | R16 · PASS | R17 · PASS | R18 · PASS |
| M04 | 411 × 891 | 1.0 | R19 · PASS | R20 · PASS | R21 · PASS | R22 · PASS | R23 · PASS | R24 · PASS |
| M05 | 411 × 891 | 1.4 | R25 · PASS | R26 · PASS | R27 · PASS | R28 · PASS | R29 · PASS | R30 · PASS |
| M06 | 411 × 891 | 2.0 | R31 · PASS | R32 · PASS | R33 · PASS | R34 · PASS | R35 · PASS | R36 · PASS |
| M07 | 600 × 960 | 1.0 | R37 · PASS | R38 · PASS | R39 · PASS | R40 · PASS | R41 · PASS | R42 · PASS |
| M08 | 600 × 960 | 1.4 | R43 · PASS | R44 · PASS | R45 · PASS | R46 · PASS | R47 · PASS | R48 · PASS |
| M09 | 600 × 960 | 2.0 | R49 · PASS | R50 · PASS | R51 · PASS | R52 · PASS | R53 · PASS | R54 · PASS |

Acceptance requires all 54 route cells to have observed successful results,
with no skipped, ignored, or assumption-bypassed tests.

## Transient physical-device context captures

The runner writes these files to generated output only. Each file must be
nonempty, a valid PNG, exactly 1080 × 2400 pixels, and distinct by SHA-256.
Hashes are printed by `scripts/run_responsive_matrix.sh` and copied into this
table after the capture run.

These PNGs confirm focal content and light/dark rendering on the physical AVD
display; they are not full forced-viewport renders. At 420 dpi, the 320 dp
profile is letterboxed and the forced 600 dp surface exceeds the 1080 px
physical width. The automated semantics/bounds assertions across all 54 route
scenarios—not PNG edge inspection—are the authoritative containment evidence.

| Filename | Profile and theme | Route | SHA-256 | Status |
| --- | --- | --- | --- | --- |
| `320x720-font200-light-today.png` | 320 × 720 dp · 2.0 · light | Today | `80300e9be1a0a7eaac8f25673dbf850379f3ad1ab553dd2ca2dcefb7085caffc` | PASS |
| `320x720-font200-dark-track-fast.png` | 320 × 720 dp · 2.0 · dark | Active Track Fast | `d824075293f786cf1624783ee6256d500ca58fd095450714df13b8be1ef90f09` | PASS |
| `411x891-font140-light-reminder-center.png` | 411 × 891 dp · 1.4 · light | Reminder Center | `a7fdd29c5cb1ec77410f93fd6acf8f46f92de391445e8c0db29213341888877b` | PASS |
| `411x891-font140-dark-premium.png` | 411 × 891 dp · 1.4 · dark | Guided Premium journey | `9d27d08b5c479002da7721ad8cc61249665b4700b92497de1c5a4899fdf1b05f` | PASS |
| `600x960-font200-light-fasting-days.png` | 600 × 960 dp · 2.0 · light | Fasting Days | `11b30828d949450b17a6b8e0d0815d61835c40644fa23583a54a0d0698aeafca` | PASS |
| `600x960-font200-dark-privacy-data.png` | 600 × 960 dp · 2.0 · dark | Privacy & Data | `5bd1964bb04a60d2a98f79a56ebc1ff75742834721e18ca5d744e1bd88066603` | PASS |

## Release gate record

The release AAB row includes bundletool validation, expected upload-key
identity, nonempty R8 mapping, no legacy sacred artwork, and the
≤12,000,000-byte size limit.

| Gate | Required evidence | Status |
| --- | --- | --- |
| Static checks | ktlint, detekt, lint, unit tests, sacred-art verification, debug assembly, and `git diff --check` | PASS |
| Responsive matrix | 54 route scenarios across the nine profiles with zero skips | PASS · exact 10/10 JUnit methods |
| Full connected suite | Complete connected Android suite with zero failures or skipped tests | PASS · exact 119/119 fresh tests |
| Theme and stress inspection | Light/dark inspection at 320 dp × 2.0, 411 dp × 1.4, and 600 dp × 2.0 | PASS · physical capture limitations documented |
| Screenshot evidence | Six valid, distinct transient captures and metadata | PASS |
| Release AAB | Bundletool, signing, R8 mapping, artwork, and AAB size ≤12,000,000 bytes | PASS · 7,850,176 bytes; mapping 45,287,942 bytes |
| Follow-up audit | Impeccable audit with the responsive evidence issue removed | PASS · 20/20; responsive P2 closed |

## Commands and output

Run the dedicated evidence command from the repository root:

```sh
ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" ./scripts/run_responsive_matrix.sh --timeout-seconds 1200
```

Use `--serial SERIAL` only when selecting one device from a deliberately
multi-device adb session. The command invokes only the responsive connected
test, then validates and collects the six captures into:

```text
app/build/outputs/responsive-evidence/
```

This directory is generated output and is not promoted into tracked Play
assets. The final audit owner must review the command output, metadata file,
route results, and hashes before changing any status in this document.

The runner's host watchdog exits `124` after the configured hard timeout,
terminates only its spawned Gradle process group (`TERM`, then bounded `KILL`),
and writes `timeout-diagnostics.txt`. A timeout is a failed gate and must never
be reported as a skipped or successful matrix run.
