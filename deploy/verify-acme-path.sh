#!/bin/sh
set -eu

cd "$(dirname "$0")"

if [ -f .env ]; then
    set -a
    . ./.env
    set +a
fi

: "${FITBRIDGE_PUBLIC_URL:?FITBRIDGE_PUBLIC_URL is required}"
domain=$(sh ./resolve-public-domain.sh "$FITBRIDGE_PUBLIC_URL")

challenge_dir="volumes/certbot-webroot/.well-known/acme-challenge"
mkdir -p "$challenge_dir"
probe_file=$(mktemp "$challenge_dir/fitbridge-probe.XXXXXX")
probe_name=$(basename "$probe_file")
probe_value="fitbridge-acme-ready-$probe_name"
trap 'rm -f "$probe_file"' EXIT HUP INT TERM

printf '%s' "$probe_value" > "$probe_file"
chmod 644 "$probe_file"

attempt=1
max_attempts="${ACME_VERIFY_MAX_ATTEMPTS:-30}"
while [ "$attempt" -le "$max_attempts" ]; do
    response=$(curl --fail --silent --show-error \
        --connect-timeout 2 \
        --max-time 5 \
        -H "Host: $domain" \
        "http://127.0.0.1/.well-known/acme-challenge/$probe_name" || true)
    if [ "$response" = "$probe_value" ]; then
        echo "==> ACME HTTP-01 route is ready for $domain"
        exit 0
    fi

    echo "==> Waiting for ACME HTTP-01 route ($attempt/$max_attempts)"
    attempt=$((attempt + 1))
    sleep 2
done

echo "ERROR: ACME HTTP-01 route did not become ready for $domain" >&2
echo "==> Edge container status:" >&2
docker compose -f docker-compose.yml -f docker-compose.prod.yml \
    ps envoy certbot-helper >&2 || true
echo "==> Recent Envoy and ACME helper logs:" >&2
docker compose -f docker-compose.yml -f docker-compose.prod.yml \
    logs --no-color --tail 100 envoy certbot-helper >&2 || true
exit 1
