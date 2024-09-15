--liquibase formatted sql

--changeset roman:1
CREATE TABLE statistic
(
    id BIGINT PRIMARY KEY REFERENCES site(id) ON DELETE CASCADE,
    pages BIGINT,
    lemmas BIGINT
);