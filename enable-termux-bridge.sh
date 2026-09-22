#!/data/data/com.termux/files/usr/bin/bash
set -e
mkdir -p "$HOME/.termux"
properties="$HOME/.termux/termux.properties"
if grep -qE '^[[:space:]]*allow-external-apps[[:space:]]*=' "$properties" 2>/dev/null; then
  sed -i -E 's/^[[:space:]]*allow-external-apps[[:space:]]*=.*/allow-external-apps = true/' "$properties"
else
  printf '\nallow-external-apps = true\n' >> "$properties"
fi
termux-reload-settings
echo TERMUX_BRIDGE_ENABLED
