#!/data/data/com.termux/files/usr/bin/bash
# Probe the Termux environment state and write a report.
OUT=/sdcard/probe-report.txt
: > "$OUT"

{
  echo "=== TERMUX ==="
  echo "prefix       : $PREFIX"
  echo "bash         : $BASH_VERSION"

  echo
  echo "=== TOOLS ==="
  for t in sshd passwd proot-distro node npm git; do
    p=$(command -v "$t" 2>/dev/null)
    if [ -n "$p" ]; then echo "$t -> $p"; else echo "$t -> MISSING"; fi
  done

  echo
  echo "=== PROOT ROOTFS ==="
  RD=/data/data/com.termux/files/usr/var/lib/proot-distro/installed-rootfs
  if [ -d "$RD" ]; then ls -la "$RD"; else echo "no rootfs dir at $RD"; fi

  echo
  echo "=== HOME ==="
  ls -la "$HOME"

  echo
  echo "=== HOME SCRIPTS ==="
  find "$HOME" -maxdepth 2 -name '*.sh' 2>/dev/null

  echo
  echo "=== TERMUX PROPERTIES ==="
  TP=$HOME/.termux/termux.properties
  if [ -f "$TP" ]; then cat "$TP"; else echo "NO termux.properties"; fi

  echo
  echo "=== PROBE_DONE ==="
} >> "$OUT" 2>&1

echo "REPORT_WRITTEN"
