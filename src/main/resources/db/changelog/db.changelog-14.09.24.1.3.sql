--liquibase formatted sql

--changeset roman:1
CREATE TABLE statistic
(
    id BIGINT PRIMARY KEY REFERENCES site(id),
    pages BIGINT,
    lemmas BIGINT
);