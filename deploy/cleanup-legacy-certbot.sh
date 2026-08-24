#!/bin/sh
set -eu

# The previous deployment used a long-running certbot container with a
# destructive initial-issuance entrypoint. Remove that disposable container
# before starting the new host-cron-based renewal flow. Bind-mounted
# certificates under volumes/certs are deliberately left untouched.
#
# This script is tolerant: after the certbot service was removed from
# docker-compose.prod.yml, the container may no longer exist at all. In that
# case, the script is a no-op and the deploy still succeeds.
cd "$(dirname "$0")"

if ! docker compose \
        -f docker-compose.yml \
        -f docker-compose.prod.yml \
        ps --services --all 2>/dev/null | grep -Fxq certbot; then
    echo "==> No legacy Certbot container found in compose project, nothing to clean up."
    exit 0
fi

docker compose --profile certbot \
    -f docker-compose.yml \
    -f docker-compose.prod.yml \
    rm --stop --force certbot

echo "==> Legacy Certbot container cleanup completed."
