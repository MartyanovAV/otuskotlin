#!/bin/sh
set -eu

# ==============================================================================
# FitBridge Certbot Renewal Script (certbot-renew.sh)
# Используется для периодического продления сертификатов через Cron хоста.
# Пример в crontab (crontab -e):
# 0 3 * * 1 /path/to/otuskotlin/deploy/certbot-renew.sh >> /var/log/certbot-renew.log 2>&1
# ==============================================================================

cd "$(dirname "$0")"

echo "==> [$(date -u)] Starting Certbot renewal check..."

docker compose -f docker-compose.yml -f docker-compose.prod.yml run --rm \
    --entrypoint certbot \
    certbot renew \
    --webroot \
    -w /var/www/certbot \
    --quiet

# Envoy uses a static TLS context and must be restarted to read renewed files.
docker compose -f docker-compose.yml -f docker-compose.prod.yml restart envoy

echo "==> [$(date -u)] Certbot renewal check completed."
