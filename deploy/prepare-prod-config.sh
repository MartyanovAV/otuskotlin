#!/bin/sh
set -eu

cd "$(dirname "$0")"

if [ -f .env ]; then
    set -a
    . ./.env
    set +a
fi

# 1. Проверка обязательных переменных
: "${FITBRIDGE_PUBLIC_URL:?FITBRIDGE_PUBLIC_URL is required}"
: "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}"
: "${KC_DB_PASSWORD:?KC_DB_PASSWORD is required}"
: "${LIQUIBASE_DB_PASSWORD:?LIQUIBASE_DB_PASSWORD is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
: "${KC_BOOTSTRAP_ADMIN_PASSWORD:?KC_BOOTSTRAP_ADMIN_PASSWORD is required}"
: "${GREPTIMEDB_PASS:?GREPTIMEDB_PASS is required}"
: "${LETSENCRYPT_EMAIL:?LETSENCRYPT_EMAIL is required}"

public_url="${FITBRIDGE_PUBLIC_URL%/}"
escaped_public_url=$(printf '%s' "$public_url" | sed 's/[&|]/\\&/g')
domain=$(sh ./resolve-public-domain.sh "$public_url")

echo "==> Preparing production configuration for: $public_url (domain: $domain)"

# 2. Генерация конфигурации Caddy для prod. Caddyfile.prod.template использует
#    ${LETSENCRYPT_EMAIL}, ${FITBRIDGE_PUBLIC_URL} и ${FITBRIDGE_PUBLIC_URL_HOST}.
#    ${FITBRIDGE_PUBLIC_URL_HOST} — это $domain, производный из URL.
mkdir -p volumes/caddy

if [ -f "caddy/Caddyfile.prod.template" ]; then
    sed \
        -e "s|\${LETSENCRYPT_EMAIL}|$LETSENCRYPT_EMAIL|g" \
        -e "s|\${FITBRIDGE_PUBLIC_URL}|$escaped_public_url|g" \
        -e "s|\${FITBRIDGE_PUBLIC_URL_HOST}|$domain|g" \
        caddy/Caddyfile.prod.template > volumes/caddy/Caddyfile.prod
    echo "  [OK] volumes/caddy/Caddyfile.prod generated"
else
    echo "ERROR: caddy/Caddyfile.prod.template not found" >&2
    exit 1
fi

# 3. Генерация конфигурации Keycloak Realm для prod
if [ -f "volumes/keycloak/import-prod/fit-bridge-realm.json.template" ]; then
    sed "s|\${FITBRIDGE_PUBLIC_URL}|$escaped_public_url|g" \
        volumes/keycloak/import-prod/fit-bridge-realm.json.template > volumes/keycloak/import-prod/fit-bridge-realm.json
    echo "  [OK] volumes/keycloak/import-prod/fit-bridge-realm.json generated"
else
    echo "ERROR: volumes/keycloak/import-prod/fit-bridge-realm.json.template not found" >&2
    exit 1
fi

# 4. Проверка безопасности: убеждаемся, что тестовые пользователи не попали в import-prod
if [ -f "volumes/keycloak/import-prod/fit-bridge-users-0.json" ]; then
    echo "WARNING: Removing test users file from production keycloak import" >&2
    rm -f "volumes/keycloak/import-prod/fit-bridge-users-0.json"
fi

echo "==> Production configuration prepared successfully."
