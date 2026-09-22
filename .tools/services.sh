#!/bin/bash
# Proper Codex health check + agent round-trip, and (re)start DSH web.
set +e
export HOME=/root

echo "===== codex doctor ====="
timeout 90 codex doctor 2>&1 | head -60
echo "  exit=$?"

echo
echo "===== codex exec round-trip (stdin closed) ====="
out=$(timeout 120 codex exec --skip-git-repo-check "Reply with exactly: CODEX_OK" < /dev/null 2>&1)
rc=$?
echo "$out" | tail -20
echo "  exit=$rc"
echo "$out" | grep -q CODEX_OK && echo "  >>> AGENT ROUND-TRIP OK" || echo "  >>> agent round-trip did NOT return CODEX_OK"

echo
echo "===== (re)start DSH web on 3080 ====="
pkill -f 'dsh web' 2>/dev/null
sleep 1
LOG=/root/.codex-harness-mobile/harness.log
mkdir -p /root/.codex-harness-mobile
nohup dsh web --no-open --port 3080 > "$LOG" 2>&1 &
echo "  launched pid=$!"
for i in $(seq 1 30); do
  sleep 2
  if grep -q 'http://' "$LOG" 2>/dev/null; then break; fi
done
echo "  --- harness.log ---"
cat "$LOG"
echo
echo "  --- listener check ---"
(exec 3<>/dev/tcp/127.0.0.1/3080) 2>/dev/null && echo "  port 3080 OPEN" || echo "  port 3080 CLOSED"

echo
echo "SERVICES_DONE"
