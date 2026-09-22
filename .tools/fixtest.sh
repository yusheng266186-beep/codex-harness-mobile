#!/data/data/com.termux/files/usr/bin/bash
# Test the two candidate fixes for the DSH node-addon-require-builtin failure.
set +e

echo "===== installed dsh version ====="
proot-distro login debian -- bash -lc 'npm ls -g --depth=0 2>/dev/null | grep dsh'

echo
echo "===== FIX A: NODE_OPTIONS=--expose-internals ====="
proot-distro login debian -- bash -lc '
  export HOME=/root
  export NODE_OPTIONS=--expose-internals
  timeout 45 dsh web --no-open --port 3080 > /tmp/dsh-expose.log 2>&1 &
  sleep 30
  echo "--- log ---"
  cat /tmp/dsh-expose.log
  echo "--- port 3080 ---"
  (exec 3<>/dev/tcp/127.0.0.1/3080) 2>/dev/null && echo "3080 OPEN" || echo "3080 closed"
  pkill -f "dsh web" 2>/dev/null
'

echo
echo "===== FIX B: what versions exist on npm ====="
proot-distro login debian -- bash -lc 'npm view @deepseek-ai/dsh versions --json 2>&1 | tail -20'

echo
echo "FIXTEST_DONE"
