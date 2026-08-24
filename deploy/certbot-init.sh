#!/bin/sh
set -eu

# ==============================================================================
# FitBridge Certbot Initial Certificate Issuance (certbot-init.sh)
# Выпускает боевой SSL-сертификат Let's Encrypt для боевого домена.
# Запуск: sh certbot-init.sh
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

if [ -f ".env" ]; then
    # Load variables from .env
    set -a
    . ./.env
    set +a
fi

: "${FITBRIDGE_PUBLIC_URL:?FITBRIDGE_PUBLIC_URL is required (defined in .env)}"
: "${LETSENCRYPT_EMAIL:?LETSENCRYPT_EMAIL is required (defined in .env, e.g. admin@yourdomain.com)}"

domain=$(sh ./resolve-public-domain.sh "$FITBRIDGE_PUBLIC_URL")

cert_dir="volumes/certs/live/$domain"
renewal_config="volumes/certs/renewal/$domain.conf"

if [ -f "$renewal_config" ] && ! sh ./certbot-lineage-exists.sh "$domain"; then
    echo "ERROR: Renewal configuration $renewal_config does not list '$domain'; refusing a potentially duplicate issuance" >&2
    exit 1
fi

# The bootstrap certificate occupies live/$domain only until the first real
# issuance. Remove it only when it is demonstrably self-signed. Never remove
# an existing non-bootstrap certificate automatically.
if [ ! -f "$renewal_config" ] && [ -f "$cert_dir/fullchain.pem" ]; then
    issuer=$(openssl x509 -in "$cert_dir/fullchain.pem" -noout -issuer -nameopt RFC2253 | sed 's/^issuer=//')
    subject=$(openssl x509 -in "$cert_dir/fullchain.pem" -noout -subject -nameopt RFC2253 | sed 's/^subject=//')
    if [ "$issuer" = "$subject" ]; then
        echo "==> Removing the temporary self-signed bootstrap certificate"
        rm -rf "volumes/certs/live/$domain" "volumes/certs/archive/$domain"
    else
        echo "ERROR: A non-Certbot certificate already exists for '$domain'; refusing to replace it" >&2
        exit 1
    fi
fi

echo "==> Requesting Let's Encrypt certificate for: $domain"
echo "==> Notification email: $LETSENCRYPT_EMAIL"

staging_args=""
if [ "${LETSENCRYPT_STAGING:-false}" = "true" ]; then
    echo "==> [WARN] Using Let's Encrypt STAGING environment (for testing rate limits)"
    staging_args="--test-cert"
fi

# Убедимся, что директории созданы
mkdir -p "volumes/certs" "volumes/certbot-webroot"

# Start only the independent public edge and prove that the exact webroot file
# is reachable through Envoy before asking Let's Encrypt to validate it.
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-build certbot-helper envoy
sh ./verify-acme-path.sh

# Run certbot as a one-shot container that is independent of the rest of the
# compose stack. This way, a failing peer service (keycloak, postgres, ...)
# never blocks certificate issuance, and we do not pull the entire production
# project up just to talk to Let's Encrypt.
docker run --rm \
    -v "$(pwd)/volumes/certs:/etc/letsencrypt" \
    -v "$(pwd)/volumes/certbot-webroot:/var/www/certbot" \
    certbot/certbot:v5.7.0 certonly \
    --non-interactive \
    --webroot \
    -w /var/www/certbot \
    --cert-name "$domain" \
    -d "$domain" \
    --email "$LETSENCRYPT_EMAIL" \
    --agree-tos \
    --no-eff-email \
    --key-type ecdsa \
    $staging_args

# Restart Envoy so its static TLS context reads the freshly issued files.
docker compose -f docker-compose.yml -f docker-compose.prod.yml restart envoy

echo "=============================================================================="
echo "==> [SUCCESS] Certificate issued successfully for $domain!"
echo "==> Envoy restarted and is now using the issued certificate."
echo "=============================================================================="
