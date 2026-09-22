#!/data/data/com.termux/files/usr/bin/bash
# Verify services are reachable from the Termux (Android) side too.
set +e

echo "===== Termux-side listeners ====="
for p in 3080 3200 4500 8022; do
  if (exec 3<>/dev/tcp/127.0.0.1/$p) 2>/dev/null; then
    echo "  port $p OPEN"
  else
    echo "  port $p closed"
  fi
done

echo
echo "===== HTTP fetch of DSH from Termux view ====="
curl -s -o /dev/null -w '  dsh 3080: http=%{http_code} time=%{time_total}s\n' --max-time 10 http://127.0.0.1:3080/ 2>&1

echo
echo "===== docker/proot processes ====="
ps -A -o PID,PPID,ARGS 2>/dev/null | grep -Ei 'node|dsh|codex|cdesktop|proot|sshd' | grep -v grep | head -20

echo
echo "===== current tokens ====="
echo "harness: $(cat /root/.codex-harness-mobile/harness.log 2>/dev/null)"

echo
echo "VERIFY_DONE"
