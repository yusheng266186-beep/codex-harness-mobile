#!/bin/bash
# Diagnose the Debian-side runtime: network egress, codex auth, dsh status.
set +e

echo "===== ENV ====="
echo "node    : $(node -v 2>&1)"
echo "npm     : $(npm -v 2>&1)"
echo "codex   : $(codex --version 2>&1)"
echo "HOME    : $HOME"
echo "PATH    : $PATH"

echo
echo "===== DNS ====="
getent hosts api.openai.com 2>&1 | head -3
getent hosts registry.npmjs.org 2>&1 | head -3

echo
echo "===== EGRESS: api.openai.com ====="
curl -s -o /dev/null -w 'http=%{http_code} time=%{time_total}s\n' --max-time 20 https://api.openai.com/v1/models 2>&1

echo
echo "===== EGRESS: chatgpt.com ====="
curl -s -o /dev/null -w 'http=%{http_code} time=%{time_total}s\n' --max-time 20 https://chatgpt.com 2>&1

echo
echo "===== EGRESS: npmjs ====="
curl -s -o /dev/null -w 'http=%{http_code} time=%{time_total}s\n' --max-time 20 https://registry.npmjs.org 2>&1

echo
echo "===== CODEX AUTH ====="
codex login status 2>&1

echo
echo "===== CODEX CONFIG ====="
ls -la "$HOME/.codex/" 2>&1
echo "--- config.toml ---"
cat "$HOME/.codex/config.toml" 2>&1 | head -40

echo
echo "===== DSH INSTALLED? ====="
ls -la "$HOME/.npm-global/lib/node_modules/@deepseek-ai/" 2>&1
command -v dsh 2>&1
npm ls -g --depth=0 2>&1 | head -20

echo
echo "===== CDESKTOP CACHE ====="
find "$HOME" -maxdepth 4 -iname '*cdesktop*' 2>/dev/null | head -20

echo
echo "DIAG_DONE"
