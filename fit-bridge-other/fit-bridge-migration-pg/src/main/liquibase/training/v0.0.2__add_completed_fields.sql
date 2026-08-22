--liquibase formatted sql

--changeset martyanovav:3 labels:v0.0.2
ALTER TYPE "training_plan_status_type" ADD VALUE 'COMPLETED';
CREATE TYPE "workout_difficulty_type" AS ENUM ('EASY', 'NORMAL', 'HARD', 'MAX');

ALTER TABLE "training_plan"
    ADD COLUMN "completed_at" timestamp without time zone,
    ADD COLUMN "difficulty" workout_difficulty_type,
    ADD COLUMN "coach_comment" text not null default '' constraint training_plan_coach_comment_len check (length("coach_comment") <= 1000);
