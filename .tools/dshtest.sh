#!/data/data/com.termux/files/usr/bin/bash
# Check whether the updated DSH still launches, and where it actually installs.
set +e

echo "===== dsh in DEBIAN (/usr/bin/dsh) ====="
proot-distro login debian -- bash -lc '
  echo "which: $(command -v dsh)"
  echo "--- content of /usr/bin/dsh ---"
  head -20 /usr/bin/dsh 2>&1
  echo "--- dsh --help ---"
  timeout 25 dsh --help 2>&1 | head -25
  echo "--- global npm root ---"
  npm root -g 2>&1
  ls -la "$(npm root -g)/@deepseek-ai/" 2>&1
'

echo
echo "===== does DSH itself start (60s probe) ====="
proot-distro login debian -- bash -lc '
  export HOME=/root
  timeout 60 dsh web --no-open --port 3080 > /root/dsh-probe.log 2>&1 &
  sleep 25
  echo "--- probe log ---"
  cat /root/dsh-probe.log 2>&1
  echo "--- port check ---"
  (exec 3<>/dev/tcp/127.0.0.1/3080) 2>/dev/null && echo "3080 OPEN" || echo "3080 closed"
'

echo
echo "TERMUX_DSH_TEST_DONE"
