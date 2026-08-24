#!/bin/bash

JKS_PASS=app123456

DIR_CA=volumes/ca
DIR_CADDY=volumes/caddy/certs
DIR_KC=volumes/keycloak/certs

mkdir -p $DIR_CA
mkdir -p $DIR_CADDY
mkdir -p $DIR_KC

openssl genpkey -algorithm RSA -out $DIR_CA/ca.key
openssl req -x509 -new -nodes -key $DIR_CA/ca.key -sha256 -days 365 -out $DIR_CA/ca.crt -subj "/CN=fit_bridge_ca"

openssl genpkey -algorithm RSA -out $DIR_CADDY/caddy_sidecar.key
openssl req -new -key $DIR_CADDY/caddy_sidecar.key -out $DIR_CADDY/caddy_sidecar.csr -subj "/CN=caddy_sidecar"
openssl x509 -req -in $DIR_CADDY/caddy_sidecar.csr -CA $DIR_CA/ca.crt -CAkey $DIR_CA/ca.key -CAcreateserial -out $DIR_CADDY/caddy_sidecar.crt -days 365 -sha256

openssl genpkey -algorithm RSA -out $DIR_KC/keycloak.key
openssl req -new -key $DIR_KC/keycloak.key -out $DIR_KC/keycloak.csr -subj "/CN=keycloak"
openssl x509 -req -in $DIR_KC/keycloak.csr -CA $DIR_CA/ca.crt -CAkey $DIR_CA/ca.key -CAcreateserial -out $DIR_KC/keycloak.crt -days 365 -sha256

openssl pkcs12 -export -out $DIR_KC/keycloak.p12 -inkey $DIR_KC/keycloak.key -in $DIR_KC/keycloak.crt -name keycloak -passout pass:$JKS_PASS
keytool -importkeystore -srckeystore $DIR_KC/keycloak.p12 -srcstoretype pkcs12 -destkeystore $DIR_KC/keycloak.jks -deststoretype JKS -srcstorepass $JKS_PASS -deststorepass $JKS_PASS

echo "Certificates generated successfully!"
