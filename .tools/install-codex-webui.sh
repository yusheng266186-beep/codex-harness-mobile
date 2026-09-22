#!/usr/bin/env bash
set -Eeuo pipefail

# Codex Harness Mobile installs this WebUI inside the Debian proot.  Keep the
# commit pinned so a fresh phone setup is reproducible and an upstream change
# cannot silently change the mobile runtime.
CODEX_WEBUI_REPO="${CODEX_WEBUI_REPO:-https://github.com/LimLLL/codex-webui.git}"
CODEX_WEBUI_COMMIT="${CODEX_WEBUI_COMMIT:-e98ee58ac8c80780258474e0f13ca67a463a2726}"
CODEX_WEBUI_DIR="${CODEX_WEBUI_DIR:-/root/.codex-harness-mobile/codex-webui}"
CODEX_WEBUI_PORT="${CODEX_WEBUI_PORT:-3200}"

log() {
  printf '[codex-webui] %s\n' "$*"
}

die() {
  printf '[codex-webui] ERROR: %s\n' "$*" >&2
  exit 1
}

command -v git >/dev/null 2>&1 || die "git is required"
command -v node >/dev/null 2>&1 || die "Node.js is required; run bootstrap-debian.sh first"

if ! command -v pnpm >/dev/null 2>&1; then
  command -v corepack >/dev/null 2>&1 || die "pnpm/corepack is required; run bootstrap-debian.sh first"
  corepack enable
  corepack prepare pnpm@10.18.3 --activate
fi

mkdir -p "$(dirname "$CODEX_WEBUI_DIR")"
if [ ! -d "$CODEX_WEBUI_DIR/.git" ]; then
  log "cloning upstream WebUI"
  git clone --filter=blob:none "$CODEX_WEBUI_REPO" "$CODEX_WEBUI_DIR"
fi

cd "$CODEX_WEBUI_DIR"
git fetch --depth 1 origin "$CODEX_WEBUI_COMMIT"
git checkout --detach "$CODEX_WEBUI_COMMIT"

log "installing JavaScript dependencies"
pnpm install --frozen-lockfile

log "generating the official Codex app-server schema"
pnpm codex:schema

log "building the responsive WebUI"
pnpm --dir web build
pnpm build

umask 077
if [ ! -s "$CODEX_WEBUI_DIR/.webui-api-key" ]; then
  log "creating a device-local WebUI API key"
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -hex 32 > "$CODEX_WEBUI_DIR/.webui-api-key"
  else
    od -An -N32 -tx1 /dev/urandom | tr -d ' \n' > "$CODEX_WEBUI_DIR/.webui-api-key"
    printf '\n' >> "$CODEX_WEBUI_DIR/.webui-api-key"
  fi
fi

webui_key="$(tr -d '\r\n' < "$CODEX_WEBUI_DIR/.webui-api-key")"
[ "${#webui_key}" -ge 32 ] || die "generated WebUI key is unexpectedly short"
cat > "$CODEX_WEBUI_DIR/.env" <<EOF
WEBUI_API_KEY=$webui_key
PORT=$CODEX_WEBUI_PORT
CODEX_BIN=codex
CODEX_HOME=/root/.codex
EOF

printf '%s\n' "$CODEX_WEBUI_COMMIT" > "$CODEX_WEBUI_DIR/.codex-harness-mobile-ready"
log "ready at $CODEX_WEBUI_DIR on port $CODEX_WEBUI_PORT"
