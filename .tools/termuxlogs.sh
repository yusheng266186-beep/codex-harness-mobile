#!/data/data/com.termux/files/usr/bin/bash
# Inspect Termux's own log for RUN_COMMAND / PendingIntent handling.
set +e
LOG=$HOME/.termux/termux-log.txt
echo "=== Termux internal runcommand log ==="
if [ -f "$LOG" ]; then tail -40 "$LOG"; else echo "no termux-log.txt"; fi

echo
echo "=== termux.properties active lines ==="
grep -vE '^\s*#|^\s*$' "$HOME/.termux/termux.properties" 2>/dev/null

echo
echo "=== Termux version ==="
echo "$TERMUX_VERSION"

echo
echo "=== RUN_COMMAND service reachable? ==="
dumpsys_placeholder=1
echo "(checked on the Android side separately)"

echo
echo "LOGSH_DONE"
