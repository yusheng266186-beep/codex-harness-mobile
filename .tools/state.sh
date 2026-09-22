#!/data/data/com.termux/files/usr/bin/bash
# Full state dump: DSH, cdesktop, Termux home layout.
set +e

echo "===== A. Termux home (what is actually there) ====="
ls -la "$HOME" 2>&1

echo
echo "===== B. dsh-native / DeepSeek dirs (pre-existing attempts?) ====="
for d in "$HOME/dsh-native" "$HOME/DeepSeek"; do
  echo "--- $d ---"
  ls -la "$d" 2>&1 | head -15
done

echo
echo "===== C. running services ====="
ps -A -o PID,ARGS 2>/dev/null | grep -E 'dsh|proot|cdesktop|codex|node' | grep -v grep | cut -c1-130

echo
echo "===== D. listeners ====="
cat /proc/net/tcp 2>/dev/null | awk 'NR>1 && $4=="0A" {print $2}' | while read a; do
  port=$((16#${a#*:}))
  echo "  listening: $port"
done

echo
echo "===== E. app state files ====="
ls -la "$HOME/.codex-harness-mobile/" 2>&1

echo
echo "===== F. harness.log (size + content) ====="
wc -c "$HOME/.codex-harness-mobile/harness.log" 2>&1
cat "$HOME/.codex-harness-mobile/harness.log" 2>&1

echo
echo "===== G. Debian visibility from Termux home ====="
test -d /data/data/com.termux/files/home && echo "termux home path exists" || echo "MISSING"

echo
echo "STATE_DONE"
