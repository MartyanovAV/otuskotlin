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
# issuance. Keep it in place while Envoy serves the ACME HTTP-01 route: removing
# files referenced by the active TLS listener can make Envoy restart-loop before
# Certbot even gets a chance to validate the domain. The real certificate is
# therefore issued into an isolated state directory and promoted only after a
# successful issuance.
bootstrap_certificate=false
if [ ! -f "$renewal_config" ] && [ -f "$cert_dir/fullchain.pem" ]; then
    issuer=$(openssl x509 -in "$cert_dir/fullchain.pem" -noout -issuer -nameopt RFC2253 | sed 's/^issuer=//')
    subject=$(openssl x509 -in "$cert_dir/fullchain.pem" -noout -subject -nameopt RFC2253 | sed 's/^subject=//')
    if [ "$issuer" = "$subject" ]; then
        bootstrap_certificate=true
        echo "==> Temporary self-signed bootstrap certificate will remain active during ACME validation"
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

# Run Certbot against an empty, isolated state directory. The bootstrap
# certificate deliberately lives in the final Certbot path so Envoy can start,
# but presenting that path directly to Certbot would collide with the lineage
# it is about to create.
issuance_dir=$(mktemp -d "volumes/certbot-initial.XXXXXX")
cleanup_issuance_dir() {
    if [ -n "${issuance_dir:-}" ] && [ -d "$issuance_dir" ]; then
        rm -rf "$issuance_dir"
    fi
}
trap cleanup_issuance_dir EXIT HUP INT TERM

docker run --rm \
    -v "$(pwd)/$issuance_dir:/etc/letsencrypt" \
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

issued_chain="$issuance_dir/live/$domain/fullchain.pem"
issued_key="$issuance_dir/live/$domain/privkey.pem"
issued_renewal="$issuance_dir/renewal/$domain.conf"

if [ ! -f "$issued_chain" ] || [ ! -f "$issued_key" ] || [ ! -f "$issued_renewal" ]; then
    echo "ERROR: Certbot reported success but did not create the expected '$domain' lineage" >&2
    exit 1
fi
if ! openssl x509 -in "$issued_chain" -noout -checkhost "$domain" >/dev/null 2>&1; then
    echo "ERROR: The issued certificate does not cover '$domain'" >&2
    exit 1
fi

# Promote only a complete, validated Certbot state. Until this point any
# failure leaves the bootstrap certificate and the running Envoy untouched.
if [ "$bootstrap_certificate" = "true" ]; then
    echo "==> Replacing the temporary bootstrap certificate with the issued lineage"
    rm -rf "volumes/certs/live/$domain" "volumes/certs/archive/$domain"
fi
cp -a "$issuance_dir/." "volumes/certs/"

if ! sh ./certbot-lineage-exists.sh "$domain"; then
    echo "ERROR: Issued Certbot lineage was not promoted successfully" >&2
    exit 1
fi
sh ./prepare-envoy-cert-permissions.sh

# Restart Envoy so its static TLS context reads the freshly issued files.
docker compose -f docker-compose.yml -f docker-compose.prod.yml restart envoy

echo "=============================================================================="
echo "==> [SUCCESS] Certificate issued successfully for $domain!"
echo "==> Envoy restarted and is now using the issued certificate."
echo "=============================================================================="
