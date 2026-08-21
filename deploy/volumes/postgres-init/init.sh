#!/bin/bash
set -e

# Убеждаемся, что база по умолчанию существует, чтобы psql мог подключиться
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- 1. Keycloak: своя БД и свой пользователь
    CREATE USER ${KC_DB_USERNAME:-keycloak} WITH PASSWORD '${KC_DB_PASSWORD:-keycloak-password}';
    CREATE DATABASE ${KC_DB_NAME:-keycloak_db} OWNER ${KC_DB_USERNAME:-keycloak};

    -- 2. Учетка Liquibase (создает таблицы, владеет схемой)
    CREATE USER ${LIQUIBASE_DB_USERNAME:-liquibase} WITH PASSWORD '${LIQUIBASE_DB_PASSWORD:-liquibase-password}';

    -- 3. Учетка Training Service (только DML: SELECT, INSERT, UPDATE, DELETE)
    CREATE USER ${DB_USER:-training_user} WITH PASSWORD '${DB_PASSWORD:-training-password}';

    -- БД training_db принадлежит Liquibase
    CREATE DATABASE ${DB_NAME:-training_db} OWNER ${LIQUIBASE_DB_USERNAME:-liquibase};
EOSQL

# Настраиваем права для Training Service в БД training_db
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "${DB_NAME:-training_db}" <<-EOSQL
    -- Даем права на схему public для микросервиса (необходимо в PostgreSQL 15+)
    GRANT USAGE, CREATE ON SCHEMA public TO ${DB_USER:-training_user};

    -- Гарантируем, что новые таблицы, создаваемые Liquibase, будут доступны микросервису
    ALTER DEFAULT PRIVILEGES FOR ROLE ${LIQUIBASE_DB_USERNAME:-liquibase} IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO ${DB_USER:-training_user};
    ALTER DEFAULT PRIVILEGES FOR ROLE ${LIQUIBASE_DB_USERNAME:-liquibase} IN SCHEMA public GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO ${DB_USER:-training_user};
EOSQL
