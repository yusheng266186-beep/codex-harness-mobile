#!/data/data/com.termux/files/usr/bin/bash
# Start DSH web inside Debian as a persistent background process.
# The `proot-distro login` session itself is what must stay alive,
# so we launch it detached from the Termux side.
set +e

pkill -f 'dsh web' 2>/dev/null
pkill -f 'proot-distro login debian' 2>/dev/null
sleep 1

LOG=$HOME/.codex-harness-mobile/harness.log
mkdir -p "$HOME/.codex-harness-mobile"

cat > "$HOME/.tools/dsh-run.sh" <<'EOF'
#!/bin/bash
export HOME=/root
mkdir -p /root/.codex-harness-mobile
exec dsh web --no-open --port "${1:-3080}"
EOF
chmod +x "$HOME/.tools/dsh-run.sh"

# Launch the whole proot session detached, with stdio redirected to a file.
setsid proot-distro login debian \
  --bind "$HOME/.tools:/mnt/tools" \
  -- bash /mnt/tools/dsh-run.sh 3080 \
  < /dev/null > "$LOG" 2>&1 &
disown 2>/dev/null

echo "launched pid=$!"
for i in $(seq 1 45); do
  sleep 2
  if grep -q 'http://' "$LOG" 2>/dev/null; then break; fi
done

echo "--- harness.log ---"
cat "$LOG" 2>&1
echo
echo "START_TERMUX_DONE"
