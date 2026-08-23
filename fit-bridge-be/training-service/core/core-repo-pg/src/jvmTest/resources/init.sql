--liquibase formatted sql

--changeset martyanovav:1 labels:v0.0.1
CREATE TYPE "client_card_status_type" AS ENUM ('ACTIVE', 'ARCHIVED');
CREATE TYPE "training_plan_status_type" AS ENUM ('ACTIVE', 'ARCHIVED', 'COMPLETED');
CREATE TYPE "workout_difficulty_type" AS ENUM ('EASY', 'NORMAL', 'HARD', 'MAX');

CREATE TABLE "client_card" (
    "id" text primary key constraint client_card_id_len check (length("id") between 1 and 64),
    "owner_user_id" text not null constraint client_card_owner_user_id_len check (length("owner_user_id") between 1 and 128),
    "created_by_user_id" text not null constraint client_card_created_by_user_id_len check (length("created_by_user_id") between 1 and 128),
    "display_name" text not null constraint client_card_dn_len check (length("display_name") between 1 and 120),
    "note" text not null default '' constraint client_card_note_len check (length("note") <= 1000),
    "status" client_card_status_type not null default 'ACTIVE',
    "lock" text not null default gen_random_uuid()::text,
    "created_at" timestamp without time zone not null default (now() at time zone 'utc'),
    "updated_at" timestamp without time zone not null default (now() at time zone 'utc'),
    "archived_at" timestamp without time zone
);

CREATE INDEX client_card_search_idx ON "client_card" ("owner_user_id", "status", "display_name");

--changeset martyanovav:2 labels:v0.0.1
CREATE TABLE "training_plan" (
    "id" text primary key constraint training_plan_id_len check (length("id") between 1 and 64),
    "client_card_id" text not null references "client_card"("id"),
    "owner_user_id" text not null constraint training_plan_owner_user_id_len check (length("owner_user_id") between 1 and 128),
    "created_by_user_id" text not null constraint training_plan_created_by_user_id_len check (length("created_by_user_id") between 1 and 128),
    "title" text not null constraint training_plan_title_len check (length("title") between 3 and 120),
    "plan_items" jsonb not null constraint training_plan_items_array check (jsonb_typeof("plan_items") = 'array' and jsonb_array_length("plan_items") between 1 and 200),
    "status" training_plan_status_type not null default 'ACTIVE',
    "version" integer not null default 1,
    "lock" text not null default gen_random_uuid()::text,
    "created_at" timestamp without time zone not null default (now() at time zone 'utc'),
    "updated_at" timestamp without time zone not null default (now() at time zone 'utc'),
    "archived_at" timestamp without time zone,
    "completed_at" timestamp without time zone,
    "difficulty" workout_difficulty_type,
    "coach_comment" text not null default '' constraint training_plan_coach_comment_len check (length("coach_comment") <= 1000)
);

CREATE INDEX training_plan_client_card_id_idx ON "training_plan" ("client_card_id");
CREATE INDEX training_plan_search_idx ON "training_plan" ("owner_user_id", "client_card_id", "status", "title");