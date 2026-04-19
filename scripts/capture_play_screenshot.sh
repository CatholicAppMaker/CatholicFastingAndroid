#!/bin/zsh
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: scripts/capture_play_screenshot.sh <route-key> <output-file>"
  echo "Route keys: today, calendar, tracker, setup, premium, privacy, friday-note"
  exit 1
fi

route_key="$1"
output_file="$2"

package_name="com.kevpierce.catholicfastingapp"
adb_bin="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}/platform-tools/adb"

if [[ ! -x "$adb_bin" ]]; then
  echo "adb not found at $adb_bin"
  exit 1
fi

serial="${ANDROID_SERIAL:-$("$adb_bin" devices | awk 'NR>1 && $2 == "device" { print $1; exit }')}"

if [[ -z "$serial" ]]; then
  echo "No connected Android device or emulator found."
  exit 1
fi

case "$route_key" in
  today) deeplink="catholicfasting://open/today" ;;
  calendar) deeplink="catholicfasting://open/calendar" ;;
  tracker) deeplink="catholicfasting://open/tracker" ;;
  setup) deeplink="catholicfasting://open/more/setup" ;;
  premium) deeplink="catholicfasting://open/more/premium" ;;
  privacy) deeplink="catholicfasting://open/more/privacy" ;;
  friday-note) deeplink="catholicfasting://open/calendar/friday-note" ;;
  *)
    echo "Unknown route key: $route_key"
    exit 1
    ;;
esac

mkdir -p "$(dirname "$output_file")"

"$adb_bin" -s "$serial" shell am start -W -a android.intent.action.VIEW -d "$deeplink" "$package_name" >/dev/null
sleep 2
"$adb_bin" -s "$serial" exec-out screencap -p >"$output_file"

echo "Captured $route_key -> $output_file"
