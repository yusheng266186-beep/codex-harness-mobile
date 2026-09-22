#!/bin/bash
set -e

export DEBIAN_FRONTEND=noninteractive

apt-get update
apt-get -y -o Dpkg::Options::=--force-confold upgrade
apt-get install -y --no-install-recommends ca-certificates curl git build-essential python3 python3-pip pkg-config unzip

curl -fsSL https://deb.nodesource.com/setup_22.x -o /tmp/nodesource_setup.sh
bash /tmp/nodesource_setup.sh
apt-get install -y nodejs

node --version
npm --version

npm install -g --include=optional @openai/codex
npm install -g @deepseek-ai/dsh

echo
echo 'DEBIAN_CODEX_DSH_COMPLETE'
codex --version || true
dsh --version || true
