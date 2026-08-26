#!/bin/zsh
set -euo pipefail

# Regenerates the six truthful Play screenshots from
# PlayScreenshotInstrumentationTest. The instrumentation test owns fixture
# seeding and route/focal-content assertions; this host script owns device
# preconditions, artifact validation, and promotion into tracked files.

repo_root="$(CDPATH='' cd -- "$(dirname -- "$0")/.." && pwd)"
test_class="com.kevpierce.catholicfastingapp.PlayScreenshotInstrumentationTest"
raw_dir="$repo_root/release/play-assets/screenshots/raw"
final_dir="$repo_root/release/play-assets/screenshots/final"
remote_dir="/sdcard/cfa-play-screenshots"
avd_name="Medium_Phone_API_36.1"
expected_width=1080
expected_height=2400
dry_run=false
serial="${ANDROID_SERIAL:-}"
timeout_seconds="${SCREENSHOT_TIMEOUT_SECONDS:-600}"

typeset -a capture_names=(
  "raw-today-1080x2400.png"
  "raw-fasting-days-1080x2400.png"
  "raw-track-fast-1080x2400.png"
  "raw-reminder-center-1080x2400.png"
  "raw-premium-1080x2400.png"
  "raw-privacy-data-1080x2400.png"
)

typeset -A final_names=(
  [raw-today-1080x2400.png]="screenshot-01-today.png"
  [raw-fasting-days-1080x2400.png]="screenshot-02-fasting-days.png"
  [raw-track-fast-1080x2400.png]="screenshot-03-track-fast.png"
  [raw-reminder-center-1080x2400.png]="screenshot-04-reminder-center.png"
  [raw-premium-1080x2400.png]="screenshot-05-premium.png"
  [raw-privacy-data-1080x2400.png]="screenshot-06-privacy-data.png"
)

usage() {
  cat <<EOF
Usage: scripts/regenerate_play_screenshots.sh [--dry-run] [--serial SERIAL] [--timeout-seconds SECONDS]

Runs $test_class on $avd_name, collects its six raw PNGs, validates the
complete set, and promotes them to release/play-assets/screenshots/final.

  --dry-run       Validate the existing raw directory without running Gradle
                  or changing any tracked files.
  --serial ID     Select one connected adb device (also accepts ANDROID_SERIAL).
  --timeout-seconds SECONDS
                  Hard host timeout for connected Gradle (default: 600).
  -h, --help      Show this help.
EOF
}

