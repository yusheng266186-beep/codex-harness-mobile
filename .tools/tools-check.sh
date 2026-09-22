#!/data/data/com.termux/files/usr/bin/bash
# What unzip-capable tools exist on each side?
set +e

echo "===== TERMUX side ====="
for t in unzip bsdtar tar python3 node busybox; do
  p=$(command -v "$t" 2>/dev/null)
  if [ -n "$p" ]; then echo "  $t -> $p"; else echo "  $t -> MISSING"; fi
done

echo
echo "===== DEBIAN side (non-interactive shell, as the app launches it) ====="
proot-distro login debian -- bash -c '
  echo "PATH=$PATH"
  for t in unzip bsdtar tar python3 node busybox; do
    p=$(command -v "$t" 2>/dev/null)
    if [ -n "$p" ]; then echo "  $t -> $p"; else echo "  $t -> MISSING"; fi
  done
'

echo
echo "===== DEBIAN side (login shell) ====="
proot-distro login debian -- bash -lc '
  for t in unzip python3 node; do
    p=$(command -v "$t" 2>/dev/null)
    if [ -n "$p" ]; then echo "  $t -> $p"; else echo "  $t -> MISSING"; fi
  done
  echo "  node version: $(node -v 2>&1)"
'

echo
echo "===== node zlib availability (what I would use to extract) ====="
proot-distro login debian -- bash -lc 'node -e "const z=require(\"zlib\"); console.log(\"zlib ok\", typeof z.inflateRawSync)" 2>&1'

echo
echo "TOOLS_DONE"
