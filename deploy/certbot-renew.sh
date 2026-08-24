#!/bin/sh
set -eu

# ==============================================================================
# FitBridge Certbot Renewal Script (certbot-renew.sh)
# Используется для периодического продления сертификатов через Cron хоста.
# Пример в crontab (crontab -e):
# 23 4,16 * * * sh /path/to/deploy/certbot-renew.sh >> /path/to/deploy/certbot-renew.log 2>&1
# ==============================================================================

cd "$(dirname "$0")"

if [ "${CERTBOT_LOCK_HELD:-false}" != "true" ]; then
    if ! command -v flock >/dev/null 2>&1; then
        echo "ERROR: flock is required to serialize Certbot operations" >&2
        exit 1
    fi
    exec 8>.certbot.lock
    if ! flock -w "${CERTBOT_LOCK_TIMEOUT_SECONDS:-180}" 8; then
        echo "ERROR: Another Certbot operation did not finish within the lock timeout" >&2
        exit 1
    fi
fi

echo "==> [$(date -u)] Starting Certbot renewal check..."

# The public edge must recover independently from the application stack. Prove
# that the exact HTTP-01 webroot path is reachable before Certbot contacts CA.
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-build certbot-helper envoy
sh ./verify-acme-path.sh

reload_marker="volumes/certs/.envoy-reload-required"

docker run --rm \
    -v "$(pwd)/volumes/certs:/etc/letsencrypt" \
    -v "$(pwd)/volumes/certbot-webroot:/var/www/certbot" \
    certbot/certbot:v5.7.0 renew \
    --non-interactive \
    --webroot \
    -w /var/www/certbot \
    --quiet \
    --deploy-hook "touch /etc/letsencrypt/.envoy-reload-required"

# Certbot exits successfully when no renewal was needed. Restart Envoy only
# when the deploy hook confirms that at least one certificate was renewed.
if [ -f "$reload_marker" ]; then
    echo "==> Certificate renewed; restarting Envoy to load the new files"
    sh ./prepare-envoy-cert-permissions.sh
    docker compose -f docker-compose.yml -f docker-compose.prod.yml restart envoy
    rm -f "$reload_marker"
else
    echo "==> Certificate is not due for renewal; Envoy restart is not required"
fi

echo "==> [$(date -u)] Certbot renewal check completed."
