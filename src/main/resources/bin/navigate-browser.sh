#!/usr/bin/env bash
#
# Redirects the running kiosk browser to a new URL.
#
# Usage: navigate-browser.sh <display> <pid-file> <url>
#
set -euo pipefail

DISPLAY_ARG="${1:?display required}"
PID_FILE="${2:?pid file required}"
URL="${3:?url required}"

if [ ! -f "$PID_FILE" ]; then
  echo "quickquestion: browser not launched (no pid file)" >&2
  exit 1
fi

export DISPLAY="$DISPLAY_ARG"

BROWSER=""
for candidate in google-chrome google-chrome-stable chromium chromium-browser firefox; do
  if command -v "$candidate" >/dev/null 2>&1; then
    BROWSER="$candidate"
    break
  fi
done

if [ -z "$BROWSER" ]; then
  echo "quickquestion: no supported browser found" >&2
  exit 1
fi

"$BROWSER" --kiosk --display="$DISPLAY" "$URL" >/dev/null 2>&1 &
