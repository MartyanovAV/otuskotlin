#!/bin/sh
set -eu

cd "$(dirname "$0")"

# The official Envoy image drops privileges before starting Envoy. Use a
# dedicated numeric group which is also configured through ENVOY_GID in
# docker-compose.yml. A high fixed GID avoids granting access to a common host
# group while keeping the private key unreadable to everyone else.
envoy_cert_gid=2443

private_key_path=$(sed -n 's|^[[:space:]]*filename: "\(/etc/letsencrypt/live/[^" ]*/privkey\.pem\)"|\1|p' \
    volumes/envoy/envoy.prod.yaml | head -n 1)
certificate_path=$(sed -n 's|^[[:space:]]*filename: "\(/etc/letsencrypt/live/[^" ]*/fullchain\.pem\)"|\1|p' \
    volumes/envoy/envoy.prod.yaml | head -n 1)

if [ -z "$private_key_path" ] || [ -z "$certificate_path" ]; then
    echo "ERROR: Unable to resolve Envoy TLS paths from volumes/envoy/envoy.prod.yaml" >&2
    exit 1
fi

lineage=$(basename "$(dirname "$private_key_path")")
case "$lineage" in
    ""|.|..|*[!A-Za-z0-9._-]*)
        echo "ERROR: Unsafe Certbot lineage name in Envoy configuration: $lineage" >&2
        exit 1
        ;;
esac

host_key="volumes/certs/live/$lineage/privkey.pem"
host_certificate="volumes/certs/live/$lineage/fullchain.pem"
if [ ! -f "$host_key" ] || [ ! -f "$host_certificate" ]; then
    echo "ERROR: Envoy TLS certificate or private key is missing for lineage '$lineage'" >&2
    exit 1
fi

# Certbot's live files are symlinks into archive/. Both the target key and all
# traversed directories must be accessible to Envoy's dedicated group.
chgrp "$envoy_cert_gid" "$host_key"
chmod 640 "$host_key"
chmod 644 "$host_certificate"
chmod 755 volumes/certs volumes/certs/live "volumes/certs/live/$lineage"

if [ -d "volumes/certs/archive/$lineage" ]; then
    chgrp "$envoy_cert_gid" volumes/certs/archive "volumes/certs/archive/$lineage"
    chmod 750 volumes/certs/archive "volumes/certs/archive/$lineage"
fi

echo "  [OK] Envoy can read the private key through dedicated GID $envoy_cert_gid"
