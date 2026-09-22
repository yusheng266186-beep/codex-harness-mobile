#!/bin/bash
set -e

export DEBIAN_FRONTEND=noninteractive

apt-get update
apt-get -y -o Dpkg::Options::=--force-confold upgrade
apt-get install -y --no-install-recommends ca-certificates curl git build-essential python3 python3-pip pkg-config unzip openssl

curl -fsSL https://deb.nodesource.com/setup_22.x -o /tmp/nodesource_setup.sh
bash /tmp/nodesource_setup.sh
apt-get install -y nodejs

# The selected Linux WebUI is a pnpm workspace.  Keep the package manager
# version aligned with its lockfile so the phone build is reproducible.
corepack enable
corepack prepare pnpm@10.18.3 --activate

node --version
npm --version
pnpm --version

npm install -g --include=optional @openai/codex
npm install -g @deepseek-ai/dsh

echo
echo 'DEBIAN_CODEX_DSH_COMPLETE'
codex --version || true
dsh --version || true
echo 'Run .tools/install-codex-webui.sh inside Debian, or let the mobile app install it on first launch.'
