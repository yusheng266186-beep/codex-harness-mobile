#!/bin/bash
# Precisely map which hosts resolve to fake VPN IPs and which are reachable.
set +e

hosts="api.openai.com chatgpt.com auth.openai.com registry.npmjs.org github.com raw.githubusercontent.com objects.githubusercontent.com"

echo "===== IP MAP ====="
for h in $hosts; do
  ip=$(getent ahostsv4 "$h" 2>/dev/null | head -1 | awk '{print $1}')
  printf '  %-34s -> %s\n' "$h" "$ip"
done

echo
echo "===== REACHABILITY (default resolver, incl. fake IPs) ====="
for h in $hosts; do
  code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 12 "https://$h" 2>/dev/null)
  printf '  %-34s http=%s\n' "$h" "$code"
done

echo
echo "===== VERBOSE: registry.npmjs.org (fake IP but allegedly works) ====="
curl -sv -o /dev/null --max-time 15 https://registry.npmjs.org 2>&1 | grep -Ei 'trying|connected|proxy|http/|subject:|issuer:' | head -10

echo
echo "===== ENV / PROXY ====="
echo "http_proxy=[$http_proxy] https_proxy=[$https_proxy] all_proxy=[$all_proxy] no_proxy=[$no_proxy]"
echo "--- npm config ---"
npm config get proxy 2>&1
npm config get https-proxy 2>&1
npm config get registry 2>&1

echo
echo "===== /etc/hosts ====="
cat /etc/hosts

echo
echo "DIAG4_DONE"
