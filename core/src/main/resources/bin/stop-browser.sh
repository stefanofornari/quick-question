#!/usr/bin/env bash
#
# Cleanup hook for the kiosk browser.
# Actual process termination is handled by WebChatService via ProcessHandle.
#
# Usage: stop-browser.sh <pid-file>
#
set -euo pipefail

PID_FILE="${1:?pid file required}"

rm -f "$PID_FILE"
