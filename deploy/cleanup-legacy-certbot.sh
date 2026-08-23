#!/bin/sh
set -eu

# The previous deployment used a long-running certbot container with a
# destructive initial-issuance entrypoint. Remove that disposable container
# before starting the new host-cron-based renewal flow. Bind-mounted
# certificates under volumes/certs are deliberately left untouched.
cd "$(dirname "$0")"

docker compose --profile certbot \
    -f docker-compose.yml \
    -f docker-compose.prod.yml \
    rm --stop --force certbot

echo "==> Legacy Certbot container cleanup completed."
