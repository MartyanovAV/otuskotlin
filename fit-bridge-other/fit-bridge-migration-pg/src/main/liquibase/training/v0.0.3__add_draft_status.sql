--liquibase formatted sql

--changeset martyanovav:4 labels:v0.0.3
ALTER TYPE "training_plan_status_type" ADD VALUE 'DRAFT' BEFORE 'ACTIVE';
