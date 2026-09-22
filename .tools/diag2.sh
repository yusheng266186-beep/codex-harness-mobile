#!/bin/bash
# Confirm whether the egress failure is IPv6-only.
set +e

echo "===== FORCE IPv4 ====="
curl -4 -s -o /dev/null -w 'openai  ipv4: http=%{http_code} time=%{time_total}s\n' --max-time 15 https://api.openai.com/v1/models 2>&1
curl -4 -s -o /dev/null -w 'chatgpt ipv4: http=%{http_code} time=%{time_total}s\n' --max-time 15 https://chatgpt.com 2>&1

echo
echo "===== FORCE IPv6 ====="
curl -6 -s -o /dev/null -w 'openai  ipv6: http=%{http_code} time=%{time_total}s\n' --max-time 15 https://api.openai.com/v1/models 2>&1
curl -6 -s -o /dev/null -w 'chatgpt ipv6: http=%{http_code} time=%{time_total}s\n' --max-time 15 https://chatgpt.com 2>&1

echo
echo "===== DNS: A records (IPv4) ====="
getent ahostsv4 api.openai.com 2>&1 | head -4
getent ahostsv4 chatgpt.com 2>&1 | head -4

echo
echo "===== ROUTES ====="
ip route 2>&1 | head -10
echo "--- ipv6 ---"
ip -6 route 2>&1 | head -10

echo
echo "===== RESOLV ====="
cat /etc/resolv.conf 2>&1
echo "--- gai.conf (precedence) ---"
cat /etc/gai.conf 2>&1 | grep -v '^#' | grep -v '^$' | head -10

echo
echo "===== CDESKTOP CACHE ====="
ls -la "/root/.cdesktop" 2>&1
find "/root/.cdesktop" -type f 2>/dev/null | head -20

echo
echo "DIAG2_DONE"
