#!/bin/sh
set -eu

cd "$(dirname "$0")"

compose_files="-f docker-compose.yml -f docker-compose.prod.yml"

if [ ! -f volumes/db-bootstrap/db-bootstrap.sh ]; then
    echo "ERROR: Required database bootstrap script is missing: volumes/db-bootstrap/db-bootstrap.sh" >&2
    exit 1
fi

echo "==> Validating merged production Compose configuration"
# shellcheck disable=SC2086
docker compose $compose_files config --quiet

if [ ! -f volumes/caddy/Caddyfile.prod ]; then
    echo "ERROR: Missing Caddyfile.prod. Run prepare-prod-config.sh first" >&2
    exit 1
fi

# Caddy ships with a `validate` subcommand that loads the Caddyfile and
# confirms it parses, including the JSON adapter conversion. We mount the
# prepared Caddyfile as the production container would receive it.
# This script is only called from CI (deploy-config job) where the
# fitbridge-caddy:local image is built beforehand.
echo "==> Validating generated Caddy production configuration"
caddyfile_host="$(pwd)/volumes/caddy/Caddyfile.prod"
if command -v cygpath >/dev/null 2>&1; then
    caddyfile_host=$(cygpath -m "$caddyfile_host")
fi
container_config_path="/etc/caddy/Caddyfile"
MSYS2_ARG_CONV_EXCL='*' docker run --rm \
    -v "$caddyfile_host:$container_config_path:ro" \
    fitbridge-caddy:local \
    caddy validate --config "$container_config_path" --adapter caddyfile

echo "==> Production configuration validation completed"
