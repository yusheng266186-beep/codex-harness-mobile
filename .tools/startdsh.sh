#!/bin/bash
# Start DSH web fully detached so it survives the proot session teardown.
set +e
export HOME=/root

pkill -f 'dsh web' 2>/dev/null
sleep 1

LOG=/root/.codex-harness-mobile/harness.log
mkdir -p /root/.codex-harness-mobile

# setsid + full detach: new session, no controlling terminal, stdio to file.
setsid dsh web --no-open --port 3080 < /dev/null > "$LOG" 2>&1 &
disown 2>/dev/null

echo "launched, waiting for URL..."
for i in $(seq 1 40); do
  sleep 2
  if grep -q 'http://' "$LOG" 2>/dev/null; then break; fi
done

echo "--- harness.log ---"
cat "$LOG" 2>&1

echo
echo "--- process ---"
ps -A -o PID,PPID,ARGS 2>/dev/null | grep -i 'dsh web' | grep -v grep

echo
if (exec 3<>/dev/tcp/127.0.0.1/3080) 2>/dev/null; then
  echo "port 3080 OPEN inside this session"
else
  echo "port 3080 closed inside this session"
fi
echo "START_DONE"
