#!/data/data/com.termux/files/usr/bin/bash
# Copy the pre-staged cdesktop ARM64 binaries into Debian's cdesktop cache.
#
# Why: cdesktop's npx wrapper downloads ~49 MB from binaries.cdesktop.ai on
# first run. Through this phone's VPN that transfer stalls (observed stuck at
# 2%), so we ship the verified archives instead and place them exactly where
# the wrapper's ensureBinary() looks first:
#   ~/.cdesktop/bin/<BINARY_TAG>/<platform>/<binaryName>.zip
# When the zip is already present the wrapper returns it without any network
# call at all, so the first launch is offline and instant.
set +e

SRC=/sdcard/Download/cdesktop
TAG=v0.2.3-20260519022845
PLATFORM=linux-arm64

echo "=== source check ==="
ls -la "$SRC" 2>&1
if [ ! -d "$SRC" ]; then
  echo "MISSING: $SRC"
  exit 1
fi

echo
echo "=== staging into Debian (via bind mount) ==="
proot-distro login debian --bind "$SRC:/mnt/cdesktop" -- bash -lc "
set -e
TAG=$TAG
PLATFORM=$PLATFORM
CACHE=\$HOME/.cdesktop/bin/\$TAG/\$PLATFORM
mkdir -p \"\$CACHE\"

for name in cdesktop cdesktop-mcp cdesktop-review; do
  if [ ! -f \"/mnt/cdesktop/\$name.zip\" ]; then
    echo \"  \$name.zip MISSING in source\"
    continue
  fi
  cp -f \"/mnt/cdesktop/\$name.zip\" \"\$CACHE/\$name.zip\"
  echo \"  staged \$name.zip -> \$CACHE/\$name.zip\"
done

echo
echo '=== cache contents ==='
ls -la \"\$CACHE\"

echo
echo '=== extracting for immediate use ==='
for name in cdesktop cdesktop-mcp cdesktop-review; do
  zip=\"\$CACHE/\$name.zip\"
  [ -f \"\$zip\" ] || continue
  if command -v unzip >/dev/null 2>&1; then
    unzip -o -q \"\$zip\" -d \"\$CACHE/\" 2>/dev/null && echo \"  unzip: \$name\"
  else
    node -e \"const fs=require('fs');const {execSync}=require('child_process');\" 2>/dev/null
    python3 -c \"import zipfile,sys;zipfile.ZipFile('\$zip').extractall('\$CACHE')\" 2>/dev/null && echo \"  python: \$name\"
  fi
  [ -f \"\$CACHE/\$name\" ] && chmod +x \"\$CACHE/\$name\"
done

echo
echo '=== final cache ==='
ls -la \"\$CACHE\"

echo
echo '=== pre-installing the cdesktop npm wrapper (53 KB, avoids npx at launch) ==='
if [ ! -x /usr/bin/cdesktop ]; then
  npm install -g --silent cdesktop@0.2.3 2>&1 | tail -5
fi
command -v cdesktop && echo '  wrapper installed' || echo '  wrapper NOT installed'

echo
echo 'STAGE_DONE'
"
