#!/bin/bash
set -euo pipefail

kc_db_name="${KC_DB_NAME:-keycloak_db}"
kc_db_username="${KC_DB_USERNAME:-keycloak}"
kc_db_password="${KC_DB_PASSWORD:-keycloak-password}"
liquibase_db_username="${LIQUIBASE_DB_USERNAME:-liquibase}"
liquibase_db_password="${LIQUIBASE_DB_PASSWORD:-liquibase-password}"
training_db_name="${DB_NAME:-training_db}"
training_db_username="${DB_USER:-training_user}"
training_db_password="${DB_PASSWORD:-training-password}"

# This script runs once on the first start of the postgres data volume.
# It is intentionally idempotent: roles, databases and grants are only
# created when missing. If a previous deploy already provisioned the data
# volume (for example, with rotated secrets in the GH environment), the
# existing roles are left in place so we do not clobber their data.

# 1. Roles: create if missing; do not touch existing ones here, because the
#    operator may have rotated KC_DB_PASSWORD / DB_PASSWORD since the first
#    deploy and we have no way to know the previous password from inside this
#    script. Password rotation between deploys is handled separately by the
#    `db-bootstrap` compose service that runs on every `up`.
psql -v ON_ERROR_STOP=1 \
    -v kc_db_username="$kc_db_username" \
    -v kc_db_password="$kc_db_password" \
    -v liquibase_db_username="$liquibase_db_username" \
    -v liquibase_db_password="$liquibase_db_password" \
    -v training_db_username="$training_db_username" \
    -v training_db_password="$training_db_password" \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" <<'EOSQL'
    -- 1. Keycloak user
    SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'kc_db_username', :'kc_db_password')
    WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'kc_db_username')
    \gexec

    -- 2. Liquibase user
    SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'liquibase_db_username', :'liquibase_db_password')
    WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'liquibase_db_username')
    \gexec

    -- 3. Training Service user
    SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'training_db_username', :'training_db_password')
    WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'training_db_username')
    \gexec
EOSQL

# 2. Databases: created only when missing. CREATE DATABASE cannot run inside
#    a transaction block, so we resolve each one with a separate psql call.
create_db_if_missing() {
    local db_name="$1" owner="$2"
    psql -v ON_ERROR_STOP=1 \
        -v db_name="$db_name" \
        -v db_owner="$owner" \
        --username "$POSTGRES_USER" \
        --dbname "$POSTGRES_DB" <<'EOSQL'
        SELECT format('CREATE DATABASE %I OWNER %I', :'db_name', :'db_owner')
        WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = :'db_name')
        \gexec
EOSQL
}

create_db_if_missing "$kc_db_name" "$kc_db_username"
create_db_if_missing "$training_db_name" "$liquibase_db_username"

# 3. Grants for the Training Service inside training_db. Idempotent.
psql -v ON_ERROR_STOP=1 \
    -v liquibase_db_username="$liquibase_db_username" \
    -v training_db_username="$training_db_username" \
    --username "$POSTGRES_USER" \
    --dbname "$training_db_name" <<'EOSQL'
    -- Даем права на схему public для микросервиса (необходимо в PostgreSQL 15+)
    GRANT USAGE ON SCHEMA public TO :"training_db_username";

    -- Гарантируем, что новые таблицы, создаваемые Liquibase, будут доступны микросервису
    ALTER DEFAULT PRIVILEGES FOR ROLE :"liquibase_db_username" IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO :"training_db_username";
    ALTER DEFAULT PRIVILEGES FOR ROLE :"liquibase_db_username" IN SCHEMA public GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO :"training_db_username";
EOSQL
