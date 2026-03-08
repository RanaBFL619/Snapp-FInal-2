#!/bin/bash
# Uninstall old Snapp app and install the new build (so you see the new login UI).
# Run from project root:  ./androidApp/install-fresh.sh
# Or from androidApp:     ./install-fresh.sh

set -e
cd "$(dirname "$0")/.." || exit 1

ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
if [[ ! -x "$ADB" ]]; then
  echo "adb not found at $ADB"
  echo "Set ANDROID_HOME or ensure Android SDK is at ~/Library/Android/sdk"
  exit 1
fi

echo "Uninstalling old Snapp app..."
"$ADB" uninstall com.snapp.android 2>/dev/null || true

echo "Clean build and install (so new UI is in the APK)..."
./gradlew clean :androidApp:installDebug --no-daemon

echo "Done. Open the Snapp app on the emulator to see the new login screen."
