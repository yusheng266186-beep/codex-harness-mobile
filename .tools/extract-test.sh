#!/data/data/com.termux/files/usr/bin/bash
# Reproduce the app's restartHarnessAndOpen script verbatim and show what it prints.
set +e

extract_url() {
  grep -oE 'https?://[^[:space:]]+' "$HOME/.codex-harness-mobile/harness.log" 2>/dev/null \
    | tail -n 1 | sed 's/[),.;]$//'
}

echo "===== raw harness.log ====="
cat -A "$HOME/.codex-harness-mobile/harness.log" 2>&1 | tail -5

echo
echo "===== extract_url output ====="
url=$(extract_url)
echo "url=[$url]"
echo "url length=${#url}"

echo
echo "===== port 3080 check (toybox nc) ====="
/system/bin/toybox nc -z -w 1 127.0.0.1 3080 >/dev/null 2>&1 && echo "nc: OPEN" || echo "nc: closed"

echo
echo "===== what the app would print ====="
if /system/bin/toybox nc -z -w 1 127.0.0.1 3080 >/dev/null 2>&1 && [ -n "$url" ]; then
  printf '%s\n' "$url"
  echo "(exit 0 branch taken)"
else
  echo "(would fall through to restart branch)"
fi

echo
echo "EXTRACT_DONE"
