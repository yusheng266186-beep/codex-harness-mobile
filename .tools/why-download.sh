#!/data/data/com.termux/files/usr/bin/bash
# Why is cdesktop still downloading, and why is dsh web silent?
set +e

echo "===== 1. did the staged zips reach /sdcard? ====="
ls -la /sdcard/Download/cdesktop/ 2>&1

echo
echo "===== 2. what is in Debian's cdesktop cache? ====="
proot-distro login debian -- bash -lc '
  echo "HOME=$HOME"
  echo "--- cache tree ---"
  find "$HOME/.cdesktop" -maxdepth 5 2>/dev/null | head -20
  echo "--- npm global bin ---"
  ls -la /usr/bin/cdesktop /usr/local/bin/cdesktop 2>&1
  echo "--- is the npm wrapper installed? ---"
  npm ls -g --depth=0 2>&1 | head -10
'

echo
echo "===== 3. is a download still running? ====="
ps -A -o PID,ARGS 2>/dev/null | grep -i cdesktop | grep -v grep | cut -c1-140

echo
echo "===== 4. does proot-distro accept --bind? ====="
proot-distro login --help 2>&1 | head -30

echo
echo "DIAG_DONE"
