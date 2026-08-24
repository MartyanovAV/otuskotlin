#!/bin/sh
set -eu

cd "$(dirname "$0")"

compose_files="-f docker-compose.yml -f docker-compose.prod.yml"

if [ ! -f volumes/db-bootstrap/db-bootstrap.sh ]; then
    echo "ERROR: Required database bootstrap script is missing: volumes/db-bootstrap/db-bootstrap.sh" >&2
    exit 1
fi

echo "==> Validating merged production Compose configuration"
# shellcheck disable=SC2086
docker compose $compose_files config --quiet

certificate_path=$(sed -n 's|^[[:space:]]*filename: "\(/etc/letsencrypt/live/[^" ]*/fullchain\.pem\)"|\1|p' \
    volumes/envoy/envoy.prod.yaml | head -n 1)
private_key_path=$(sed -n 's|^[[:space:]]*filename: "\(/etc/letsencrypt/live/[^" ]*/privkey\.pem\)"|\1|p' \
    volumes/envoy/envoy.prod.yaml | head -n 1)

if [ -z "$certificate_path" ] || [ -z "$private_key_path" ]; then
    echo "ERROR: Unable to resolve TLS file paths from generated Envoy configuration" >&2
    exit 1
fi

# Never expose production private keys to the one-shot validation container.
# Envoy only needs structurally valid files for --mode validate, so generate an
# isolated short-lived pair with the same in-container paths.
validation_certs=$(mktemp -d)
case "$validation_certs" in
    /tmp/*|/var/tmp/*) ;;
    *)
        echo "ERROR: Refusing unexpected temporary directory: $validation_certs" >&2
        exit 1
        ;;
esac
trap 'rm -rf "$validation_certs"' EXIT HUP INT TERM

mkdir -p \
    "$(dirname "$validation_certs$certificate_path")" \
    "$(dirname "$validation_certs$private_key_path")"
MSYS2_ARG_CONV_EXCL='/CN=' openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
    -keyout "$validation_certs$private_key_path" \
    -out "$validation_certs$certificate_path" \
    -subj "/CN=envoy-config-validation.invalid" 2>/dev/null
chmod 755 "$validation_certs" "$(dirname "$validation_certs$certificate_path")"
chmod 644 "$validation_certs$certificate_path" "$validation_certs$private_key_path"

echo "==> Validating generated Envoy production configuration"
envoy_config_host="$(pwd)/volumes/envoy/envoy.prod.yaml"
validation_certs_host="$validation_certs/etc/letsencrypt"
if command -v cygpath >/dev/null 2>&1; then
    envoy_config_host=$(cygpath -m "$envoy_config_host")
    validation_certs_host=$(cygpath -m "$validation_certs_host")
fi
container_config_path="/tmp/fitbridge-envoy-validation.yaml"
MSYS2_ARG_CONV_EXCL='*' docker run --rm \
    -v "$envoy_config_host:$container_config_path:ro" \
    -v "$validation_certs_host:/etc/letsencrypt:ro" \
    envoyproxy/envoy:v1.39.0 \
    --mode validate \
    -c "$container_config_path"

rm -rf "$validation_certs"
trap - EXIT HUP INT TERM
echo "==> Production configuration validation completed"
