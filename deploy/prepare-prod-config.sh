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

public_url="${FITBRIDGE_PUBLIC_URL%/}"
escaped_public_url=$(printf '%s' "$public_url" | sed 's/[&|]/\\&/g')
domain=$(sh ./resolve-public-domain.sh "$public_url")

echo "==> Preparing production configuration for: $public_url (domain: $domain)"

# 2. Проверка наличия SSL сертификатов / Генерация Bootstrap Dummy Certificate
cert_name="$domain"
renewal_dir="volumes/certs/renewal"

if [ -f "$renewal_dir/$domain.conf" ] && ! sh ./certbot-lineage-exists.sh "$domain"; then
    echo "ERROR: Renewal configuration $renewal_dir/$domain.conf does not list '$domain'; refusing a potentially duplicate issuance" >&2
    exit 1
fi

# Certbot may create a suffixed lineage (for example, domain-0001) when a
# certificate directory already exists. Resolve the lineage by its domains so
# Envoy does not keep using an old bootstrap certificate.
if [ -d "$renewal_dir" ] && [ ! -f "$renewal_dir/$cert_name.conf" ]; then
    for renewal_file in "$renewal_dir"/*.conf; do
        [ -f "$renewal_file" ] || continue
        domains_line=$(grep '^domains[[:space:]]*=' "$renewal_file" || true)
        if printf '%s\n' "$domains_line" | tr ' ,' '\n' | grep -Fxq "$domain"; then
            cert_name=$(basename "$renewal_file" .conf)
            echo "  [OK] Certbot lineage for $domain: $cert_name"
            break
        fi
    done
fi

escaped_cert_name=$(printf '%s' "$cert_name" | sed 's/[&|]/\\&/g')
cert_dir="volumes/certs/live/$cert_name"
cert_chain="$cert_dir/fullchain.pem"
cert_key="$cert_dir/privkey.pem"
mkdir -p "volumes/certbot-webroot"

if [ -f "$cert_chain" ] && [ -f "$cert_key" ]; then
    if ! openssl x509 -in "$cert_chain" -noout >/dev/null 2>&1; then
        echo "ERROR: Existing certificate for '$domain' is not a valid PEM certificate" >&2
        exit 1
    fi
    if ! openssl pkey -in "$cert_key" -noout >/dev/null 2>&1; then
        echo "ERROR: Existing private key for '$domain' is not a valid PEM key" >&2
        exit 1
    fi
    certificate_public_key=$(openssl x509 -in "$cert_chain" -pubkey -noout |
        openssl pkey -pubin -outform PEM 2>/dev/null)
    private_public_key=$(openssl pkey -in "$cert_key" -pubout -outform PEM 2>/dev/null)
    if [ "$certificate_public_key" != "$private_public_key" ]; then
        echo "ERROR: Certificate and private key do not match for '$domain'" >&2
        exit 1
    fi
    issuer=$(openssl x509 -in "$cert_chain" -noout -issuer -nameopt RFC2253 | sed 's/^issuer=//')
    subject=$(openssl x509 -in "$cert_chain" -noout -subject -nameopt RFC2253 | sed 's/^subject=//')
    if [ "$issuer" = "$subject" ]; then
        if sh ./certbot-lineage-exists.sh "$domain"; then
            echo "ERROR: A self-signed certificate exists at $cert_chain but a renewal config is also present" >&2
            echo "ERROR: This is a corrupt state; remove the cert directory and re-run certbot-init.sh" >&2
            exit 1
        fi
        echo "  [WARN] Only a self-signed bootstrap certificate is present for $domain; certbot-init.sh will replace it"
    else
        if ! openssl x509 -in "$cert_chain" -noout -checkhost "$domain" >/dev/null 2>&1; then
            echo "ERROR: Existing certificate does not cover production hostname '$domain'" >&2
            exit 1
        fi
        enddate=$(openssl x509 -in "$cert_chain" -noout -enddate | sed 's/^notAfter=//')
        end_epoch=$(date -u -d "$enddate" +%s 2>/dev/null || echo 0)
        now_epoch=$(date -u +%s)
        days_left=$(( (end_epoch - now_epoch) / 86400 ))
        if [ "$days_left" -lt 21 ]; then
            echo "  [WARN] SSL certificate for $domain expires in $days_left days; deployment will attempt renewal"
        else
            echo "  [OK] SSL certificate exists for $domain (expires in $days_left days)"
        fi
    fi
elif [ -e "$cert_chain" ] || [ -L "$cert_chain" ] || [ -e "$cert_key" ] || [ -L "$cert_key" ]; then
    echo "ERROR: Incomplete SSL certificate state for '$domain'; refusing to overwrite it" >&2
    exit 1
else
    echo "==> No SSL certificate found for '$domain'. Generating bootstrap dummy certificate for initial Envoy boot..."
    mkdir -p "$cert_dir"
    bootstrap_key=$(mktemp "$cert_dir/.privkey.pem.XXXXXX")
    bootstrap_chain=$(mktemp "$cert_dir/.fullchain.pem.XXXXXX")
    cleanup_bootstrap_files() {
        rm -f "${bootstrap_key:-}" "${bootstrap_chain:-}"
    }
    trap cleanup_bootstrap_files EXIT HUP INT TERM
    MSYS2_ARG_CONV_EXCL='/CN=' openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
        -keyout "$bootstrap_key" \
        -out "$bootstrap_chain" \
        -subj "/CN=$domain" 2>/dev/null
    chmod 600 "$bootstrap_key"
    chmod 644 "$bootstrap_chain"
    # Publish only complete files. Envoy may be restart-looping after an
    # interrupted first issuance, so writing directly to its active paths can
    # let it observe a partially generated private key.
    mv "$bootstrap_key" "$cert_key"
    mv "$bootstrap_chain" "$cert_chain"
    trap - EXIT HUP INT TERM
    chmod 755 "volumes/certs" "volumes/certs/live" "volumes/certs/live/$cert_name" 2>/dev/null || true
    echo "  [OK] Temporary bootstrap certificate created for $domain"
fi

# 3. Генерация конфигурации Envoy для prod
if [ -f "volumes/envoy/envoy.prod.yaml.template" ]; then
    sed -e "s|\${FITBRIDGE_PUBLIC_URL}|$escaped_public_url|g" \
        -e "s|\${DOMAIN}|$escaped_cert_name|g" \
        volumes/envoy/envoy.prod.yaml.template > volumes/envoy/envoy.prod.yaml
    echo "  [OK] volumes/envoy/envoy.prod.yaml generated"
else
    echo "ERROR: volumes/envoy/envoy.prod.yaml.template not found" >&2
    exit 1
fi

# The official Envoy image runs as an unprivileged user. Apply the same
# dedicated group permissions to bootstrap and real Certbot keys on every
# configuration preparation, including recovery after interrupted deploys.
sh ./prepare-envoy-cert-permissions.sh

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
