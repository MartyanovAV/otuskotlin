#!/bin/sh
set -eu

cd "$(dirname "$0")"

required_variables="
GHCR_USERNAME
APP_VERSION
FITBRIDGE_PUBLIC_URL
LETSENCRYPT_EMAIL
POSTGRES_DB
POSTGRES_USER
POSTGRES_PASSWORD
KC_DB_NAME
KC_DB_USERNAME
KC_DB_PASSWORD
LIQUIBASE_DB_USERNAME
LIQUIBASE_DB_PASSWORD
DB_NAME
DB_USER
DB_PASSWORD
KC_BOOTSTRAP_ADMIN_USERNAME
KC_BOOTSTRAP_ADMIN_PASSWORD
GREPTIMEDB_USER
GREPTIMEDB_PASS
"

for variable_name in $required_variables; do
    eval "variable_value=\${$variable_name-}"
    if [ -z "$variable_value" ]; then
        echo "ERROR: $variable_name is required" >&2
        exit 1
    fi
done

domain=$(sh ./resolve-public-domain.sh "$FITBRIDGE_PUBLIC_URL")
# Persist one canonical representation. This avoids a trailing slash producing
# subtly different Keycloak issuer, audience and redirect URI values.
FITBRIDGE_PUBLIC_URL="https://$domain"
case "${LETSENCRYPT_STAGING:-false}" in
    true|false) ;;
    *)
        echo "ERROR: LETSENCRYPT_STAGING must be either true or false" >&2
        exit 1
        ;;
esac

write_value() {
    variable_name="$1"
    variable_value="$2"

    case "$variable_value" in
        *'
'*)
            echo "ERROR: $variable_name contains a newline and cannot be stored safely in .env" >&2
            exit 1
            ;;
    esac
    if printf '%s' "$variable_value" | grep -q '[[:cntrl:]]'; then
        echo "ERROR: $variable_name contains a control character and cannot be stored safely in .env" >&2
        exit 1
    fi
    case "$variable_value" in
        *"'"*)
            echo "ERROR: $variable_name contains a single quote; rotate this value before storing it in the shell-compatible production .env" >&2
            exit 1
            ;;
    esac

    # Compose and POSIX shells both treat single-quoted values literally, so
    # passwords containing $, #, backslashes or spaces survive manual recovery.
    printf "%s='%s'\n" "$variable_name" "$variable_value"
}

umask 077
temporary_env=$(mktemp .env.tmp.XXXXXX)
trap 'rm -f "$temporary_env"' EXIT HUP INT TERM

{
    for variable_name in $required_variables; do
        eval "variable_value=\${$variable_name}"
        write_value "$variable_name" "$variable_value"
    done
    write_value LETSENCRYPT_STAGING "${LETSENCRYPT_STAGING:-false}"
} > "$temporary_env"

mv -f "$temporary_env" .env
trap - EXIT HUP INT TERM
echo "==> Production environment written to $(pwd)/.env with mode 0600"
