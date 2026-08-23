#!/bin/sh
set -eu

# ==============================================================================
# FitBridge Certbot Initial Certificate Issuance (certbot-init.sh)
# Выпускает боевой SSL-сертификат Let's Encrypt для боевого домена.
# Запуск: sh certbot-init.sh
# ==============================================================================

cd "$(dirname "$0")"

if [ -f ".env" ]; then
    # Load variables from .env
    set -a
    . ./.env
    set +a
fi

: "${FITBRIDGE_PUBLIC_URL:?FITBRIDGE_PUBLIC_URL is required (defined in .env)}"
: "${LETSENCRYPT_EMAIL:?LETSENCRYPT_EMAIL is required (defined in .env, e.g. admin@yourdomain.com)}"

domain=$(printf '%s' "$FITBRIDGE_PUBLIC_URL" | sed -e 's|^https://||' -e 's|:[0-9]*$||' -e 's|/.*$||')

echo "==> Requesting Let's Encrypt certificate for: $domain"
echo "==> Notification email: $LETSENCRYPT_EMAIL"

staging_args=""
if [ "${LETSENCRYPT_STAGING:-false}" = "true" ]; then
    echo "==> [WARN] Using Let's Encrypt STAGING environment (for testing rate limits)"
    staging_args="--test-cert"
fi

# Убедимся, что директории созданы
mkdir -p "volumes/certs" "volumes/certbot-webroot"

# Выполняем выпуск сертификата через Certbot
docker compose -f docker-compose.yml -f docker-compose.prod.yml run --rm \
    --entrypoint certbot \
    certbot certonly \
    --webroot \
    -w /var/www/certbot \
    -d "$domain" \
    --email "$LETSENCRYPT_EMAIL" \
    --agree-tos \
    --no-eff-email \
    --key-type ecdsa \
    $staging_args

# Envoy uses a static TLS context and reads certificates at process startup.
docker compose -f docker-compose.yml -f docker-compose.prod.yml restart envoy

echo "=============================================================================="
echo "==> [SUCCESS] Certificate issued successfully for $domain!"
echo "==> Envoy restarted and is now using the issued certificate."
echo "=============================================================================="
