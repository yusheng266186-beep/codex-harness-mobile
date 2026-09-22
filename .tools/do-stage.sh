#!/data/data/com.termux/files/usr/bin/bash
# Do the real staging into Debian's cdesktop cache and prove it works offline.
set +e

STAGE=/data/data/com.termux/files/home/.cdesktop-staging
DEST=/root/.cdesktop/bin/v0.2.3-20260519022845/linux-arm64

echo "===== clean any leftover partial download ====="
proot-distro login debian -- bash -c "rm -f $DEST/*.tmp; ls -la $DEST 2>&1"

echo
echo "===== copy the three verified archives into the wrapper's cache ====="
proot-distro login debian -- bash -c "
  mkdir -p $DEST
  for n in cdesktop cdesktop-mcp cdesktop-review; do
    cp -f $STAGE/\$n.zip $DEST/\$n.zip && echo \"  staged \$n.zip\"
  done
  echo
  echo '--- sha256 in cache ---'
  sha256sum $DEST/*.zip
"

echo
echo "===== extract with unzip ====="
proot-distro login debian -- bash -c "
  cd $DEST
  for n in cdesktop cdesktop-mcp cdesktop-review; do
    if unzip -o -q \$n.zip 2>/dev/null; then echo \"  unzipped \$n\"; else echo \"  FAILED \$n\"; fi
    chmod +x \$n 2>/dev/null
  done
  echo
  echo '--- final cache ---'
  ls -la $DEST
"

echo
echo "===== install the npm wrapper (53 KB, so launch needs no npx) ====="
proot-distro login debian -- bash -lc 'npm install -g --silent cdesktop@0.2.3 2>&1 | tail -3; command -v cdesktop || echo "wrapper MISSING"'

echo
echo "STAGING_DONE"
