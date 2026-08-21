#!/usr/bin/env bash
#
# Launches the system default web browser in full-screen kiosk mode on the
# VNC shared display and navigates to the given URL.
#
# Usage: launch-browser.sh <display> <pid-file> <url> [--dry-run]
#
set -euo pipefail

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
    cat <<'EOF'
Usage: launch-browser.sh <display> <pid-file> <url> [--dry-run]

Launches the system default web browser in full-screen kiosk mode on the
VNC shared display and navigates to the given URL.
EOF
    exit 0
fi

DISPLAY_ARG="${1:?display required}"
PID_FILE="${2:?pid file required}"
URL="${3:?url required}"
DRY_RUN=false

if [[ "${4:-}" == "--dry-run" ]]; then
    DRY_RUN=true
fi

export DISPLAY="$DISPLAY_ARG"

DEFAULT_DESKTOP=$(xdg-settings get default-web-browser 2>/dev/null || xdg-mime query default x-scheme-handler/https 2>/dev/null || true)

if [[ -z "$DEFAULT_DESKTOP" ]]; then
    echo "quickquestion: could not detect default web browser" >&2
    exit 1
fi

DESKTOP_PATH=""
XDG_DATA_DIRS="${XDG_DATA_HOME:-$HOME/.local/share}:${XDG_DATA_DIRS:-/usr/local/share:/usr/share}"
IFS=':' read -ra DIRS <<< "$XDG_DATA_DIRS"
for dir in "${DIRS[@]}"; do
    if [[ -f "$dir/applications/$DEFAULT_DESKTOP" ]]; then
        DESKTOP_PATH="$dir/applications/$DEFAULT_DESKTOP"
        break
    fi
done

EXEC_CMD=""
if [[ -n "$DESKTOP_PATH" && -f "$DESKTOP_PATH" ]]; then
    EXEC_CMD=$(grep -m1 '^Exec=' "$DESKTOP_PATH" 2>/dev/null | cut -d'=' -f2- | awk '{print $1}')
fi

if [[ -z "$EXEC_CMD" ]]; then
    if [[ "$DEFAULT_DESKTOP" =~ [Ff]irefox ]]; then
        EXEC_CMD="firefox"
    elif [[ "$DEFAULT_DESKTOP" =~ [Cc]hrome ]]; then
        EXEC_CMD="google-chrome"
    elif [[ "$DEFAULT_DESKTOP" =~ [Cc]hromium ]]; then
        EXEC_CMD="chromium"
    fi
fi

if [[ -z "$EXEC_CMD" ]]; then
    echo "quickquestion: could not extract executable from '$DEFAULT_DESKTOP'" >&2
    exit 1
fi

CMD=("$EXEC_CMD")
case "$DEFAULT_DESKTOP" in
    *chrome*|*chromium*|*brave*|*edge*|*vivaldi*)
        CMD+=("--new-window" "--kiosk" "--no-first-run" "$URL")
        ;;
    *firefox*|*zen*|*waterfox*)
        CMD+=("--new-instance" "--kiosk" "$URL")
        ;;
    *)
        CMD+=("--kiosk" "$URL")
        ;;
esac

if [ "$DRY_RUN" = true ]; then
    echo "[DRY-RUN] Command to execute:"
    printf '%q ' "${CMD[@]}"
    echo ""
else
    "${CMD[@]}" &
    disown
    echo $! > "$PID_FILE"
fi
