#!/bin/bash
# Determine whether real OpenAI IPs are reachable, bypassing the VPN's fake DNS.
set +e

echo "===== real OpenAI IP via DNS-over-HTTPS (bypasses local DNS) ====="
REAL=$(curl -s --max-time 15 -H 'accept: application/dns-json' \
  'https://1.1.1.1/dns-query?name=api.openai.com&type=A' \
  | tr ',' '\n' | grep -o '"data":"[0-9.]*"' | head -3 | cut -d'"' -f4)
echo "resolved via DoH: $REAL"

if [ -z "$REAL" ]; then
  echo "DoH failed, trying well-known IPs"
  REAL="162.159.140.245 172.66.0.243"
fi

echo
echo "===== direct connect to real IPs (Host header set) ====="
for ip in $REAL; do
  curl -s -o /dev/null -w "  $ip -> http=%{http_code} time=%{time_total}s\n" \
    --max-time 12 --resolve "api.openai.com:443:$ip" https://api.openai.com/v1/models 2>&1
done

echo
echo "===== what is 198.18.0.6 (the fake IP) ====="
curl -s -o /dev/null -w '  fake-ip direct: http=%{http_code} time=%{time_total}s\n' --max-time 8 http://198.18.0.6/ 2>&1
nc -z -w 3 198.18.0.6 443 2>&1 && echo "  port 443 OPEN" || echo "  port 443 CLOSED/unreachable"

echo
echo "===== npm path that DOES work ====="
curl -s -o /dev/null -w '  npmjs: http=%{http_code} time=%{time_total}s\n' --max-time 15 https://registry.npmjs.org 2>&1
getent ahostsv4 registry.npmjs.org 2>&1 | head -2

echo
echo "===== proxy env / config ====="
env | grep -i proxy
echo "(no output above = no proxy env)"
cat /etc/environment 2>/dev/null
cat /root/.npmrc 2>/dev/null
echo "--- apt proxy ---"
cat /etc/apt/apt.conf.d/*proxy* 2>/dev/null

echo
echo "DIAG3_DONE"
