#!/bin/sh
set -eu

deploy_dir=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
renew_script="$deploy_dir/certbot-renew.sh"
cron_marker="# FitBridge Certbot renewal"
cron_entry="0 3 * * 1 sh $renew_script >> $deploy_dir/certbot-renew.log 2>&1"

if ! command -v crontab >/dev/null 2>&1; then
    echo "ERROR: crontab is required to configure Certbot renewal" >&2
    exit 1
fi

existing_crontab=$(crontab -l 2>/dev/null || true)
clean_crontab=$(printf '%s\n' "$existing_crontab" | grep -v -E 'FitBridge Certbot renewal|certbot-renew\.sh' || true)

{
    if [ -n "$clean_crontab" ]; then
        printf '%s\n' "$clean_crontab"
    fi
    printf '%s\n' "$cron_marker"
    printf '%s\n' "$cron_entry"
} | crontab -

echo "==> Certbot renewal cron configured: $cron_entry"
