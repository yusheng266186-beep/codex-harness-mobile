#!/data/data/com.termux/files/usr/bin/bash
# Report the app-managed service logs and Termux/debian state.
echo "===== APP STATE DIR ====="
ls -la "$HOME/.codex-harness-mobile/" 2>&1

for f in harness.log codex.log cdesktop.log; do
  echo
  echo "===== $f ====="
  cat "$HOME/.codex-harness-mobile/$f" 2>&1
done

for f in harness.pid codex.pid cdesktop.pid; do
  echo
  echo "===== $f ====="
  p=$(cat "$HOME/.codex-harness-mobile/$f" 2>/dev/null)
  echo "pid file says: $p"
  if [ -n "$p" ]; then
    if kill -0 "$p" 2>/dev/null; then echo "  -> ALIVE"; else echo "  -> DEAD"; fi
  fi
done

echo
echo "===== LISTENERS ====="
cat /proc/net/tcp | awk 'NR>1 {print $2, $4}'

echo
echo "===== PROCESSES ====="
ps -A -o PID,ARGS 2>/dev/null | grep -Ei 'node|dsh|codex|cdesktop|proot' | grep -v grep
echo "REPORT_DONE"
