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

# Убеждаемся, что база по умолчанию существует, чтобы psql мог подключиться
psql -v ON_ERROR_STOP=1 \
    -v kc_db_name="$kc_db_name" \
    -v kc_db_username="$kc_db_username" \
    -v kc_db_password="$kc_db_password" \
    -v liquibase_db_username="$liquibase_db_username" \
    -v liquibase_db_password="$liquibase_db_password" \
    -v training_db_name="$training_db_name" \
    -v training_db_username="$training_db_username" \
    -v training_db_password="$training_db_password" \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" <<'EOSQL'
    -- 1. Keycloak: своя БД и свой пользователь
    CREATE USER :"kc_db_username" WITH PASSWORD :'kc_db_password';
    CREATE DATABASE :"kc_db_name" OWNER :"kc_db_username";

    -- 2. Учетка Liquibase (создает таблицы, владеет схемой)
    CREATE USER :"liquibase_db_username" WITH PASSWORD :'liquibase_db_password';

    -- 3. Учетка Training Service (только DML: SELECT, INSERT, UPDATE, DELETE)
    CREATE USER :"training_db_username" WITH PASSWORD :'training_db_password';

    -- БД training_db принадлежит Liquibase
    CREATE DATABASE :"training_db_name" OWNER :"liquibase_db_username";
EOSQL

# Настраиваем права для Training Service в БД training_db
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
