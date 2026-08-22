#!/usr/bin/env bash
#
# Launches the system default web browser in full-screen kiosk mode on the
# VNC shared display and navigates to the given URL.
#
set -euo pipefail

usage() {
    cat <<'EOF'
Usage: launch-browser.sh <url> [--display <display>] [--pid-file <pid-file>] [--profile-dir <profile-dir>] [--browser-bin <browser-bin>] [--geometry <WxH>] [--dry-run]

Launches the system default web browser in full-screen kiosk mode on the
VNC shared display and navigates to the given URL.
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
GEOMETRY=""
DRY_RUN=false
URL=""
SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &> /dev/null && pwd)

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
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --geometry)
            if [[ -z "${2:-}" ]]; then
                echo "quickquestion: --geometry requires a value" >&2
                usage 1
            fi
            GEOMETRY="$2"
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

WIDTH=""
HEIGHT=""
if [[ -n "$GEOMETRY" ]]; then
    if [[ ! "$GEOMETRY" =~ ^[0-9]+x[0-9]+$ ]]; then
        echo "quickquestion: invalid --geometry '$GEOMETRY' (expected WxH, e.g. 600x800)" >&2
        usage 1
    fi
    WIDTH="${GEOMETRY%x*}"
    HEIGHT="${GEOMETRY#*x}"
fi

DEFAULT_DESKTOP=""
if [[ -n "$BROWSER_BIN" ]]; then
    EXEC_CMD="$BROWSER_BIN"
else
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
fi

mkdir -p "$PROFILE_DIR"
CMD=("$EXEC_CMD")
PROFILE_NAME=$(basename "${DEFAULT_DESKTOP:-browser}" .desktop | tr '[:upper:]' '[:lower:]')
PROFILE_DIR="$PROFILE_DIR/$PROFILE_NAME-profile"
mkdir -p "$PROFILE_DIR"

# 1. Base browser flags (isolating instance and profile)
case "${DEFAULT_DESKTOP_LOWER:-${PROFILE_NAME}}" in
    *chrome*|*chromium*|*brave*|*edge*|*vivaldi*)
        CMD+=(
            "--new-window"
            "--kiosk"
            "--no-first-run"
            "--user-data-dir=$PROFILE_DIR"
            "--ozone-platform=x11"
            "--display=$DISPLAY_ARG"
        )
        ;;
    *firefox*|*zen*|*waterfox*)
        CMD+=(
            "--no-remote"
            "--new-instance"
            "--kiosk"
            "--profile" "$PROFILE_DIR"
            "--display=$DISPLAY_ARG"
        )
        ;;
    *)
        CMD+=("--kiosk")
        ;;
esac

# 2. Window geometry flags
if [[ -n "$GEOMETRY" ]]; then
    case "${DEFAULT_DESKTOP_LOWER:-${PROFILE_NAME}}" in
        *chrome*|*chromium*|*brave*|*edge*|*vivaldi*)
            CMD+=("--window-position=0,0" "--window-size=$((WIDTH+1)),$((HEIGHT+1))")
            ;;
        *firefox*|*zen*|*waterfox*)
            CMD+=("--width" "$WIDTH" "--height" "$HEIGHT")
            ;;
    esac
fi

# 3. Add URL at the very end
CMD+=("$URL")

#
# Launching VNC server, saving its PID and waiting to be ready
#
VNC_BIN="${SCRIPT_DIR}/../tigervnc-1.16.2.x86_64/usr/bin/Xvnc"
if [[ ! -x "$VNC_BIN" ]]; then
    VNC_BIN="Xvnc"
fi

"$VNC_BIN" "${DISPLAY_ARG}" -geometry "${GEOMETRY:-1280x800}" -depth 24 -SecurityTypes None -ac &
echo $! > "$PID_FILE"

while ! DISPLAY="${DISPLAY_ARG}" xset q &>/dev/null; do
    sleep 0.1
done

#
# Export target display and strip Wayland variables to prevent socket leaks
#
export DISPLAY="$DISPLAY_ARG"
unset WAYLAND_DISPLAY

#
# Finally, launching the browser
#
if [ "$DRY_RUN" = true ]; then
    echo "[DRY-RUN] Command to execute:"
    printf '%q ' "${CMD[@]}"
    echo ""
else
    "${CMD[@]}" >/dev/null 2>&1 &
    disown
fi