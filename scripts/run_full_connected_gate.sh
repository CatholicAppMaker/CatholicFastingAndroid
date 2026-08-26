#!/bin/zsh
set -euo pipefail

repo_root="$(CDPATH='' cd -- "$(dirname -- "$0")/.." && pwd)"
timeout_seconds="${CONNECTED_TIMEOUT_SECONDS:-3600}"
expected_count="${FULL_CONNECTED_EXPECTED_TESTS:-119}"
serial="${ANDROID_SERIAL:-}"
results_root="$repo_root/app/build/outputs/androidTest-results/connected"
output_dir="$repo_root/app/build/outputs/full-connected-evidence"

usage() {
  echo "Usage: scripts/run_full_connected_gate.sh [--serial SERIAL] [--timeout-seconds SECONDS] [--expected-count COUNT]"
}

while (( $# > 0 )); do
  case "$1" in
    --serial)
      serial="${2:?--serial requires a device id}"
      shift 2
      ;;
    --timeout-seconds)
      timeout_seconds="${2:?--timeout-seconds requires an integer}"
      shift 2
      ;;
    --expected-count)
      expected_count="${2:?--expected-count requires an integer}"
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
  ''|*[!0-9]*) echo "Timeout must be an integer number of seconds." >&2; exit 2 ;;
esac
if (( timeout_seconds < 60 )); then
  echo "Timeout must be at least 60 seconds." >&2
  exit 2
fi
case "$expected_count" in
  ''|*[!0-9]*) echo "Expected test count must be an integer." >&2; exit 2 ;;
esac
if (( expected_count < 1 )); then
  echo "Expected test count must be positive." >&2
  exit 2
fi

sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
adb_bin="$sdk_root/platform-tools/adb"
if [[ ! -x "$adb_bin" ]]; then
  echo "adb not found at $adb_bin" >&2
  exit 1
fi

if [[ -z "$serial" ]]; then
  typeset -a devices=()
  while IFS= read -r device; do
    [[ -n "$device" ]] && devices+=("$device")
  done < <("$adb_bin" devices | awk 'NR > 1 && $2 == "device" { print $1 }')
  if (( ${#devices[@]} != 1 )); then
    echo "Expected exactly one ready adb device; found ${#devices[@]}. Pass --serial." >&2
    exit 1
  fi
  serial="${devices[1]}"
fi

state="$("$adb_bin" -s "$serial" get-state 2>/dev/null | tr -d '\r\n' || true)"
if [[ "$state" != "device" ]]; then
  echo "adb device $serial is not ready (state: ${state:-unknown})." >&2
  exit 1
fi

mkdir -p "$output_dir"
rm -f "$output_dir/results.txt" "$output_dir/timeout-diagnostics.txt"
if [[ -d "$results_root" ]]; then
  find "$results_root" -type f -name 'TEST-*.xml' -delete
fi
run_started_epoch="$(date +%s)"
gate_status=0
(
  cd "$repo_root"
  ANDROID_SERIAL="$serial" python3 scripts/run_with_watchdog.py "$timeout_seconds" \
    ./scripts/gradle --no-daemon :app:connectedDebugAndroidTest
) || gate_status=$?

if (( gate_status != 0 )); then
  if (( gate_status == 124 )); then
    {
      print -r -- "status=WATCHDOG_TIMEOUT"
      print -r -- "timeout_seconds=$timeout_seconds"
      print -r -- "serial=$serial"
      print -r -- "adb_state=$("$adb_bin" -s "$serial" get-state 2>&1 || true)"
      print -r -- "crash_logcat_begin"
      "$adb_bin" -s "$serial" shell logcat -d -b crash 2>&1 || true
      print -r -- "crash_logcat_end"
    } > "$output_dir/timeout-diagnostics.txt"
    echo "Full connected gate timed out after ${timeout_seconds}s (exit 124)." >&2
  fi
  exit "$gate_status"
fi

python3 "$repo_root/scripts/validate_connected_junit.py" \
  "$results_root" "$run_started_epoch" "$output_dir/results.txt" \
  --expected-count "$expected_count"
echo "Full connected gate passed with fresh, nonempty JUnit evidence: $output_dir/results.txt"
