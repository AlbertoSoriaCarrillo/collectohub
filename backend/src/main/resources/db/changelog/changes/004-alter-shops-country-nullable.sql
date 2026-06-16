--liquibase formatted sql

--changeset codex:004-alter-shops-country-nullable
ALTER TABLE shops ALTER COLUMN country DROP NOT NULL;

--rollback ALTER TABLE shops ALTER COLUMN country SET NOT NULL;
