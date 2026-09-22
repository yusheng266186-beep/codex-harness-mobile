#!/data/data/com.termux/files/usr/bin/bash
# FIX A v2: pass --expose-internals directly to node, not via NODE_OPTIONS.
set +e

echo "===== dsh launcher head ====="
proot-distro login debian -- bash -lc 'head -3 /usr/bin/dsh; echo "---"; ls -la /usr/bin/dsh'

echo
echo "===== FIX A v2: node --expose-internals /usr/bin/dsh web ====="
proot-distro login debian -- bash -lc '
  export HOME=/root
  timeout 60 node --expose-internals /usr/bin/dsh web --no-open --port 3080 > /tmp/dsh-expose2.log 2>&1 &
  sleep 35
  echo "--- log ---"
  cat /tmp/dsh-expose2.log
  echo "--- port 3080 ---"
  (exec 3<>/dev/tcp/127.0.0.1/3080) 2>/dev/null && echo "3080 OPEN" || echo "3080 closed"
  pkill -f "dsh web" 2>/dev/null
  pkill -f "expose-internals" 2>/dev/null
'

echo
echo "===== available versions ====="
proot-distro login debian -- bash -lc 'npm view @deepseek-ai/dsh versions 2>&1 | tr -d "[]" | tr "," "\n" | tail -15'

echo
echo "FIXTEST2_DONE"
