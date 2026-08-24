#!/bin/sh
set -eu

cd "$(dirname "$0")"

if [ -f .env ]; then
    set -a
    . ./.env
    set +a
fi

: "${FITBRIDGE_PUBLIC_URL:?FITBRIDGE_PUBLIC_URL is required}"

public_url="${FITBRIDGE_PUBLIC_URL%/}"
domain=$(sh ./resolve-public-domain.sh "$public_url")
max_attempts="${VERIFY_MAX_ATTEMPTS:-24}"
retry_delay="${VERIFY_RETRY_DELAY_SECONDS:-5}"

wait_for_url() {
    name="$1"
    url="$2"
    attempt=1

    while [ "$attempt" -le "$max_attempts" ]; do
        if curl --fail --silent --show-error \
            --connect-timeout 5 \
            --max-time 10 \
            --output /dev/null \
            "$url"; then
            echo "  [OK] $name"
            return 0
        fi

        echo "  [WAIT] $name ($attempt/$max_attempts)"
        attempt=$((attempt + 1))
        sleep "$retry_delay"
    done

    echo "ERROR: $name did not become available: $url" >&2
    return 1
}

wait_for_keycloak_redirect() {
    attempt=1
    authorization_url="$public_url/realms/fit-bridge/protocol/openid-connect/auth"

    while [ "$attempt" -le "$max_attempts" ]; do
        http_code=$(curl --silent --show-error \
            --connect-timeout 5 \
            --max-time 10 \
            --output /dev/null \
            --write-out '%{http_code}' \
            --get "$authorization_url" \
            --data-urlencode 'client_id=fit-bridge-web' \
            --data-urlencode "redirect_uri=$public_url" \
            --data-urlencode 'response_type=code' \
            --data-urlencode 'scope=openid' \
            --data-urlencode 'state=fitbridge-deploy-check' \
            --data-urlencode 'nonce=fitbridge-deploy-check' \
            --data-urlencode 'code_challenge=5Z2g3g8H9B2Kj9Q7jBjNqLYVjWJzVJsT-Eb3P2YkJkM' \
            --data-urlencode 'code_challenge_method=S256' || true)
        case "$http_code" in
            200|302|303)
                echo "  [OK] Keycloak client redirect URI"
                return 0
                ;;
        esac

        echo "  [WAIT] Keycloak client redirect URI (HTTP $http_code, $attempt/$max_attempts)"
        attempt=$((attempt + 1))
        sleep "$retry_delay"
    done

    echo "ERROR: Keycloak rejected redirect URI '$public_url' for client 'fit-bridge-web'" >&2
    echo "ERROR: Startup --import-realm skips an existing realm; reconcile the live client instead of overriding the realm and losing state" >&2
    return 1
}

echo "==> Verifying public production endpoints at $public_url"
wait_for_url "Envoy HTTPS health" "$public_url/health"
wait_for_url "Training Service readiness" "$public_url/health/training/ready"
wait_for_url "Keycloak OIDC discovery" "$public_url/realms/fit-bridge/.well-known/openid-configuration"
wait_for_keycloak_redirect
wait_for_url "Frontend" "$public_url/"

certificate_file=$(mktemp)
trap 'rm -f "$certificate_file"' EXIT HUP INT TERM

echo "==> Inspecting the certificate served publicly by $domain:443"
if ! openssl s_client \
    -connect "$domain:443" \
    -servername "$domain" \
    -showcerts </dev/null 2>/dev/null \
    | openssl x509 -outform PEM > "$certificate_file"; then
    echo "ERROR: Unable to read the public TLS certificate for $domain" >&2
    exit 1
fi

openssl x509 -in "$certificate_file" -noout -checkhost "$domain"
if ! openssl x509 -in "$certificate_file" -noout -checkend 604800; then
    echo "ERROR: Public TLS certificate for $domain expires in less than 7 days" >&2
    exit 1
fi

issuer=$(openssl x509 -in "$certificate_file" -noout -issuer | sed 's/^issuer=//')
enddate=$(openssl x509 -in "$certificate_file" -noout -enddate | sed 's/^notAfter=//')
echo "  [OK] Public certificate: issuer=$issuer expires=$enddate"
echo "==> Production verification completed successfully"
