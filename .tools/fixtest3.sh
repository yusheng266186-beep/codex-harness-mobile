#!/data/data/com.termux/files/usr/bin/bash
# Test --expose-internals, writing results to a file (survives pkill killing our session).
set +e

OUT="$HOME/.tools/dsh-fixtest.txt"
: > "$OUT"

{
  echo "===== dsh launcher ====="
  head -3 /usr/bin/dsh 2>&1
} >> "$OUT" 2>&1

proot-distro login debian -- bash -lc '
  export HOME=/root
  {
    echo
    echo "===== start: node --expose-internals /usr/bin/dsh web ====="
    set -x
    timeout 60 node --expose-internals /usr/bin/dsh web --no-open --port 3080 > /tmp/dsh-e.log 2>&1 &
    BGPID=$!
    set +x
    sleep 35
    echo "--- /tmp/dsh-e.log ---"
    cat /tmp/dsh-e.log
    echo "--- port check ---"
    (exec 3<>/dev/tcp/127.0.0.1/3080) 2>/dev/null && echo "3080 OPEN" || echo "3080 closed"
    kill $BGPID 2>/dev/null
    echo "===== available versions ====="
    npm view @deepseek-ai/dsh versions 2>&1 | tr -d "[]" | tr "," "\n" | tail -12
    echo "===== END ====="
  } >> /data/data/com.termux/files/home/.tools/dsh-fixtest.txt 2>&1
'

echo "written"
