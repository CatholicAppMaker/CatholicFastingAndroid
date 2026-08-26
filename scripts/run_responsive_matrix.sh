#!/bin/zsh
set -euo pipefail

# Runs the deterministic responsive matrix instrumentation test and collects
# its six transient evidence captures. The script writes only to app/build,
# which is generated output and is not part of the tracked release assets.

repo_root="$(CDPATH='' cd -- "$(dirname -- "$0")/.." && pwd)"
avd_name="Medium_Phone_API_36.1"
test_class="com.kevpierce.catholicfastingapp.ResponsiveMatrixInstrumentationTest"
remote_dir="/sdcard/cfa-responsive-evidence"
output_dir="$repo_root/app/build/outputs/responsive-evidence"
results_root="$repo_root/app/build/outputs/androidTest-results/connected"
expected_width=1080
expected_height=2400
expected_physical_size="${expected_width}x${expected_height}"
serial=""
timeout_seconds="${RESPONSIVE_TIMEOUT_SECONDS:-1200}"

typeset -a capture_names=(
  "320x720-font200-light-today.png"
  "320x720-font200-dark-track-fast.png"
  "411x891-font140-light-reminder-center.png"
  "411x891-font140-dark-premium.png"
  "600x960-font200-light-fasting-days.png"
  "600x960-font200-dark-privacy-data.png"
)

typeset -a expected_test_names=(
  "compact320x720At100PercentFont"
  "compact320x720At140PercentFont"
  "compact320x720At200PercentFont"
  "reference411x891At100PercentFont"
  "reference411x891At140PercentFont"
  "reference411x891At200PercentFont"
  "tablet600x960At100PercentFont"
  "tablet600x960At140PercentFont"
  "tablet600x960At200PercentFont"
  "captureResponsiveStressEvidence"
)

usage() {
  cat <<EOF
Usage: scripts/run_responsive_matrix.sh [--serial SERIAL] [--timeout-seconds SECONDS]

Runs $test_class on the $avd_name emulator and collects six transient
responsive-evidence PNGs into app/build/outputs/responsive-evidence.

  --serial SERIAL  Select one ready adb device. Without this option, exactly
                   one ready adb device must be connected.
  --timeout-seconds SECONDS
                   Hard host timeout for connected Gradle (default: 1200).
                   Also accepts RESPONSIVE_TIMEOUT_SECONDS.
  -h, --help       Show this help.
EOF
}

