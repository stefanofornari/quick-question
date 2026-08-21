#!/bin/sh

#
# for reference and future work this is how to launch with flatpak
#
exec flatpak run \
  --unshare=ipc \
  --nosocket=wayland \
  --nosocket=session-bus \
  --filesystem=/tmp/.X11-unix \
  org.chromium.Chromium \
  --display=:5 \
  --user-data-dir="/tmp/chromium-disp5" \
  --ozone-platform=x11 \
  --disable-gpu \
  --no-first-run \
  --no-default-browser-check \
  --window-size=601,801 --window-position=0,0 \
  "$@"
