--liquibase formatted sql

--changeset martyanovav:1 labels:v0.0.1
CREATE TYPE "client_card_status_type" AS ENUM ('ACTIVE', 'ARCHIVED');
CREATE TYPE "training_plan_status_type" AS ENUM ('ACTIVE', 'ARCHIVED');

CREATE TABLE "client_card" (
    "id" text primary key constraint client_card_id_len check (length("id") between 1 and 64),
    "owner_id" text not null constraint client_card_owner_id_len check (length("owner_id") between 1 and 128),
    "display_name" text not null constraint client_card_dn_len check (length("display_name") between 1 and 120),
    "note" text not null default '' constraint client_card_note_len check (length("note") <= 1000),
    "status" client_card_status_type not null default 'ACTIVE',
    "lock" bigint not null default 1 constraint client_card_lock_positive check ("lock" > 0),
    "created_at" timestamptz not null default now(),
    "updated_at" timestamptz not null default now(),
    "archived_at" timestamptz
);

CREATE INDEX client_card_search_idx ON "client_card" ("owner_id", "status", "display_name");

--changeset martyanovav:2 labels:v0.0.1
CREATE TABLE "training_plan" (
    "id" text primary key constraint training_plan_id_len check (length("id") between 1 and 64),
    "client_card_id" text not null references "client_card"("id"),
    "owner_id" text not null constraint training_plan_owner_id_len check (length("owner_id") between 1 and 128),
    "title" text not null constraint training_plan_title_len check (length("title") between 3 and 120),
    "plan_items" jsonb not null constraint training_plan_items_array check (jsonb_typeof("plan_items") = 'array' and jsonb_array_length("plan_items") between 1 and 200),
    "status" training_plan_status_type not null default 'ACTIVE',
    "version" integer not null default 1,
    "lock" bigint not null default 1 constraint training_plan_lock_positive check ("lock" > 0),
    "created_at" timestamptz not null default now(),
    "updated_at" timestamptz not null default now(),
    "archived_at" timestamptz
);

CREATE INDEX training_plan_client_card_id_idx ON "training_plan" ("client_card_id");
CREATE INDEX training_plan_search_idx ON "training_plan" ("owner_id", "client_card_id", "status", "title");
