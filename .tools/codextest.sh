#!/bin/bash
# Test whether codex can actually reach its backend, then (re)start the local services.
set +e
export HOME=/root

echo "===== 1. RE-TEST api.openai.com over the VPN fake-IP path ====="
curl -s -o /dev/null -w '  fake-ip  : http=%{http_code} time=%{time_total}s\n' --max-time 20 https://api.openai.com/v1/models
curl -s -o /dev/null -w '  real-ip  : http=%{http_code} time=%{time_total}s\n' --max-time 20 \
  --resolve api.openai.com:443:172.66.0.243 https://api.openai.com/v1/models

echo
echo "===== 2. codex exec smoke test (does the agent round-trip?) ====="
timeout 120 codex exec --skip-git-repo-check "Reply with exactly: CODEX_OK" 2>&1 | tail -25
echo "  exit=$?"

echo
echo "===== 3. codex --help (top level) ====="
timeout 20 codex --help 2>&1 | head -30

echo
echo "CODEX_TEST_DONE"
