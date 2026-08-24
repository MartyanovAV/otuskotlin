#!/bin/sh
set -eu

cd "$(dirname "$0")"

domain="${1:?Usage: certbot-lineage-exists.sh DOMAIN}"
renewal_dir="volumes/certs/renewal"

for renewal_file in "$renewal_dir"/*.conf; do
    [ -f "$renewal_file" ] || continue
    domains_line=$(grep '^domains[[:space:]]*=' "$renewal_file" || true)
    if printf '%s\n' "$domains_line" | tr ' ,' '\n' | grep -Fxq "$domain"; then
        exit 0
    fi
done

exit 1
