#!/usr/bin/env bash
#
# Redirects the running kiosk browser to a new URL.
#
set -euo pipefail

usage() {
    cat <<'EOF'
Usage: navigate-browser.sh <url> [--display <display>] [--pid-file <pid-file>] [--profile-dir <profile-dir>] [--browser-bin <browser-bin>]

Redirects the running kiosk browser to a new URL.
EOF
    exit "${1:-0}"
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
    usage 0
fi

DISPLAY_ARG=":5"
PID_FILE=""
PROFILE_DIR=""
BROWSER_BIN=""
URL=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --display)
            if [[ -z "${2:-}" ]]; then
                echo "quickquestion: --display requires a value" >&2
                usage 1
            fi
            DISPLAY_ARG="$2"
            shift 2
            ;;
        --pid-file)
            if [[ -z "${2:-}" ]]; then
                echo "quickquestion: --pid-file requires a value" >&2
                usage 1
            fi
            PID_FILE="$2"
            shift 2
            ;;
        --profile-dir)
            if [[ -z "${2:-}" ]]; then
                echo "quickquestion: --profile-dir requires a value" >&2
                usage 1
            fi
            PROFILE_DIR="$2"
            shift 2
            ;;
        --browser-bin)
            if [[ -z "${2:-}" ]]; then
                echo "quickquestion: --browser-bin requires a value" >&2
                usage 1
            fi
            BROWSER_BIN="$2"
            shift 2
            ;;
        -*)
            echo "quickquestion: unknown option $1" >&2
            usage 1
            ;;
        *)
            if [[ -z "$URL" ]]; then
                URL="$1"
            else
                echo "quickquestion: unexpected argument $1" >&2
                usage 1
            fi
            shift
            ;;
    esac
done

if [[ -z "$URL" ]]; then
    echo "quickquestion: url required" >&2
    usage 1
fi

if [[ -z "$PID_FILE" ]]; then
    echo "quickquestion: --pid-file is required" >&2
    usage 1
fi

if [[ -z "$PROFILE_DIR" ]]; then
    echo "quickquestion: --profile-dir is required" >&2
    usage 1
fi

export DISPLAY="$DISPLAY_ARG"

DEFAULT_DESKTOP=""
if [[ -n "$BROWSER_BIN" ]]; then
    BROWSER="$BROWSER_BIN"
else
    DEFAULT_DESKTOP=$(xdg-settings get default-web-browser 2>/dev/null || xdg-mime query default x-scheme-handler/https 2>/dev/null || true)

    if [[ -z "$DEFAULT_DESKTOP" ]]; then
        echo "quickquestion: could not detect default web browser" >&2
        exit 1
    fi

    BROWSER=""
    for candidate in google-chrome google-chrome-stable chromium chromium-browser firefox; do
        if command -v "$candidate" >/dev/null 2>&1; then
            BROWSER="$candidate"
            break
        fi
    done

    if [[ -z "$BROWSER" ]]; then
        echo "quickquestion: no supported browser found" >&2
        exit 1
    fi
fi

mkdir -p "$PROFILE_DIR"
PROFILE_NAME=$(basename "${DEFAULT_DESKTOP:-browser}" .desktop | tr '[:upper:]' '[:lower:]')
PROFILE_DIR="$PROFILE_DIR/$PROFILE_NAME-profile"
mkdir -p "$PROFILE_DIR"

case "${DEFAULT_DESKTOP_LOWER:-${PROFILE_NAME}}" in
    *chrome*|*chromium*|*brave*|*edge*|*vivaldi*)
        "$BROWSER" --kiosk --display="$DISPLAY" "--user-data-dir=$PROFILE_DIR" "$URL" >/dev/null 2>&1 &
        ;;
    *firefox*|*zen*|*waterfox*)
        "$BROWSER" --kiosk --display="$DISPLAY" "--profile" "$PROFILE_DIR" "$URL" >/dev/null 2>&1 &
        ;;
    *)
        "$BROWSER" --kiosk --display="$DISPLAY" "$URL" >/dev/null 2>&1 &
        ;;
esac

echo $! > "$PID_FILE"
