#!/usr/bin/env bash
set -euo pipefail

KCHOST=http://localhost:8080
REALM=fit-bridge
CLIENT_ID=fit-bridge-smoke
UNAME=fitbridge-test
PASSWORD=fitbridge

TOKEN_RESPONSE="$(curl --fail-with-body --silent --show-error \
  --data-urlencode "client_id=$CLIENT_ID" \
  --data-urlencode "username=$UNAME" \
  --data-urlencode "password=$PASSWORD" \
  --data-urlencode "grant_type=password" \
  "$KCHOST/realms/$REALM/protocol/openid-connect/token")"

if [[ $TOKEN_RESPONSE =~ \"access_token\":\"([^\"]+)\" ]]; then
  ACCESS_TOKEN="${BASH_REMATCH[1]}"
else
  echo "Keycloak response does not contain access_token" >&2
  exit 1
fi

echo "$ACCESS_TOKEN"
