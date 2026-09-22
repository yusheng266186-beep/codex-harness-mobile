#!/data/data/com.termux/files/usr/bin/bash
# Restart DSH cleanly so the app can also start it, and report the live URL.
set +e

echo "===== killing ALL dsh instances (incl. old proot sessions) ====="
pkill -f 'dsh web' 2>/dev/null
pkill -f 'dsh-run.sh' 2>/dev/null
pkill -f 'proot-distro login debian' 2>/dev/null
sleep 2

echo "remaining dsh procs:"
ps -A -o PID,ARGS 2>/dev/null | grep -Ei 'dsh|proot-distro' | grep -v grep
echo "(empty = all dead)"

echo
echo "===== is 3080 free now? ====="
(exec 3<>/dev/tcp/127.0.0.1/3080) 2>/dev/null && echo "  3080 STILL OPEN" || echo "  3080 free"

echo
echo "KILL_DONE"