while (( $# > 0 )); do
  case "$1" in
    --serial)
      if (( $# < 2 )); then
        echo "--serial requires a device id." >&2
        exit 2
      fi
      serial="$2"
      shift 2
      ;;
    --timeout-seconds)
      if (( $# < 2 )); then
        echo "--timeout-seconds requires an integer." >&2
        exit 2
      fi
      timeout_seconds="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

case "$timeout_seconds" in
  ''|*[!0-9]*)
    echo "Timeout must be an integer number of seconds." >&2
    exit 2
    ;;
esac
if (( timeout_seconds < 60 )); then
  echo "Timeout must be at least 60 seconds." >&2
  exit 2
fi

sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
adb_bin="$sdk_root/platform-tools/adb"
gradle_bin="$repo_root/scripts/gradle"
watchdog_bin="$repo_root/scripts/run_with_watchdog.py"

if [[ ! -x "$adb_bin" ]]; then
  echo "adb not found at $adb_bin" >&2
  exit 1
fi

if [[ ! -x "$gradle_bin" ]]; then
  echo "Gradle launcher not found at $gradle_bin" >&2
  exit 1
fi

if [[ ! -f "$watchdog_bin" ]]; then
  echo "Watchdog helper not found at $watchdog_bin" >&2
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "python3 is required to inspect connected JUnit XML reports." >&2
  exit 1
fi

if [[ -z "$serial" ]]; then
  typeset -a connected_devices
  connected_devices=()
  while IFS= read -r device; do
    [[ -n "$device" ]] && connected_devices+=("$device")
  done < <("$adb_bin" devices | awk 'NR > 1 && $2 == "device" { print $1 }')

  if (( ${#connected_devices[@]} == 0 )); then
    echo "No ready adb device found. Start $avd_name or pass --serial." >&2
    exit 1
  fi
  if (( ${#connected_devices[@]} != 1 )); then
    echo "Expected exactly one ready adb device; found ${#connected_devices[@]}. Pass --serial." >&2
    printf '  %s\n' "${connected_devices[@]}" >&2
    exit 1
  fi
  serial="${connected_devices[1]}"
fi

device_state="$("$adb_bin" -s "$serial" get-state 2>/dev/null | tr -d '\r\n' || true)"
if [[ "$device_state" != "device" ]]; then
  echo "adb device $serial is not ready (state: ${device_state:-unknown})." >&2
  exit 1
fi

device_avd="$("$adb_bin" -s "$serial" shell getprop ro.boot.qemu.avd_name | tr -d '\r\n')"
if [[ "$device_avd" != "$avd_name" ]]; then
  echo "Expected AVD $avd_name; got ${device_avd:-unknown}." >&2
  exit 1
fi

wm_size_raw="$("$adb_bin" -s "$serial" shell wm size | tr -d '\r\n')"
physical_size="$("$adb_bin" -s "$serial" shell wm size | awk -F': ' '/Physical size:/{print $2; exit}' | tr -d '\r\n')"
if [[ "$physical_size" != "$expected_physical_size" ]]; then
  echo "Expected physical device size $expected_physical_size; got ${physical_size:-unknown}." >&2
  echo "wm size: ${wm_size_raw:-unknown}" >&2
  exit 1
fi

density_raw="$("$adb_bin" -s "$serial" shell wm density | tr -d '\r\n')"
sdk_version="$("$adb_bin" -s "$serial" shell getprop ro.build.version.sdk | tr -d '\r\n')"
android_release="$("$adb_bin" -s "$serial" shell getprop ro.build.version.release | tr -d '\r\n')"

device_locale="$("$adb_bin" -s "$serial" shell cmd locale get-device-locale 2>/dev/null | tr -d '\r\n' || true)"
if [[ -z "$device_locale" || "$device_locale" == "null" ]]; then
  device_locale="$("$adb_bin" -s "$serial" shell getprop persist.sys.locale | tr -d '\r\n')"
fi
if [[ -z "$device_locale" || "$device_locale" == "null" ]]; then
  device_locale="$("$adb_bin" -s "$serial" shell getprop ro.product.locale | tr -d '\r\n')"
fi

device_timezone="$("$adb_bin" -s "$serial" shell getprop persist.sys.timezone | tr -d '\r\n')"
night_mode="$("$adb_bin" -s "$serial" shell cmd uimode night 2>/dev/null | tr -d '\r\n' || true)"
global_night_mode="$("$adb_bin" -s "$serial" shell settings get secure ui_night_mode 2>/dev/null | tr -d '\r\n' || true)"
window_animation_scale="$("$adb_bin" -s "$serial" shell settings get global window_animation_scale | tr -d '\r\n')"
transition_animation_scale="$("$adb_bin" -s "$serial" shell settings get global transition_animation_scale | tr -d '\r\n')"
animator_duration_scale="$("$adb_bin" -s "$serial" shell settings get global animator_duration_scale | tr -d '\r\n')"

if [[ "$device_locale" != "en-US" && "$device_locale" != "en-US,"* ]]; then
  echo "Expected en-US locale; got ${device_locale:-unknown}." >&2
  exit 1
fi
if [[ "$device_timezone" != "America/New_York" ]]; then
  echo "Expected America/New_York timezone; got ${device_timezone:-unknown}." >&2
  exit 1
fi
if [[ "$night_mode" != *"No night mode"* && "$night_mode" != *": no"* && "$night_mode" != "no" ]]; then
  echo "Expected physical night mode light; got ${night_mode:-unknown}." >&2
  exit 1
fi
if [[ "$global_night_mode" != "1" && "$global_night_mode" != "no" ]]; then
  echo "Expected global night mode light (ui_night_mode=1); got ${global_night_mode:-unknown}." >&2
  exit 1
fi
for animation_value in "$window_animation_scale" "$transition_animation_scale" "$animator_duration_scale"; do
  if [[ "$animation_value" != "0" && "$animation_value" != "0.0" ]]; then
    echo "Expected all animation scales to be 0 or 0.0; got ${animation_value:-unknown}." >&2
    exit 1
  fi
done

echo "Responsive matrix device:"
echo "  serial=$serial"
echo "  avd=$device_avd"
echo "  sdk=$sdk_version"
echo "  android_release=$android_release"
echo "  locale=${device_locale:-unknown}"
echo "  timezone=${device_timezone:-unknown}"
echo "  physical_size=${physical_size:-unknown}"
echo "  density=${density_raw:-unknown}"
echo "  night_mode=${night_mode:-unknown}"
echo "  global_night_mode=${global_night_mode:-unknown}"
echo "  window_animation_scale=${window_animation_scale:-unknown}"
echo "  transition_animation_scale=${transition_animation_scale:-unknown}"
echo "  animator_duration_scale=${animator_duration_scale:-unknown}"
echo "  timeout_seconds=$timeout_seconds"

work_dir="$(mktemp -d "${TMPDIR:-/tmp}/cfa-responsive-evidence.XXXXXX")"
trap 'rm -rf "$work_dir"' EXIT
staged_dir="$work_dir/responsive-evidence"
mkdir -p "$staged_dir"
mkdir -p "$output_dir"
rm -f "$output_dir/targeted-test-results.txt" "$output_dir/metadata.txt"
for capture_name in "${capture_names[@]}"; do
  rm -f "$output_dir/$capture_name"
done

echo "Running $test_class on $serial ($avd_name) with a ${timeout_seconds}s hard timeout..."
"$adb_bin" -s "$serial" shell rm -rf "$remote_dir"
"$adb_bin" -s "$serial" shell mkdir -p "$remote_dir"
run_started_epoch="$(date +%s)"
watchdog_status=0
(
  cd "$repo_root"
  ANDROID_SERIAL="$serial" python3 "$watchdog_bin" "$timeout_seconds" \
    ./scripts/gradle --no-daemon :app:connectedDebugAndroidTest \
    "-Pandroid.testInstrumentationRunnerArguments.class=$test_class"
) || watchdog_status=$?
if (( watchdog_status != 0 )); then
  if (( watchdog_status == 124 )); then
    diagnostics_path="$output_dir/timeout-diagnostics.txt"
    {
      print -r -- "status=WATCHDOG_TIMEOUT"
      print -r -- "timeout_seconds=$timeout_seconds"
      print -r -- "serial=$serial"
      print -r -- "avd=$device_avd"
      print -r -- "adb_state=$($adb_bin -s "$serial" get-state 2>&1 || true)"
      print -r -- "app_pid=$($adb_bin -s "$serial" shell pidof -s com.kevpierce.catholicfastingapp 2>&1 || true)"
      print -r -- "crash_logcat_begin"
      "$adb_bin" -s "$serial" shell logcat -d -b crash 2>&1 || true
      print -r -- "crash_logcat_end"
    } > "$diagnostics_path"
    echo "Responsive matrix timed out after ${timeout_seconds}s (exit 124)." >&2
    echo "Diagnostics: $diagnostics_path" >&2
  fi
  exit "$watchdog_status"
fi

validate_targeted_junit() {
  python3 - "$results_root" "$output_dir/targeted-test-results.txt" "$test_class" "$run_started_epoch" "${expected_test_names[@]}" <<'PY'
import sys
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

results_root = Path(sys.argv[1])
result_path = Path(sys.argv[2])
target_class = sys.argv[3]
run_started_epoch = int(sys.argv[4])
expected_names = sys.argv[5:]
expected_set = set(expected_names)


def local_name(tag):
    return tag.rsplit("}", 1)[-1]


cases = []
report_files = []
parse_errors = []
failure_count = 0
error_count = 0
skipped_count = 0
ignored_count = 0

for xml_path in sorted(results_root.rglob("TEST-*.xml")):
    if xml_path.stat().st_mtime < run_started_epoch - 2:
        continue
    try:
        root = ET.parse(xml_path).getroot()
    except (ET.ParseError, OSError) as error:
        parse_errors.append(f"{xml_path}: {error}")
        continue

    if local_name(root.tag) not in {"testsuite", "testsuites"}:
        continue

    matching_cases = []
    for testcase in root.iter():
        if local_name(testcase.tag) != "testcase":
            continue
        if testcase.attrib.get("classname") != target_class:
            continue
        matching_cases.append(testcase)

    if not matching_cases:
        continue

    report_files.append(str(xml_path))
    for testcase in matching_cases:
        name = testcase.attrib.get("name", "")
        problems = []
        status = testcase.attrib.get("status", "").lower()
        if status in {"failure", "failed"}:
            failure_count += 1
            problems.append("failure")
        if status == "error":
            error_count += 1
            problems.append("error")
        if status == "skipped":
            skipped_count += 1
            problems.append("skipped")
        if status == "ignored":
            ignored_count += 1
            problems.append("ignored")

        for child in testcase:
            child_name = local_name(child.tag)
            if child_name == "failure":
                failure_count += 1
                problems.append("failure")
            elif child_name == "error":
                error_count += 1
                problems.append("error")
            elif child_name == "skipped":
                skipped_count += 1
                problems.append("skipped")
            elif child_name == "ignored":
                ignored_count += 1
                problems.append("ignored")

        cases.append((name, problems, str(xml_path)))

observed_names = [name for name, _, _ in cases]
name_counts = Counter(observed_names)
duplicate_names = sorted(name for name, count in name_counts.items() if count > 1)
missing_names = sorted(expected_set - set(observed_names))
unexpected_names = sorted(set(observed_names) - expected_set)
case_problem_count = sum(len(problems) for _, problems, _ in cases)

result_path.parent.mkdir(parents=True, exist_ok=True)
with result_path.open("w", encoding="utf-8") as result:
    result.write(f"class={target_class}\n")
    result.write(f"expected_count={len(expected_names)}\n")
    result.write(f"observed_count={len(cases)}\n")
    result.write("expected_names=" + ",".join(expected_names) + "\n")
    result.write("observed_names=" + ",".join(sorted(observed_names)) + "\n")
    result.write("missing_names=" + ",".join(missing_names) + "\n")
    result.write("unexpected_names=" + ",".join(unexpected_names) + "\n")
    result.write("duplicate_names=" + ",".join(duplicate_names) + "\n")
    result.write(f"failures={failure_count}\n")
    result.write(f"errors={error_count}\n")
    result.write(f"skipped={skipped_count}\n")
    result.write(f"ignored={ignored_count}\n")
    result.write(f"case_problem_count={case_problem_count}\n")
    result.write(f"targeted_xml_count={len(report_files)}\n")
    result.write("targeted_xml_files=" + ",".join(report_files) + "\n")
    result.write("parse_errors=" + " | ".join(parse_errors) + "\n")

valid = (
    len(cases) == len(expected_names)
    and set(observed_names) == expected_set
    and not duplicate_names
    and not missing_names
    and not unexpected_names
    and failure_count == 0
    and error_count == 0
    and skipped_count == 0
    and ignored_count == 0
    and case_problem_count == 0
    and not parse_errors
    and len(report_files) == 1
)

with result_path.open("a", encoding="utf-8") as result:
    result.write(f"status={'valid' if valid else 'invalid'}\n")

if not valid:
    print(f"Targeted JUnit result validation failed; see {result_path}.", file=sys.stderr)
    print(f"Observed {len(cases)} testcase(s): {', '.join(sorted(observed_names))}", file=sys.stderr)
    if missing_names:
        print(f"Missing: {', '.join(missing_names)}", file=sys.stderr)
    if unexpected_names:
        print(f"Unexpected: {', '.join(unexpected_names)}", file=sys.stderr)
    if duplicate_names:
        print(f"Duplicate: {', '.join(duplicate_names)}", file=sys.stderr)
    if failure_count or error_count or skipped_count or ignored_count:
        print(
            f"failures={failure_count} errors={error_count} "
            f"skipped={skipped_count} ignored={ignored_count}",
            file=sys.stderr,
        )
    raise SystemExit(1)

print(f"Targeted JUnit results valid: {len(cases)} exact testcases.")
PY
}

echo "Inspecting connected JUnit XML for $test_class..."
if [[ ! -d "$results_root" ]]; then
  echo "Connected JUnit results directory missing: $results_root" >&2
  exit 1
fi
validate_targeted_junit

echo "Collecting instrumentation captures from $remote_dir..."
for capture_name in "${capture_names[@]}"; do
  "$adb_bin" -s "$serial" pull "$remote_dir/$capture_name" "$staged_dir/$capture_name" >/dev/null
done

png_dimensions() {
  local image_path="$1"

  if command -v python3 >/dev/null 2>&1; then
    python3 - "$image_path" <<'PY'
import struct
import sys

with open(sys.argv[1], "rb") as image:
    signature = image.read(8)
    if signature != b"\x89PNG\r\n\x1a\n":
        raise SystemExit("not a PNG")
    chunk_length_bytes = image.read(4)
    chunk_type = image.read(4)
    if len(chunk_length_bytes) != 4 or chunk_type != b"IHDR":
        raise SystemExit("missing PNG IHDR")
    chunk_length = struct.unpack(">I", chunk_length_bytes)[0]
    if chunk_length < 8:
        raise SystemExit("invalid PNG IHDR")
    width, height = struct.unpack(">II", image.read(8))
print(width, height)
PY
    return 0
  fi

  if command -v sips >/dev/null 2>&1; then
    local width height
    width="$(sips -g pixelWidth "$image_path" 2>/dev/null | awk '/pixelWidth:/{print $2}')"
    height="$(sips -g pixelHeight "$image_path" 2>/dev/null | awk '/pixelHeight:/{print $2}')"
    if [[ -n "$width" && -n "$height" ]]; then
      echo "$width $height"
      return 0
    fi
  fi

  echo "Cannot validate PNG dimensions: need python3 or sips." >&2
  return 1
}

sha256() {
  if command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$1" | awk '{print $1}'
  elif command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | awk '{print $1}'
  else
    echo "No SHA-256 utility found (need shasum or sha256sum)." >&2
    return 1
  fi
}

validate_capture_set() {
  local directory="$1"
  local capture_name image_path dimensions width height digest previous
  typeset -A seen_hashes

  for capture_name in "${capture_names[@]}"; do
    image_path="$directory/$capture_name"
    if [[ ! -s "$image_path" ]]; then
      echo "Capture missing or empty: $image_path" >&2
      return 1
    fi

    dimensions="$(png_dimensions "$image_path")"
    width="${dimensions%% *}"
    height="${dimensions##* }"
    if [[ "$width" != "$expected_width" || "$height" != "$expected_height" ]]; then
      echo "Capture has ${width}x${height}; expected ${expected_width}x${expected_height}: $image_path" >&2
      return 1
    fi

    digest="$(sha256 "$image_path")"
    previous="${seen_hashes[$digest]:-}"
    if [[ -n "$previous" ]]; then
      echo "Captures must be distinct; duplicate SHA-256 $digest for $previous and $capture_name." >&2
      return 1
    fi
    seen_hashes[$digest]="$capture_name"
    echo "capture OK: $capture_name (${width}x${height}, sha256=$digest)"
  done
}

validate_capture_set "$staged_dir"

mkdir -p "$output_dir"
for capture_name in "${capture_names[@]}"; do
  cp "$staged_dir/$capture_name" "$output_dir/$capture_name"
done

metadata_path="$output_dir/metadata.txt"
{
  print -r -- "serial=$serial"
  print -r -- "avd=$device_avd"
  print -r -- "sdk=$sdk_version"
  print -r -- "android_release=$android_release"
  print -r -- "locale=${device_locale:-unknown}"
  print -r -- "timezone=${device_timezone:-unknown}"
  print -r -- "physical_size=${physical_size:-unknown}"
  print -r -- "wm_size=${wm_size_raw:-unknown}"
  print -r -- "density=${density_raw:-unknown}"
  print -r -- "night_mode=${night_mode:-unknown}"
  print -r -- "global_night_mode=${global_night_mode:-unknown}"
  print -r -- "window_animation_scale=${window_animation_scale:-unknown}"
  print -r -- "transition_animation_scale=${transition_animation_scale:-unknown}"
  print -r -- "animator_duration_scale=${animator_duration_scale:-unknown}"
} > "$metadata_path"

echo "Responsive evidence collected in $output_dir"
echo "Metadata written to $metadata_path"
for capture_name in "${capture_names[@]}"; do
  echo "final: $capture_name sha256=$(sha256 "$output_dir/$capture_name")"
done
