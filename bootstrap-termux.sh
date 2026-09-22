#!/data/data/com.termux/files/usr/bin/bash
set -e

pkg update -y
pkg upgrade -y
pkg install -y git curl wget proot-distro openssh termux-tools
termux-wake-lock || true
proot-distro install debian

echo
echo 'TERMUX_BOOTSTRAP_COMPLETE'
echo 'Run: proot-distro login debian'
