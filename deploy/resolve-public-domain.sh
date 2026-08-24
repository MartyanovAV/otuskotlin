#!/bin/sh
set -eu

public_url="${1:-${FITBRIDGE_PUBLIC_URL:-}}"

case "$public_url" in
    https://*) ;;
    *)
        echo "ERROR: FITBRIDGE_PUBLIC_URL must use HTTPS" >&2
        exit 1
        ;;
esac

public_url="${public_url%/}"
domain="${public_url#https://}"

case "$domain" in
    ""|*/*|*:*|*'?'*|*'#'*|*@*)
        echo "ERROR: FITBRIDGE_PUBLIC_URL must be exactly https://<dns-hostname> without credentials, port, path, query or fragment" >&2
        exit 1
        ;;
esac

if [ "${#domain}" -gt 253 ] ||
    ! printf '%s\n' "$domain" |
        grep -Eq '^([A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?\.)+[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?$'; then
    echo "ERROR: FITBRIDGE_PUBLIC_URL contains an invalid DNS hostname: $domain" >&2
    exit 1
fi

lowercase_domain=$(printf '%s' "$domain" | tr '[:upper:]' '[:lower:]')
if [ "$domain" != "$lowercase_domain" ]; then
    echo "ERROR: FITBRIDGE_PUBLIC_URL hostname must be lowercase: $lowercase_domain" >&2
    exit 1
fi

printf '%s\n' "$domain"
