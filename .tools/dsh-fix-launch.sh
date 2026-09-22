#!/data/data/com.termux/files/usr/bin/bash
# Launch DSH with --expose-internals, detached, and record what happens.
set +e

pkill -f 'dsh web' 2>/dev/null
pkill -f 'expose-internals' 2>/dev/null
sleep 2

LOGDIR="$HOME/.codex-harness-mobile"
LOG="$LOGDIR/harness.log"
mkdir -p "$LOGDIR"
: > "$LOG"

# The dsh shim needs Node internals exposed, which NODE_OPTIONS refuses to
# carry; the flag has to go directly on the node command line.
cat > "$HOME/.tools/dsh-launch.sh" <<'EOF'
#!/bin/bash
export HOME=/root
exec node --expose-internals /usr/bin/dsh web --no-open --port 3080
EOF
chmod +x "$HOME/.tools/dsh-launch.sh"

# Launch the whole proot session detached so it survives this ssh session.
setsid proot-distro login debian \
  -- bash /data/data/com.termux/files/home/.tools/dsh-launch.sh \
  < /dev/null > "$LOG" 2>&1 &
disown 2>/dev/null

echo "launched pid=$!"
for i in $(seq 1 40); do
  sleep 2
  if grep -q 'http://' "$LOG" 2>/dev/null; then break; fi
done

echo "--- harness.log ---"
cat "$LOG" 2>&1
echo
echo "LAUNCH_DONE"
