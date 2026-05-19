#!/bin/bash

KCHOST=http://localhost:8080
REALM=fit-bridge
CLIENT_ID=fit-bridge-service
UNAME=fitbridge-test
PASSWORD=fitbridge

ACCESS_TOKEN=`curl \
  -d "client_id=$CLIENT_ID" \
  -d "username=$UNAME" \
  -d "password=$PASSWORD" \
  -d "grant_type=password" \
  "$KCHOST/realms/$REALM/protocol/openid-connect/token"  | jq -r '.access_token'`
echo "$ACCESS_TOKEN"
