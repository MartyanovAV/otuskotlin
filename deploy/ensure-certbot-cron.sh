#!/bin/sh
set -eu

deploy_dir=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
renew_script="$deploy_dir/certbot-renew.sh"
cron_marker="# FitBridge Certbot renewal"
# Run twice a day at a random minute per EFF best practice: this gives ~60
# renewal attempts before a 90-day Let's Encrypt certificate expires, which
# is the only sane way to survive an extended outage of the renewal pipeline
# (DNS, ACME server, container runtime, ...).
cron_entry="23 4,16 * * * sh \"$renew_script\" >> \"$deploy_dir/certbot-renew.log\" 2>&1"

# Hard precondition: the renewal lineage for the configured public domain must
# already exist. Without it,
# `certbot renew` is a no-op and the cron would silently do nothing for the
# lifetime of the certificate (until the bootstrap expires). Force the operator
# to run certbot-init.sh first.
if [ ! -f "$deploy_dir/.env" ]; then
    echo "ERROR: Production environment is missing: $deploy_dir/.env" >&2
    exit 1
fi
set -a
. "$deploy_dir/.env"
set +a
: "${FITBRIDGE_PUBLIC_URL:?FITBRIDGE_PUBLIC_URL is required in $deploy_dir/.env}"
domain=$(sh "$deploy_dir/resolve-public-domain.sh" "$FITBRIDGE_PUBLIC_URL")
if ! sh "$deploy_dir/certbot-lineage-exists.sh" "$domain"; then
    echo "ERROR: No valid Let's Encrypt renewal lineage found for '$domain'" >&2
    echo "ERROR: Run 'sh ./certbot-init.sh' once to issue the initial certificate, then re-run this script" >&2
    exit 1
fi

if ! command -v crontab >/dev/null 2>&1; then
    echo "ERROR: crontab is required to configure Certbot renewal" >&2
    exit 1
fi

existing_crontab=$(crontab -l 2>/dev/null || true)
clean_crontab=$(printf '%s\n' "$existing_crontab" |
    grep -Fv "$cron_marker" |
    grep -Fv "$renew_script" || true)

{
    if [ -n "$clean_crontab" ]; then
        printf '%s\n' "$clean_crontab"
    fi
    printf '%s\n' "$cron_marker"
    printf '%s\n' "$cron_entry"
} | crontab -

echo "==> Certbot renewal cron configured: $cron_entry"
