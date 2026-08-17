#!/bin/bash

TOKEN=$(./keycloak-tokens.sh)

curl -X POST -H "Authorization: Bearer ${TOKEN}" \
  -H "X-Request-ID: 1234" \
  -H "x-client-request-id: 1235" \
  -H "Content-Type: application/json" \
  -X POST \
  --data '{"requestType":"clientCard.search","requestId":"1234","clientCardFilter":{"pageSize":10,"pageNumber":1}}' \
  http://localhost:8080/v1/clientCard/search