while (( $# > 0 )); do
  case "$1" in
    --dry-run)
      dry_run=true
      shift
      ;;
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

if [[ "$dry_run" == false ]]; then
  sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
  adb_bin="$sdk_root/platform-tools/adb"
  if [[ ! -x "$adb_bin" ]]; then
    echo "adb not found at $adb_bin" >&2
    exit 1
  fi

  gradle_bin="$repo_root/scripts/gradle"
  if [[ ! -x "$gradle_bin" ]]; then
    echo "Gradle launcher not found at $gradle_bin" >&2
    exit 1
  fi

  if [[ -z "$serial" ]]; then
    typeset -a connected_devices
    connected_devices=()
    while IFS= read -r device; do
      [[ -n "$device" ]] && connected_devices+=("$device")
    done < <("$adb_bin" devices | awk 'NR > 1 && $2 == "device" { print $1 }')
    if (( ${#connected_devices[@]} == 1 )); then
      serial="${connected_devices[1]}"
    elif (( ${#connected_devices[@]} == 0 )); then
      echo "No connected Android device found. Start $avd_name or pass --serial." >&2
      exit 1
    else
      echo "Multiple Android devices found. Pass --serial to select one." >&2
      printf '  %s\n' "${connected_devices[@]}" >&2
      exit 1
    fi
  fi

  device_state="$($adb_bin -s "$serial" get-state 2>/dev/null | tr -d '\r\n' || true)"
  if [[ "$device_state" != "device" ]]; then
    echo "adb device $serial is not ready (state: ${device_state:-unknown})." >&2
    exit 1
  fi

  device_avd="$($adb_bin -s "$serial" shell getprop ro.boot.qemu.avd_name | tr -d '\r\n')"
  if [[ "$device_avd" != "$avd_name" ]]; then
    echo "Expected AVD $avd_name; got ${device_avd:-unknown}." >&2
    exit 1
  fi

  wm_size="$($adb_bin -s "$serial" shell wm size | tr -d '\r')"
  if [[ "$wm_size" != *"${expected_width}x${expected_height}"* ]]; then
    echo "Expected a ${expected_width}x${expected_height} capture device; got:" >&2
    echo "$wm_size" >&2
    exit 1
  fi

  device_locale="$($adb_bin -s "$serial" shell cmd locale get-device-locale 2>/dev/null | tr -d '\r\n' || true)"
  if [[ -z "$device_locale" || "$device_locale" == "null" ]]; then
    device_locale="$($adb_bin -s "$serial" shell getprop persist.sys.locale | tr -d '\r\n')"
  fi
  if [[ -z "$device_locale" || "$device_locale" == "null" ]]; then
    device_locale="$($adb_bin -s "$serial" shell getprop ro.product.locale | tr -d '\r\n')"
  fi
  if [[ "$device_locale" != "en-US" && "$device_locale" != "en-US,"* ]]; then
    echo "Expected en-US locale; got ${device_locale:-unknown}." >&2
    exit 1
  fi

  device_timezone="$($adb_bin -s "$serial" shell getprop persist.sys.timezone | tr -d '\r\n')"
  if [[ "$device_timezone" != "America/New_York" ]]; then
    echo "Expected America/New_York timezone; got ${device_timezone:-unknown}." >&2
    exit 1
  fi

  night_mode="$($adb_bin -s "$serial" shell cmd uimode night | tr -d '\r\n' || true)"
  if [[ "$night_mode" != *"No night mode"* && "$night_mode" != *": no"* ]]; then
    echo "Expected light theme (uimode night = no); got ${night_mode:-unknown}." >&2
    exit 1
  fi

  for animation_setting in window_animation_scale transition_animation_scale animator_duration_scale; do
    animation_value="$($adb_bin -s "$serial" shell settings get global "$animation_setting" | tr -d '\r\n')"
    if [[ "$animation_value" != "0" && "$animation_value" != "0.0" ]]; then
      echo "Expected $animation_setting=0 for deterministic captures; got ${animation_value:-null}." >&2
      exit 1
    fi
  done
fi

png_dimensions() {
  local image_path="$1"
  if command -v sips >/dev/null 2>&1; then
    local width height
    width="$(sips -g pixelWidth "$image_path" 2>/dev/null | awk '/pixelWidth:/{print $2}')"
    height="$(sips -g pixelHeight "$image_path" 2>/dev/null | awk '/pixelHeight:/{print $2}')"
    if [[ -n "$width" && -n "$height" ]]; then
      echo "$width $height"
      return 0
    fi
  fi

  if command -v python3 >/dev/null 2>&1; then
    python3 - "$image_path" <<'PY'
import struct
import sys

with open(sys.argv[1], "rb") as image:
    signature = image.read(8)
    if signature != b"\x89PNG\r\n\x1a\n":
        raise SystemExit("not a PNG")
    chunk_length = struct.unpack(">I", image.read(4))[0]
    chunk_type = image.read(4)
    if chunk_type != b"IHDR" or chunk_length < 8:
        raise SystemExit("missing PNG IHDR")
    width, height = struct.unpack(">II", image.read(8))
print(width, height)
PY
    return 0
  fi

  echo "Neither sips nor python3 is available to validate PNG dimensions." >&2
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
  local label="$2"
  local capture_name image_path dimensions width height digest previous
  typeset -A seen_hashes

  for capture_name in "${capture_names[@]}"; do
    image_path="$directory/$capture_name"
    if [[ ! -s "$image_path" ]]; then
      echo "$label capture missing or empty: $image_path" >&2
      return 1
    fi
    dimensions="$(png_dimensions "$image_path")"
    width="${dimensions%% *}"
    height="${dimensions##* }"
    if [[ "$width" != "$expected_width" || "$height" != "$expected_height" ]]; then
      echo "$label capture has ${width}x${height}; expected ${expected_width}x${expected_height}: $image_path" >&2
      return 1
    fi
    digest="$(sha256 "$image_path")"
    previous="${seen_hashes[$digest]:-}"
    if [[ -n "$previous" ]]; then
      echo "$label captures must be distinct; duplicate SHA-256 $digest for $previous and $capture_name." >&2
      return 1
    fi
    seen_hashes[$digest]="$capture_name"
    echo "$label OK: $capture_name (${width}x${height}, sha256=$digest)"
  done
}

if [[ "$dry_run" == true ]]; then
  echo "Dry run: validating existing raw captures in $raw_dir"
  validate_capture_set "$raw_dir" "raw"
  echo "Dry run passed. No files changed."
  exit 0
fi

work_dir="$(mktemp -d "${TMPDIR:-/tmp}/cfa-play-screenshots.XXXXXX")"
trap 'rm -rf "$work_dir"' EXIT
staged_raw_dir="$work_dir/raw"
staged_final_dir="$work_dir/final"
mkdir -p "$staged_raw_dir" "$staged_final_dir"

echo "Running deterministic screenshot test on $serial ($avd_name)..."
watchdog_bin="$repo_root/scripts/run_with_watchdog.py"
results_root="$repo_root/app/build/outputs/androidTest-results/connected"
result_path="$repo_root/app/build/outputs/play-screenshot-evidence/results.txt"
mkdir -p "${result_path:h}"
rm -f "$result_path"
"$adb_bin" -s "$serial" shell rm -rf "$remote_dir"
"$adb_bin" -s "$serial" shell mkdir -p "$remote_dir"
run_started_epoch="$(date +%s)"
watchdog_status=0
(
  cd "$repo_root"
  ANDROID_SERIAL="$serial" python3 "$watchdog_bin" "$timeout_seconds" \
    "$gradle_bin" --no-daemon :app:connectedDebugAndroidTest \
    "-Pandroid.testInstrumentationRunnerArguments.class=$test_class"
) || watchdog_status=$?
if (( watchdog_status != 0 )); then
  if (( watchdog_status == 124 )); then
    echo "Screenshot instrumentation timed out after ${timeout_seconds}s (exit 124)." >&2
  fi
  exit "$watchdog_status"
fi

python3 "$repo_root/scripts/validate_connected_junit.py" \
  "$results_root" "$run_started_epoch" "$result_path" \
  --class-name "$test_class" \
  --expected-name captureTruthfulPlayListingSet \
  --require-one-report

echo "Collecting instrumentation screenshots from $remote_dir..."
for capture_name in "${capture_names[@]}"; do
  "$adb_bin" -s "$serial" pull "$remote_dir/$capture_name" "$staged_raw_dir/$capture_name" >/dev/null
done

validate_capture_set "$staged_raw_dir" "fresh raw"

mkdir -p "$raw_dir" "$final_dir"
for capture_name in "${capture_names[@]}"; do
  cp "$staged_raw_dir/$capture_name" "$raw_dir/$capture_name"
  cp "$staged_raw_dir/$capture_name" "$staged_final_dir/${final_names[$capture_name]}"
done

validate_capture_set "$staged_raw_dir" "staged raw"
for capture_name in "${capture_names[@]}"; do
  final_name="${final_names[$capture_name]}"
  dimensions="$(png_dimensions "$staged_final_dir/$final_name")"
  if [[ "$dimensions" != "$expected_width $expected_height" ]]; then
    echo "Staged final has unexpected dimensions for $final_name: $dimensions" >&2
    exit 1
  fi
done

for capture_name in "${capture_names[@]}"; do
  final_name="${final_names[$capture_name]}"
  mv -f "$staged_final_dir/$final_name" "$final_dir/$final_name"
done

echo "Promoted six raw captures and six final screenshots."
for capture_name in "${capture_names[@]}"; do
  final_name="${final_names[$capture_name]}"
  echo "final OK: $final_name ($(png_dimensions "$final_dir/$final_name"), sha256=$(sha256 "$final_dir/$final_name"))"
done
echo "Update release/play-assets/MANIFEST.md with the printed SHA-256 values."
