#!/bin/bash
set -euo pipefail

: "${PGHOST:?PGHOST is required}"
: "${PGUSER:?PGUSER is required}"
: "${PGDATABASE:?PGDATABASE is required}"
: "${KC_DB_NAME:?KC_DB_NAME is required}"
: "${KC_DB_USERNAME:?KC_DB_USERNAME is required}"
: "${KC_DB_PASSWORD:?KC_DB_PASSWORD is required}"
: "${LIQUIBASE_DB_USERNAME:?LIQUIBASE_DB_USERNAME is required}"
: "${LIQUIBASE_DB_PASSWORD:?LIQUIBASE_DB_PASSWORD is required}"
: "${DB_NAME:?DB_NAME is required}"
: "${DB_USER:?DB_USER is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"

sync_role() {
    local role_name="$1" role_password="$2"
    psql -v ON_ERROR_STOP=1 \
        -v role_name="$role_name" \
        -v role_password="$role_password" <<'EOSQL'
        SELECT format(
            CASE
                WHEN EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'role_name')
                    THEN 'ALTER ROLE %I WITH LOGIN PASSWORD %L'
                ELSE 'CREATE ROLE %I LOGIN PASSWORD %L'
            END,
            :'role_name',
            :'role_password'
        )
        \gexec
EOSQL
}

ensure_database() {
    local database_name="$1" database_owner="$2"
    psql -v ON_ERROR_STOP=1 \
        -v database_name="$database_name" \
        -v database_owner="$database_owner" <<'EOSQL'
        SELECT format('CREATE DATABASE %I OWNER %I', :'database_name', :'database_owner')
        WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = :'database_name')
        \gexec

        SELECT format('ALTER DATABASE %I OWNER TO %I', :'database_name', :'database_owner')
        \gexec
EOSQL
}

sync_role "$KC_DB_USERNAME" "$KC_DB_PASSWORD"
sync_role "$LIQUIBASE_DB_USERNAME" "$LIQUIBASE_DB_PASSWORD"
sync_role "$DB_USER" "$DB_PASSWORD"

ensure_database "$KC_DB_NAME" "$KC_DB_USERNAME"
ensure_database "$DB_NAME" "$LIQUIBASE_DB_USERNAME"

psql -v ON_ERROR_STOP=1 \
    -v database_name="$DB_NAME" \
    -v liquibase_db_username="$LIQUIBASE_DB_USERNAME" \
    -v training_db_username="$DB_USER" \
    --dbname "$DB_NAME" <<'EOSQL'
    GRANT USAGE ON SCHEMA public TO :"training_db_username";
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO :"training_db_username";
    GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO :"training_db_username";

    ALTER DEFAULT PRIVILEGES FOR ROLE :"liquibase_db_username" IN SCHEMA public
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO :"training_db_username";
    ALTER DEFAULT PRIVILEGES FOR ROLE :"liquibase_db_username" IN SCHEMA public
        GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO :"training_db_username";
EOSQL

echo "==> Database roles, passwords, ownership and grants are synchronized"
