#!/bin/sh
set -eu

: "${FITBRIDGE_PUBLIC_URL:?FITBRIDGE_PUBLIC_URL is required}"

case "$FITBRIDGE_PUBLIC_URL" in
    https://*) ;;
    *)
        echo "FITBRIDGE_PUBLIC_URL must use HTTPS" >&2
        exit 1
        ;;
esac

public_url="${FITBRIDGE_PUBLIC_URL%/}"
escaped_public_url=$(printf '%s' "$public_url" | sed 's/[&|]/\\&/g')

sed -i "s|http://localhost:8080|$escaped_public_url|g" volumes/envoy/envoy.yaml
sed -i "s|http://localhost:5173|$escaped_public_url|g" volumes/keycloak/import/fit-bridge-realm.json
sed -i "s|http://localhost:8080|$escaped_public_url|g" volumes/keycloak/import/fit-bridge-realm.json
