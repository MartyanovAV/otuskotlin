#!/bin/sh
set -eu

# 1. Проверка обязательных переменных
: "${FITBRIDGE_PUBLIC_URL:?FITBRIDGE_PUBLIC_URL is required}"
: "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}"
: "${KC_DB_PASSWORD:?KC_DB_PASSWORD is required}"
: "${LIQUIBASE_DB_PASSWORD:?LIQUIBASE_DB_PASSWORD is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
: "${KC_BOOTSTRAP_ADMIN_PASSWORD:?KC_BOOTSTRAP_ADMIN_PASSWORD is required}"
: "${GREPTIMEDB_PASS:?GREPTIMEDB_PASS is required}"

case "$FITBRIDGE_PUBLIC_URL" in
    https://*) ;;
    *)
        echo "ERROR: FITBRIDGE_PUBLIC_URL must use HTTPS for production deployment" >&2
        exit 1
        ;;
esac

public_url="${FITBRIDGE_PUBLIC_URL%/}"
escaped_public_url=$(printf '%s' "$public_url" | sed 's/[&|]/\\&/g')

domain=$(printf '%s' "$public_url" | sed -e 's|^https://||' -e 's|:[0-9]*$||' -e 's|/.*$||')
escaped_domain=$(printf '%s' "$domain" | sed 's/[&|]/\\&/g')

echo "==> Preparing production configuration for: $public_url (domain: $domain)"

# 2. Проверка наличия SSL сертификатов / Генерация Bootstrap Dummy Certificate
cert_dir="volumes/certs/live/$domain"
mkdir -p "volumes/certbot-webroot"

if [ ! -f "$cert_dir/fullchain.pem" ] || [ ! -f "$cert_dir/privkey.pem" ]; then
    echo "==> No SSL certificate found for '$domain'. Generating bootstrap dummy certificate for initial Envoy boot..."
    mkdir -p "$cert_dir"
    openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
        -keyout "$cert_dir/privkey.pem" \
        -out "$cert_dir/fullchain.pem" \
        -subj "/CN=$domain" 2>/dev/null
    chmod 644 "$cert_dir/privkey.pem"
    chmod 644 "$cert_dir/fullchain.pem"
    chmod 755 "volumes/certs" "volumes/certs/live" "volumes/certs/live/$domain" 2>/dev/null || true
    echo "  [OK] Temporary bootstrap certificate created for $domain"
else
    echo "  [OK] SSL certificate exists for $domain"
fi

# 3. Генерация конфигурации Envoy для prod
if [ -f "volumes/envoy/envoy.prod.yaml.template" ]; then
    sed -e "s|\${FITBRIDGE_PUBLIC_URL}|$escaped_public_url|g" \
        -e "s|\${DOMAIN}|$escaped_domain|g" \
        volumes/envoy/envoy.prod.yaml.template > volumes/envoy/envoy.prod.yaml
    echo "  [OK] volumes/envoy/envoy.prod.yaml generated"
else
    echo "ERROR: volumes/envoy/envoy.prod.yaml.template not found" >&2
    exit 1
fi

# 4. Генерация конфигурации Keycloak Realm для prod
if [ -f "volumes/keycloak/import-prod/fit-bridge-realm.json.template" ]; then
    sed "s|\${FITBRIDGE_PUBLIC_URL}|$escaped_public_url|g" \
        volumes/keycloak/import-prod/fit-bridge-realm.json.template > volumes/keycloak/import-prod/fit-bridge-realm.json
    echo "  [OK] volumes/keycloak/import-prod/fit-bridge-realm.json generated"
else
    echo "ERROR: volumes/keycloak/import-prod/fit-bridge-realm.json.template not found" >&2
    exit 1
fi

# 5. Проверка безопасности: убеждаемся, что тестовые пользователи не попали в import-prod
if [ -f "volumes/keycloak/import-prod/fit-bridge-users-0.json" ]; then
    echo "WARNING: Removing test users file from production keycloak import" >&2
    rm -f "volumes/keycloak/import-prod/fit-bridge-users-0.json"
fi

echo "==> Production configuration prepared successfully."
